package com.techtron.onebook.module.app.service.auction;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.app.auction.vo.AppAuctionCreateReqVO;
import com.techtron.onebook.module.app.controller.app.auction.vo.AppAuctionPageReqVO;
import com.techtron.onebook.module.app.controller.app.auction.vo.AppAuctionRespVO;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionDO;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionSettlementDO;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionMapper;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionSettlementMapper;
import com.techtron.onebook.module.app.dal.mysql.category.CollectionCategoryMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.service.notify.AppSubscribeMessageService;
import com.techtron.onebook.module.infra.api.config.ConfigApi;
import com.techtron.onebook.module.pay.api.wallet.PayWalletApi;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import com.techtron.onebook.module.pay.enums.wallet.PayWalletBizTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.framework.common.enums.UserTypeEnum.MEMBER;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.*;

@Service
@Slf4j
public class AuctionServiceImpl implements AuctionService {
    private static final int PENDING_MANUAL_PUBLISH = 0;
    private static final int ACTIVE = 1;
    private static final int PENDING_SALE_CONFIRMATION = 2;
    private static final int SUCCESS = 3;
    private static final int UNSOLD = 4;
    private static final int CANCELLED = 5;
    private static final int SYNC_EXCEPTION = 6;
    private static final int REVIEW_REJECTED = 7;
    private static final int DELIST_NONE = 0;
    private static final int DELIST_PENDING = 1;
    private static final int DELIST_APPROVED = 2;
    private static final int DELIST_REJECTED = 3;
    private static final Pattern RAW_GOOFISH_ITEM_ID = Pattern.compile("^\\d{8,20}$");
    private static final Pattern GOOFISH_ITEM_ID_IN_LINK = Pattern.compile(
            "(?i)(?:item[_-]?id|id)\\s*[=/:]\\s*(\\d{8,20})");

    @Resource private AuctionMapper auctionMapper;
    @Resource private AuctionSettlementMapper auctionSettlementMapper;
    @Resource private CollectionMapper collectionMapper;
    @Resource private CollectionCategoryMapper collectionCategoryMapper;
    @Resource private ConfigApi configApi;
    @Resource private PayWalletApi payWalletApi;
    @Resource private AppSubscribeMessageService subscribeMessageService;
    @Resource @Lazy private AuctionService self;
    @Resource private com.techtron.onebook.module.app.service.yikoujia.YikoujiaService yikoujiaService;

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 60)
    public void bindManagedProduct(Long adminUserId, Long auctionId, String productId) {
        if (productId == null || !productId.matches("[0-9]{8,18}")) throw new com.techtron.onebook.framework.common.exception.ServiceException(400, "请输入闲管家product_id");
        AuctionDO auction = auctionMapper.selectByIdForUpdate(auctionId);
        if (auction == null) throw exception(AUCTION_NOT_EXISTS);
        if (!List.of(PENDING_MANUAL_PUBLISH, ACTIVE, PENDING_SALE_CONFIRMATION, SYNC_EXCEPTION).contains(auction.getStatus())) throw exception(AUCTION_STATUS_INVALID);
        AuctionProductSnapshot snapshot;
        try {
            snapshot = AuctionProductSnapshot.parse(productId, auction.getGoofishProductId(),
                    yikoujiaService.requestGoofishProductDetail(productId));
        } catch (IllegalArgumentException ex) {
            throw new com.techtron.onebook.framework.common.exception.ServiceException(400, ex.getMessage());
        }
        if (List.of(PENDING_MANUAL_PUBLISH, SYNC_EXCEPTION).contains(auction.getStatus())) {
            bindGoofishAuction(adminUserId, auctionId, snapshot.itemId());
        }
        AuctionDO update = new AuctionDO().setId(auctionId).setGoofishManagedProductId(productId)
                .setGoofishDetail(snapshot.json()).setLastSyncTime(LocalDateTime.now())
                .setSyncError("已读取闲管家基础信息；接口价格非实时竞拍价，成交结果需人工确认");
        if (!snapshot.images().isEmpty()) update.setDisplayPicUrls(snapshot.images());
        if (snapshot.needsConfirmation()) update.setStatus(PENDING_SALE_CONFIRMATION)
                .setSyncError("接口返回售出或下架时间信息，请人工核对拍卖结果；未自动结算");
        auctionMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 10, isolation = Isolation.SERIALIZABLE)
    public Long createAuction(Long sellerId, AppAuctionCreateReqVO req) {
        if (req.getEndTime().isBefore(LocalDateTime.now().plusMinutes(5))) {
            throw exception(AUCTION_END_TIME_INVALID);
        }
        if (auctionMapper.countActiveBySeller(sellerId) >= 10) {
            throw exception(AUCTION_ACTIVE_LIMIT);
        }
        CollectionDO collection = collectionMapper.selectByIdForUpdate(req.getCollectionId());
        if (collection == null || !Objects.equals(collection.getUserId(), sellerId)
                || !Objects.equals(collection.getStatus(), 1)
                || (collection.getTradeStatus() != null && collection.getTradeStatus() != 0)) {
            throw exception(AUCTION_COLLECTION_INVALID);
        }
        if (!Objects.equals(collection.getStock(), 1)) {
            throw exception(AUCTION_SINGLE_ITEM_REQUIRED);
        }
        CollectionCategoryDO category = collectionCategoryMapper.selectById(collection.getCategoryId());
        if (!isAuctionEligible(collection, category)) {
            throw exception(AUCTION_COLLECTION_TYPE_INVALID);
        }
        collectionMapper.updateById(new CollectionDO().setId(collection.getId()).setUserId(0L).setTradeStatus(2));
        List<String> images = resolveImages(collection);
        AuctionDO auction = AuctionDO.builder()
                .sellerId(sellerId).collectionId(collection.getId())
                .collectionName(collection.getName()).categoryName(collection.getCategoryName())
                .picUrl(images.isEmpty() ? null : images.get(0)).picUrls(images).amount(1)
                .startPrice(req.getStartPrice()).minIncrement(req.getMinIncrement())
                .currentPrice(req.getStartPrice()).bidCount(0).status(PENDING_MANUAL_PUBLISH)
                .delistStatus(DELIST_NONE)
                .endTime(req.getEndTime()).build();
        auctionMapper.insert(auction);
        return auction.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void cancelAuction(Long sellerId, Long auctionId) {
        AuctionDO auction = auctionMapper.selectByIdForUpdate(auctionId);
        if (auction == null) throw exception(AUCTION_NOT_EXISTS);
        if (!Objects.equals(auction.getSellerId(), sellerId)
                || !List.of(PENDING_MANUAL_PUBLISH, SYNC_EXCEPTION).contains(auction.getStatus())) {
            throw exception(AUCTION_STATUS_INVALID);
        }
        restoreCollection(auction);
        auctionMapper.updateById(new AuctionDO().setId(auctionId).setStatus(CANCELLED)
                .setSettledTime(LocalDateTime.now()).setSyncError(null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 10)
    public void requestDelist(Long sellerId, Long auctionId) {
        AuctionDO auction = auctionMapper.selectByIdForUpdate(auctionId);
        if (auction == null) throw exception(AUCTION_NOT_EXISTS);
        if (!Objects.equals(auction.getSellerId(), sellerId) || auction.getStatus() != ACTIVE) {
            throw exception(AUCTION_STATUS_INVALID);
        }
        if (Objects.equals(auction.getDelistStatus(), DELIST_PENDING)) {
            throw exception(AUCTION_DELIST_ALREADY_PENDING);
        }
        auctionMapper.markDelistRequested(auctionId);
    }

    @Override
    public void bindGoofishAuction(Long adminUserId, Long auctionId, String goofishUrl) {
        AuctionDO auction = requireAuction(auctionId);
        if (!List.of(PENDING_MANUAL_PUBLISH, SYNC_EXCEPTION).contains(auction.getStatus())) {
            throw exception(AUCTION_STATUS_INVALID);
        }

        String itemId = resolveGoofishAuctionItemId(goofishUrl);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime managedEndTime = resolveManagedEndTime(auction, now);
        Long duplicated = auctionMapper.selectCount(new LambdaQueryWrapper<AuctionDO>()
                .eq(AuctionDO::getGoofishProductId, itemId)
                .ne(AuctionDO::getId, auctionId));
        if (duplicated > 0) throw exception(AUCTION_GOOFISH_ALREADY_BOUND);

        int updated = auctionMapper.update(new AuctionDO()
                        .setStatus(ACTIVE)
                        .setGoofishProductId(itemId)
                        .setGoofishUrl("https://www.goofish.com/item?id=" + itemId)
                        .setCurrentPrice(auction.getStartPrice())
                        .setBidCount(0)
                        .setEndTime(managedEndTime)
                        .setReviewTime(now)
                        .setReviewUserId(adminUserId)
                        .setReviewRejectReason(null)
                        .setLastSyncTime(now)
                        .setSyncError(null),
                new UpdateWrapper<AuctionDO>()
                        .set("review_reject_reason", null)
                        .set("sync_error", null)
                        .eq("id", auctionId)
                        .in("status", PENDING_MANUAL_PUBLISH, SYNC_EXCEPTION));
        if (updated == 0) throw exception(AUCTION_MANUAL_BIND_FAILED);
        subscribeMessageService.notifyAuctionPublished(auction.getSellerId(), auction.getCollectionName(),
                auction.getStartPrice(), LocalDateTime.now());
    }

    static LocalDateTime resolveManagedEndTime(AuctionDO auction, LocalDateTime now) {
        if (auction.getEndTime() == null) {
            throw exception(AUCTION_END_TIME_INVALID);
        }
        if (auction.getCreateTime() != null) {
            Duration requestedDuration = Duration.between(auction.getCreateTime(), auction.getEndTime());
            if (requestedDuration.compareTo(Duration.ofMinutes(5)) >= 0) {
                return now.plus(requestedDuration);
            }
        }
        if (auction.getEndTime().isAfter(now.plusMinutes(5))) {
            return auction.getEndTime();
        }
        throw exception(AUCTION_END_TIME_INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 10)
    public void rejectPublish(Long adminUserId, Long auctionId, String reason) {
        AuctionDO auction = auctionMapper.selectByIdForUpdate(auctionId);
        if (auction == null) throw exception(AUCTION_NOT_EXISTS);
        if (!List.of(PENDING_MANUAL_PUBLISH, SYNC_EXCEPTION).contains(auction.getStatus())) {
            throw exception(AUCTION_STATUS_INVALID);
        }
        restoreCollection(auction);
        auctionMapper.updateById(new AuctionDO().setId(auctionId).setStatus(REVIEW_REJECTED)
                .setReviewTime(LocalDateTime.now()).setReviewUserId(adminUserId)
                .setReviewRejectReason(reason.trim()).setSettledTime(LocalDateTime.now())
                .setSyncError(null));
        subscribeMessageService.notifyReviewResult(auction.getSellerId(), "未通过", "竞拍申请",
                reason, LocalDateTime.now(), auction.getCollectionName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void approveDelist(Long adminUserId, Long auctionId) {
        AuctionDO auction = auctionMapper.selectByIdForUpdate(auctionId);
        requirePendingDelist(auction);
        // 竞拍由管理员在闲鱼人工发布，审核通过前管理员应先在闲鱼确认下架。
        restoreCollection(auction);
        auctionMapper.updateById(new AuctionDO().setId(auctionId).setStatus(CANCELLED)
                .setDelistStatus(DELIST_APPROVED).setDelistAuditTime(LocalDateTime.now())
                .setDelistAuditUserId(adminUserId).setDelistRejectReason(null)
                .setSettledTime(LocalDateTime.now()).setSyncError(null));
        subscribeMessageService.notifyReviewResult(auction.getSellerId(), "已通过", "下架申请",
                "竞拍商品已下架", LocalDateTime.now(), auction.getCollectionName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 10)
    public void rejectDelist(Long adminUserId, Long auctionId, String reason) {
        AuctionDO auction = auctionMapper.selectByIdForUpdate(auctionId);
        requirePendingDelist(auction);
        auctionMapper.updateById(new AuctionDO().setId(auctionId).setDelistStatus(DELIST_REJECTED)
                .setDelistAuditTime(LocalDateTime.now()).setDelistAuditUserId(adminUserId)
                .setDelistRejectReason(reason.trim()));
        subscribeMessageService.notifyReviewResult(auction.getSellerId(), "未通过", "下架申请",
                reason, LocalDateTime.now(), auction.getCollectionName());
    }

    /**
     * 管理员在闲鱼后台核对“订单已付款且无退款”后，锁定结算快照并把卖家净收入记入钱包。
     * 唯一结算单 + 拍卖行锁共同保证重复点击不会重复入账。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 10)
    public void confirmSale(Long adminUserId, Long auctionId, Integer grossAmount, String remark) {
        AuctionDO auction = auctionMapper.selectByIdForUpdate(auctionId);
        if (auction == null) throw exception(AUCTION_NOT_EXISTS);
        if (auction.getStatus() != PENDING_SALE_CONFIRMATION) throw exception(AUCTION_STATUS_INVALID);
        if (auctionSettlementMapper.selectByAuctionId(auctionId) != null) {
            throw exception(AUCTION_SETTLEMENT_EXISTS);
        }

        int feeRate = requireFeeRate();
        int feeAmount = (int) (((long) grossAmount * feeRate + 50L) / 100L);
        int sellerIncome = grossAmount - feeAmount;
        if (sellerIncome < 0) throw exception(AUCTION_FEE_NOT_CONFIGURED);

        AuctionSettlementDO settlement = AuctionSettlementDO.builder()
                .auctionId(auctionId).sellerId(auction.getSellerId())
                .grossAmount(grossAmount).feeRate(feeRate).feeAmount(feeAmount)
                .sellerIncome(sellerIncome).status(1).confirmUserId(adminUserId)
                .confirmRemark(remark.trim()).retryCount(0).settledTime(LocalDateTime.now())
                .build();
        auctionSettlementMapper.insert(settlement);

        payWalletApi.addWalletBalance(new PayWalletAddBalanceReqDTO()
                .setUserId(auction.getSellerId()).setUserType(MEMBER.getValue())
                .setBizType(PayWalletBizTypeEnum.AUCTION_INCOME.getType())
                .setBizId("auction-settlement:" + settlement.getId()).setPrice(sellerIncome));

        int updated = auctionMapper.update(new AuctionDO().setId(auctionId).setStatus(SUCCESS)
                        .setCurrentPrice(grossAmount).setSettledTime(LocalDateTime.now()),
                new UpdateWrapper<AuctionDO>().eq("id", auctionId).eq("status", PENDING_SALE_CONFIRMATION));
        if (updated == 0) throw exception(AUCTION_STATUS_INVALID);
        subscribeMessageService.notifyAuctionSuccess(auction.getSellerId(), auction.getCollectionName(),
                grossAmount, auction.getEndTime());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 10)
    public void confirmUnsold(Long adminUserId, Long auctionId) {
        AuctionDO auction = auctionMapper.selectByIdForUpdate(auctionId);
        if (auction == null) throw exception(AUCTION_NOT_EXISTS);
        if (auction.getStatus() != PENDING_SALE_CONFIRMATION) throw exception(AUCTION_STATUS_INVALID);
        if (auctionSettlementMapper.selectByAuctionId(auctionId) != null) {
            throw exception(AUCTION_SETTLEMENT_EXISTS);
        }
        restoreCollection(auction);
        int updated = auctionMapper.update(new AuctionDO().setId(auctionId).setStatus(UNSOLD)
                        .setReviewUserId(adminUserId).setSettledTime(LocalDateTime.now()).setSyncError(null),
                new UpdateWrapper<AuctionDO>().set("sync_error", null)
                        .eq("id", auctionId).eq("status", PENDING_SALE_CONFIRMATION));
        if (updated == 0) throw exception(AUCTION_STATUS_INVALID);
        subscribeMessageService.notifyReviewResult(auction.getSellerId(), "未成交", "竞拍结果",
                "本次竞拍已流拍，藏品已恢复到您的仓库", LocalDateTime.now(), auction.getCollectionName());
    }

    @Override
    public AppAuctionRespVO getAuction(Long id) {
        AuctionDO auction = requireAuction(id);
        fillMissingImages(Collections.singletonList(auction));
        return toResp(auction);
    }

    @Override
    public PageResult<AppAuctionRespVO> getAuctionPage(AppAuctionPageReqVO req) {
        PageResult<AuctionDO> page = auctionMapper.selectPage(req);
        fillMissingImages(page.getList());
        return new PageResult<>(page.getList().stream().map(this::toResp).toList(), page.getTotal());
    }

    @Override
    public PageResult<AppAuctionRespVO> getMyAuctionPage(Long userId, AppAuctionPageReqVO req) {
        LambdaQueryWrapper<AuctionDO> query = new LambdaQueryWrapper<AuctionDO>().eq(AuctionDO::getSellerId, userId);
        if (req.getStatus() != null) query.eq(AuctionDO::getStatus, req.getStatus());
        query.orderByDesc(AuctionDO::getId);
        PageResult<AuctionDO> page = auctionMapper.selectPage(req, query);
        fillMissingImages(page.getList());
        return new PageResult<>(page.getList().stream().map(this::toResp).toList(), page.getTotal());
    }

    @Override
    public int closeExpiredAuctions(int limit) {
        int count = 0;
        for (AuctionDO auction : auctionMapper.selectSyncIds(limit)) {
            try {
                self.closeAuction(auction.getId());
                count++;
            } catch (Exception ex) {
                log.error("[syncGoofishAuctions][闲鱼竞拍({})同步失败，将在下一轮重试]", auction.getId(), ex);
            }
        }
        return count;
    }

    @Override
    public int syncManagedAuctions(int limit) {
        int count = 0;
        // 同一应用时钟，避免数据库 UTC 时区偏差；1秒容差避免 Quartz 毫秒抖动跳过整轮。
        // 任务本身每10秒触发且禁止重入，不额外启动请求。
        for (AuctionDO auction : auctionMapper.selectManagedSyncIds(Math.max(1, Math.min(limit, 50)), LocalDateTime.now().plusSeconds(1))) {
            try {
                self.closeAuction(auction.getId());
                count++;
            } catch (Exception ex) {
                log.warn("[syncManagedAuctions][拍品 {} 查询异常，下轮重试]", auction.getId());
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 60)
    public void closeAuction(Long auctionId) {
        AuctionDO auction = auctionMapper.selectByIdForUpdate(auctionId);
        if (auction == null || !List.of(ACTIVE, PENDING_SALE_CONFIRMATION).contains(auction.getStatus())) return;
        syncFromGoofish(auction);
    }

    @Override public void syncAuction(Long auctionId) { self.closeAuction(auctionId); }

    private void syncFromGoofish(AuctionDO auction) {
        if (auction.getGoofishProductId() == null || auction.getGoofishProductId().isBlank()) {
            auctionMapper.updateById(new AuctionDO().setId(auction.getId()).setStatus(SYNC_EXCEPTION)
                    .setSyncError("缺少闲鱼商品编号").setLastSyncTime(LocalDateTime.now()));
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        AuctionDO update = new AuctionDO().setId(auction.getId()).setLastSyncTime(now);
        String detailMessage = "尚未关联闲管家product_id，无法读取商品详情；到期后人工确认";
        boolean remoteTerminal = false;
        if (auction.getGoofishManagedProductId() != null && !auction.getGoofishManagedProductId().isBlank()) {
            try {
                AuctionProductSnapshot snapshot = AuctionProductSnapshot.parse(auction.getGoofishManagedProductId(), auction.getGoofishProductId(),
                        yikoujiaService.requestGoofishProductDetail(auction.getGoofishManagedProductId()));
                update.setGoofishDetail(snapshot.json());
                if (!snapshot.images().isEmpty()) update.setDisplayPicUrls(snapshot.images());
                remoteTerminal = snapshot.needsConfirmation();
                detailMessage = "已同步基础信息和照片；接口价格非实时竞拍价，成交仍需人工确认";
            } catch (Exception ex) {
                detailMessage = "闲管家详情查询失败，已保留上次数据，请核对product_id、授权及网络后重试";
            }
        }
        if (auction.getEndTime() != null && !auction.getEndTime().isAfter(now)) {
            // 闲管家开放接口不提供闲鱼拍卖实例的实时出价数据。
            // 到期后一律进入人工确认，防止把已成交误判为流拍并恢复库存。
            update.setStatus(PENDING_SALE_CONFIRMATION)
                    .setSyncError(detailMessage + "；竞拍已到期，请在闲鱼核对后确认已售出或流拍");
            if (Objects.equals(auction.getDelistStatus(), DELIST_PENDING)) {
                update.setDelistStatus(DELIST_REJECTED).setDelistAuditTime(LocalDateTime.now())
                        .setDelistRejectReason("竞拍已自然结束，无需下架审核");
            }
        } else {
            update.setSyncError(detailMessage);
        }
        if (remoteTerminal) update.setStatus(PENDING_SALE_CONFIRMATION)
                .setSyncError("接口返回售出或下架时间信息，请人工核对拍卖结果；未自动结算");
        auctionMapper.update(update, new UpdateWrapper<AuctionDO>()
                .eq("id", auction.getId()));
    }

    static String resolveGoofishAuctionItemId(String source) {
        if (source == null || source.isBlank()) throw exception(AUCTION_GOOFISH_ITEM_ID_INVALID);
        String value = source.trim();
        if (RAW_GOOFISH_ITEM_ID.matcher(value).matches()) return value;
        Matcher matcher = GOOFISH_ITEM_ID_IN_LINK.matcher(value);
        if (matcher.find()) return matcher.group(1);
        throw exception(AUCTION_GOOFISH_ITEM_ID_INVALID);
    }

    private AuctionDO requireAuction(Long id) {
        AuctionDO auction = auctionMapper.selectById(id);
        if (auction == null) throw exception(AUCTION_NOT_EXISTS);
        return auction;
    }

    private void requirePendingDelist(AuctionDO auction) {
        if (auction == null) throw exception(AUCTION_NOT_EXISTS);
        if (auction.getStatus() != ACTIVE || !Objects.equals(auction.getDelistStatus(), DELIST_PENDING)) {
            throw exception(AUCTION_DELIST_REVIEW_INVALID);
        }
    }

    private void restoreCollection(AuctionDO auction) {
        CollectionDO collection = collectionMapper.selectByIdForUpdate(auction.getCollectionId());
        if (collection != null && Objects.equals(collection.getUserId(), 0L)) {
            collectionMapper.updateById(new CollectionDO().setId(collection.getId())
                    .setUserId(auction.getSellerId()).setTradeStatus(0));
        }
    }

    private AppAuctionRespVO toResp(AuctionDO auction) {
        AppAuctionRespVO resp = BeanUtils.toBean(auction, AppAuctionRespVO.class);
        List<String> images = resolveImages(auction);
        resp.setPicUrls(images);
        resp.setPicUrl(images.isEmpty() ? null : images.get(0));
        resp.setNextBidPrice(auction.getCurrentPrice());
        AuctionSettlementDO settlement = auctionSettlementMapper.selectByAuctionId(auction.getId());
        if (settlement != null) {
            resp.setSettlementStatus(settlement.getStatus());
            resp.setGrossAmount(settlement.getGrossAmount());
            resp.setFeeRate(settlement.getFeeRate());
            resp.setFeeAmount(settlement.getFeeAmount());
            resp.setSellerIncome(settlement.getSellerIncome());
        }
        return resp;
    }

    @Override
    public void updateDisplayPhotos(Long auctionId, List<String> photos) {
        requireAuction(auctionId);
        if (photos == null || photos.isEmpty() || photos.size() > 9
                || photos.stream().anyMatch(url -> url == null || url.length() > 2048
                || !url.matches("https?://[^\\s]+"))) {
            throw new IllegalArgumentException("请上传 1 至 9 张有效的拍卖展示照片");
        }
        auctionMapper.updateById(new AuctionDO().setId(auctionId)
                .setDisplayPicUrls(photos.stream().distinct().toList()));
    }

    @Override
    public void updateShareText(com.techtron.onebook.module.app.controller.admin.auction.vo.AdminAuctionShareReqVO req) {
        requireAuction(req.getId());
        if (!req.isShareValid()) throw new IllegalArgumentException("请填写闲鱼 App 原始分享内容");
        auctionMapper.updateById(new AuctionDO().setId(req.getId()).setShareText(req.getShareText().trim()));
    }

    private int requireFeeRate() {
        String value = configApi.getConfigValueByKey("auctionfee");
        try {
            int feeRate = Integer.parseInt(value == null ? "" : value.trim());
            if (feeRate < 0 || feeRate > 99) throw new NumberFormatException();
            return feeRate;
        } catch (NumberFormatException ex) {
            throw exception(AUCTION_FEE_NOT_CONFIGURED);
        }
    }

    /**
     * 兼容历史竞拍记录：旧逻辑可能只复制了 collection.pic_url，而藏品图片实际保存在 pic_urls。
     * 这里仅在响应前按关联藏品批量补图，不修改竞拍快照和数据库记录。
     */
    private void fillMissingImages(List<AuctionDO> auctions) {
        Set<Long> collectionIds = auctions.stream()
                .filter(auction -> resolveImages(auction).isEmpty())
                .map(AuctionDO::getCollectionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (collectionIds.isEmpty()) return;
        Map<Long, List<String>> pictureMap = collectionMapper.selectByIds(collectionIds).stream()
                .collect(Collectors.toMap(CollectionDO::getId, this::resolveImages, (first, ignored) -> first));
        auctions.forEach(auction -> {
            if (!resolveImages(auction).isEmpty()) return;
            List<String> images = pictureMap.getOrDefault(auction.getCollectionId(), Collections.emptyList());
            auction.setPicUrls(images);
            auction.setPicUrl(images.isEmpty() ? null : images.get(0));
        });
    }

    private int validPrice(Integer price, Integer fallback) {
        return price == null || price <= 0 ? fallback : price;
    }

    private List<String> resolveImages(CollectionDO collection) {
        if (collection.getPicUrls() != null && !collection.getPicUrls().isEmpty()) {
            return collection.getPicUrls().stream().filter(Objects::nonNull)
                    .filter(value -> !value.isBlank()).distinct().toList();
        }
        return collection.getPicUrl() == null || collection.getPicUrl().isBlank()
                ? Collections.emptyList() : List.of(collection.getPicUrl());
    }

    private List<String> resolveImages(AuctionDO auction) {
        if (auction.getPicUrls() != null && !auction.getPicUrls().isEmpty()) {
            List<String> images = auction.getPicUrls().stream().filter(Objects::nonNull)
                    .filter(value -> !value.isBlank()).distinct().toList();
            if (!images.isEmpty()) return images;
        }
        return auction.getPicUrl() == null || auction.getPicUrl().isBlank()
                ? Collections.emptyList() : List.of(auction.getPicUrl());
    }

    private String errorMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) message = ex.getClass().getSimpleName();
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    static boolean isAuctionEligible(CollectionDO collection, CollectionCategoryDO category) {
        boolean customCategory = category != null && category.getUserId() != null && category.getUserId() != 0L;
        return customCategory || containsSignature(collection.getName()) || containsSignature(collection.getCategoryName());
    }

    private static boolean containsSignature(String value) { return value != null && value.contains("签名"); }
}
