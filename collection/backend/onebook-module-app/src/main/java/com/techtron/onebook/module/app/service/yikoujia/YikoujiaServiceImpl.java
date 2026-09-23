package com.techtron.onebook.module.app.service.yikoujia;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.framework.mybatis.core.query.MPJLambdaWrapperX;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaPageReqVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaRespVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaSaveReqVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaImportReqVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaImportRespVO;
import com.techtron.onebook.module.app.controller.app.yikoujia.vo.AppYikoujiaSaveReqVO;
import com.techtron.onebook.module.app.convert.buyorder.yikoujia.YikoujiaConvert;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.dal.dataobject.ykjorder.YkjOrderDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.category.CollectionCategoryMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaItemMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import com.techtron.onebook.module.app.dal.mysql.ykjorder.YkjOrderMapper;
import com.techtron.onebook.module.app.enums.NotifySceneEnum;
import com.techtron.onebook.module.app.mq.producer.DealNotifyProducer;
import com.techtron.onebook.module.app.service.notify.AppSubscribeMessageService;
import com.techtron.onebook.module.app.service.collection.CollectionService;
import com.techtron.onebook.module.app.service.yikoujia.bo.CreateProductBO;
import com.techtron.onebook.module.app.service.yikoujia.bo.GoldResultBO;
import com.techtron.onebook.module.app.service.yikoujia.bo.GoldfishBO;
import com.techtron.onebook.module.app.service.yikoujia.bo.GoofishAuctionBO;
import com.techtron.onebook.module.infra.api.config.ConfigApi;
import com.techtron.onebook.module.infra.api.file.FileApi;
import com.techtron.onebook.module.pay.api.wallet.PayWalletApi;
import com.techtron.onebook.module.pay.api.order.PayOrderApi;
import com.techtron.onebook.module.pay.api.order.dto.PayOrderRespDTO;
import com.techtron.onebook.module.pay.enums.order.PayOrderStatusEnum;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import com.techtron.onebook.module.pay.enums.wallet.PayWalletBizTypeEnum;
import com.techtron.onebook.module.member.api.user.MemberUserApi;
import jakarta.annotation.Resource;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.techtron.onebook.framework.common.enums.UserTypeEnum.MEMBER;
import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.*;

/**
 * 一口价 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class YikoujiaServiceImpl implements YikoujiaService {

    // 公司内部系统使用：闲鱼开放接口凭证随私有源码保留。
    private static long apiKey = Long.parseLong(System.getenv().getOrDefault("GOOFISH_API_KEY", "0"));
    private static String apiKeySecret = System.getenv().getOrDefault("GOOFISH_API_SECRET", "");
    private static String domain = "https://open.goofish.pro";
    private static final String IMPORT_CATEGORY_NAME = "闲鱼导入";
    private static final Pattern RAW_PRODUCT_ID_PATTERN = Pattern.compile("^\\d{8,20}$");
    private static final Pattern LINK_PRODUCT_ID_PATTERN = Pattern.compile(
            "(?i)(?:product[_-]?id|item[_-]?id|id)[=/:](\\d{8,20})");
    private static final Pattern EMBEDDED_PRODUCT_ID_PATTERN = Pattern.compile("(?<!\\d)(\\d{12,20})(?!\\d)");
    private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int IMAGE_DOWNLOAD_MAX_BYTES = 20 * 1024 * 1024;
    private static final String GOOFISH_NORMALIZED_IMAGE_DIRECTORY = "goofish-normalized";
    private static final int STATUS_OFF_SHELF = 0;
    private static final int STATUS_MINIAPP_SOLD = 1;
    private static final int STATUS_WITHDRAWN = 2;
    private static final int STATUS_ON_SALE = 3;
    private static final int STATUS_GOOFISH_SOLD = 4;
    private static final int STATUS_CHANNEL_CONFLICT = 6;
    private static final long GOOFISH_CALLBACK_MAX_SKEW_SECONDS = 10 * 60L;
    private static final String SALE_CHANNEL_MINIAPP = "MINIAPP";
    private static final String SALE_CHANNEL_GOOFISH = "GOOFISH";
    private static final String SALE_CHANNEL_CONFLICT = "CONFLICT";

    @Resource
    private YikoujiaMapper yikoujiaMapper;

    @Resource
    private YkjOrderMapper ykjOrderMapper;

    @Resource
    private CollectionService collectionService;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private CollectionCategoryMapper collectionCategoryMapper;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private ConfigApi configApi;

    @Resource
    private FileApi fileApi;

    @Resource
    private PayWalletApi payWalletApi;

    @Resource
    private PayOrderApi payOrderApi;

    @Resource
    private DealNotifyProducer dealNotifyProducer;

    @Resource
    private AppSubscribeMessageService subscribeMessageService;

    @Resource
    private YikoujiaItemMapper yikoujiaItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createYikoujia(AppYikoujiaSaveReqVO createReqVO) {
        List<CollectionDO> list = collectionMapper.selectForUpdate(createReqVO.getCollectionIds(), createReqVO.getUserId());
        if (list == null || list.isEmpty() || list.size() != createReqVO.getCollectionIds().size()
                || list.stream().anyMatch(c -> c.getRealStock() == null || c.getRealStock() <= 0
                    || !Objects.equals(c.getTradeStatus(), 0) || !Objects.equals(c.getGetbackStatus(), 0))) {
            throw exception(SELL_NO_COLLECTION);
        }
        List<YikoujiaDO> ykjItemList = YikoujiaConvert.INSTANCE.convert(list);
//        YikoujiaDO yikoujiaDO = BeanUtils.toBean(createReqVO, YikoujiaDO.class);
//        yikoujiaMapper.insert(yikoujiaDO);
//        long id = yikoujiaDO.getId();

        ykjItemList.forEach(item -> item.setPrice(createReqVO.getPrice()));
        yikoujiaMapper.insertBatch(ykjItemList);
        List<CollectionDO> updateList = list.stream().map(collectionDO -> new CollectionDO()
                .setId(collectionDO.getId()).setStock(0).setTradeStatus(2)
        ).toList();
        collectionMapper.updateBatch(updateList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delistYikoujia(Long id, Long userId) {
        YikoujiaDO listing = yikoujiaMapper.selectOne(new LambdaQueryWrapper<YikoujiaDO>()
                .eq(YikoujiaDO::getId, id)
                .last("FOR UPDATE"));
        if (listing == null) {
            throw exception(YIKOUJIA_NOT_EXISTS);
        }
        if (!Objects.equals(listing.getUserId(), userId)) {
            throw exception(YIKOUJIA_DELIST_FORBIDDEN);
        }
        if (!Objects.equals(listing.getStatus(), 0) && !Objects.equals(listing.getStatus(), 3)) {
            throw exception(YIKOUJIA_DELIST_STATUS_INVALID);
        }

        List<YkjOrderDO> unpaidOrders = ykjOrderMapper.selectList(
                new LambdaQueryWrapper<YkjOrderDO>()
                        .eq(YkjOrderDO::getYkjId, id)
                        .eq(YkjOrderDO::getStatus, 0));
        if (unpaidOrders.stream().anyMatch(this::isPaymentStillPending)) {
            throw exception(YIKOUJIA_DELIST_ORDER_PENDING);
        }

        CollectionDO collection = null;
        if (listing.getCollectionId() != null) {
            List<CollectionDO> collections = collectionMapper.selectForUpdate0(
                    Collections.singletonList(listing.getCollectionId()), userId);
            if (collections == null || collections.isEmpty()) {
                throw exception(COLLECTION_NOT_EXISTS);
            }
            collection = collections.get(0);
            if (collection.getRealStock() == null || collection.getRealStock() <= 0
                    || !Objects.equals(collection.getGetbackStatus(), 0)) {
                throw exception(YIKOUJIA_DELIST_STATUS_INVALID);
            }
        }

        if (listing.getProductId() != null && !listing.getProductId().isBlank()) {
            downProduct(listing.getProductId());
        }
        yikoujiaMapper.updateById(new YikoujiaDO().setId(id).setStatus(2));
        if (collection != null) {
            collectionMapper.updateById(new CollectionDO()
                    .setId(collection.getId())
                    .setTradeStatus(0)
                    .setStock(collection.getRealStock()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delistCollectionsForGetback(List<Long> collectionIds, Long userId) {
        if (collectionIds == null || collectionIds.isEmpty()) {
            throw exception(GETBACK_COLLECTION_NOT_EXISTS);
        }
        List<Long> distinctIds = collectionIds.stream().filter(Objects::nonNull).distinct().sorted().toList();
        if (distinctIds.size() != collectionIds.size()) {
            throw exception(GETBACK_COLLECTION_NOT_EXISTS);
        }

        // 与单独下架保持“先挂售单、后藏品”的锁顺序，避免并发死锁。
        List<YikoujiaDO> listings = yikoujiaMapper.selectList(new LambdaQueryWrapper<YikoujiaDO>()
                .in(YikoujiaDO::getCollectionId, distinctIds)
                .eq(YikoujiaDO::getUserId, userId)
                .in(YikoujiaDO::getStatus, STATUS_OFF_SHELF, STATUS_ON_SALE)
                .orderByAsc(YikoujiaDO::getId)
                .last("FOR UPDATE"));
        List<CollectionDO> collections = collectionMapper.selectForUpdate0(distinctIds, userId);
        if (collections.size() != distinctIds.size()) {
            throw exception(GETBACK_COLLECTION_NOT_EXISTS);
        }

        Map<Long, YikoujiaDO> listingByCollectionId = listings.stream().collect(Collectors.toMap(
                YikoujiaDO::getCollectionId,
                item -> item,
                (first, ignored) -> first,
                LinkedHashMap::new
        ));
        for (CollectionDO collection : collections) {
            if (!Objects.equals(collection.getGetbackStatus(), 0)) {
                throw exception(GETBACK_EXIST);
            }
            if (!Objects.equals(collection.getTradeStatus(), 0)
                    && !Objects.equals(collection.getTradeStatus(), 2)) {
                throw exception(GETBACK_COLLECTION_STATUS_INVALID);
            }
            if (Objects.equals(collection.getTradeStatus(), 2)
                    && !listingByCollectionId.containsKey(collection.getId())) {
                // 本地标记为出售中却找不到可下架挂售单，不冒险恢复库存。
                throw exception(GETBACK_COLLECTION_STATUS_INVALID);
            }
            Integer availableStock = Objects.equals(collection.getTradeStatus(), 2)
                    ? collection.getRealStock() : collection.getStock();
            if (availableStock == null || availableStock <= 0) {
                // 所有本地状态必须在调用闲鱼下架前校验完成，避免外部已下架而本地取回失败。
                throw exception(GETBACK_COLLECTION_STATUS_INVALID);
            }
        }

        for (YikoujiaDO listing : listings) {
            if (Objects.equals(listing.getStatus(), STATUS_ON_SALE)) {
                List<YkjOrderDO> unpaidOrders = ykjOrderMapper.selectList(
                        new LambdaQueryWrapper<YkjOrderDO>()
                                .eq(YkjOrderDO::getYkjId, listing.getId())
                                .eq(YkjOrderDO::getStatus, 0));
                if (unpaidOrders.stream().anyMatch(this::isPaymentStillPending)) {
                    throw exception(YIKOUJIA_DELIST_ORDER_PENDING);
                }
                if (listing.getProductId() != null && !listing.getProductId().isBlank()) {
                    downProduct(listing.getProductId());
                }
            }
        }

        for (YikoujiaDO listing : listings) {
            yikoujiaMapper.updateById(new YikoujiaDO().setId(listing.getId()).setStatus(STATUS_WITHDRAWN));
        }
        List<CollectionDO> listedCollections = collections.stream()
                .filter(item -> Objects.equals(item.getTradeStatus(), 2))
                .map(item -> new CollectionDO()
                        .setId(item.getId())
                        .setTradeStatus(0)
                        .setStock(item.getRealStock()))
                .toList();
        if (!listedCollections.isEmpty()) {
            collectionMapper.updateBatch(listedCollections);
        }
    }

    /**
     * 只有支付平台已经明确关闭或退款的历史支付单，才不会阻止下架。
     * 未同步、待支付或支付成功的订单一律保守拦截，避免下架与付款回调并发造成错付。
     */
    private boolean isPaymentStillPending(YkjOrderDO order) {
        if (order.getPayOrderId() == null) {
            return true;
        }
        PayOrderRespDTO payOrder = payOrderApi.getOrder(order.getPayOrderId());
        return payOrder == null
                || (!PayOrderStatusEnum.isClosed(payOrder.getStatus())
                && !PayOrderStatusEnum.isRefund(payOrder.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createYikoujiaByAdmin(YikoujiaSaveReqVO createReqVO) {
        YikoujiaDO createObj = BeanUtils.toBean(createReqVO, YikoujiaDO.class);
        yikoujiaMapper.insert(createObj);
    }

    @Override
    public YikoujiaImportRespVO previewGoofishProduct(String source) {
        String productId = resolveGoofishProductId(source);
        if (!isAuthorizedGoofishProduct(productId)) {
            throw exception(GOOFISH_PRODUCT_NOT_AUTHORIZED);
        }
        return buildGoofishProductPreview(productId);
    }

    private YikoujiaImportRespVO buildGoofishProductPreview(String productId) {
        JsonNode data = requestGoofishProductDetail(productId);
        JsonNode shop = data.path("publish_shop").isArray() && !data.path("publish_shop").isEmpty()
                ? data.path("publish_shop").get(0) : null;

        List<String> images = new ArrayList<>();
        JsonNode imageNodes = shop == null ? null : shop.path("images");
        if (imageNodes != null && imageNodes.isArray()) {
            imageNodes.forEach(image -> {
                String normalized = normalizeGoofishImageUrl(image.asText());
                if (normalized != null && !normalized.isBlank()) {
                    images.add(normalized);
                }
            });
        }

        int productStatus = data.path("product_status").asInt(0);
        int localStatus = getStatus(String.valueOf(productStatus));
        if (localStatus < 0) {
            localStatus = 0;
        }
        String title = textOrNull(shop, "title");
        if (title == null || title.isBlank()) {
            title = textOrNull(data, "title");
        }

        String resolvedProductId = data.path("product_id").asText(productId);
        return new YikoujiaImportRespVO()
                .setProductId(resolvedProductId)
                .setTitle(title)
                .setPrice(data.path("price").asInt(0))
                .setStock(Math.max(data.path("stock").asInt(1), 1))
                .setContent(textOrNull(shop, "content"))
                .setImages(images.stream().distinct().toList())
                .setSellerName(textOrNull(shop, "user_name"))
                .setProductStatus(productStatus)
                .setLocalStatus(localStatus)
                .setAlreadyImported(yikoujiaMapper.selectCount(
                        new LambdaQueryWrapper<YikoujiaDO>().eq(YikoujiaDO::getProductId, resolvedProductId)) > 0);
    }

    @Override
    public PageResult<YikoujiaImportRespVO> getGoofishProductPage(Integer pageNo, Integer pageSize,
                                                                   Integer productStatus, String source) {
        if (source != null && !source.isBlank()) {
            String productId = resolveGoofishProductId(source);
            if (!isAuthorizedGoofishProduct(productId)) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
            return new PageResult<>(Collections.singletonList(buildGoofishProductPreview(productId)), 1L);
        }
        JSONObject body = new JSONObject();
        body.put("page_no", pageNo);
        body.put("page_size", pageSize);
        if (productStatus != null) {
            body.put("product_status", productStatus);
        }
        JsonNode data = requestGoofishApi("/api/open/product/list", body);
        List<YikoujiaImportRespVO> products = new ArrayList<>();
        JsonNode nodes = data.path("list");
        if (nodes.isArray()) {
            for (JsonNode node : nodes) {
                String productId = node.path("product_id").asText();
                if (productId.isBlank()) {
                    continue;
                }
                products.add(buildGoofishProductPreview(productId));
            }
        }
        return new PageResult<>(products, data.path("count").asLong(products.size()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importGoofishProduct(YikoujiaImportReqVO importReqVO) {
        boolean selfOperated = Boolean.TRUE.equals(importReqVO.getSelfOperated());
        if (!selfOperated && importReqVO.getUserId() == null) {
            throw exception(GOOFISH_MEMBER_REQUIRED);
        }
        if (!selfOperated) {
            memberUserApi.validateUser(importReqVO.getUserId());
        }
        YikoujiaImportRespVO product = previewGoofishProduct(importReqVO.getSource());
        if (product.isAlreadyImported()) {
            throw exception(GOOFISH_PRODUCT_ALREADY_IMPORTED);
        }

        if (selfOperated) {
            YikoujiaDO yikoujia = buildImportedYikoujia(product, null, null);
            yikoujiaMapper.insert(yikoujia);
            return yikoujia.getId();
        }

        CollectionCategoryDO category = collectionCategoryMapper.selectOne(
                new LambdaQueryWrapper<CollectionCategoryDO>()
                        .eq(CollectionCategoryDO::getUserId, importReqVO.getUserId())
                        .eq(CollectionCategoryDO::getName, IMPORT_CATEGORY_NAME)
                        .last("LIMIT 1"));
        if (category == null) {
            category = new CollectionCategoryDO()
                    .setUserId(importReqVO.getUserId())
                    .setName(IMPORT_CATEGORY_NAME)
                    .setPicUrl(product.getImages().isEmpty() ? null : product.getImages().get(0));
            collectionCategoryMapper.insert(category);
        }

        CollectionDO collection = new CollectionDO()
                .setName(product.getTitle())
                .setCategoryId(category.getId())
                .setCategoryName(category.getName())
                .setUserId(importReqVO.getUserId())
                .setPicUrl(product.getImages().isEmpty() ? null : product.getImages().get(0))
                .setPicUrls(product.getImages())
                .setStatus(1)
                .setStock(0)
                .setRealStock(product.getStock())
                .setTradeStatus(2)
                .setGetbackStatus(0)
                .setCreatorUserName(product.getSellerName());
        collectionMapper.insert(collection);

        YikoujiaDO yikoujia = buildImportedYikoujia(product, importReqVO.getUserId(), collection.getId());
        yikoujiaMapper.insert(yikoujia);
        return yikoujia.getId();
    }

    @Override
    public GoofishAuctionBO getGoofishAuction(String productId) {
        JsonNode data = requestGoofishProductDetail(productId);
        JsonNode bidData = data.path("bid_data");
        if (!bidData.isObject() || bidData.path("bid_end_time").asLong(0) <= 0) {
            throw exception(AUCTION_GOOFISH_NOT_AUCTION);
        }
        int currentPrice = bidData.path("current_bid_price").asInt(data.path("price").asInt(0));
        int bidCount = bidData.path("bid_count").asInt(0);
        long endMillis = bidData.path("bid_end_time").asLong(0);
        LocalDateTime remoteEndTime = endMillis <= 0 ? null
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(endMillis), CHINA_ZONE);
        return new GoofishAuctionBO(productId, resolveGoofishItemUrl(data, productId), currentPrice,
                bidCount, data.path("product_status").asInt(0), remoteEndTime);
    }

    @Override
    public GoofishAuctionBO getGoofishAuctionBySource(String source) {
        return getGoofishAuction(resolveGoofishProductId(source));
    }

    private String resolveGoofishItemUrl(JsonNode data, String productId) {
        JsonNode shop = data.path("publish_shop").isArray() && !data.path("publish_shop").isEmpty()
                ? data.path("publish_shop").get(0) : null;
        for (String field : List.of("item_url", "jump_url", "share_url", "url")) {
            String value = textOrNull(shop, field);
            if (value == null) value = textOrNull(data, field);
            if (value != null) return value;
        }
        for (String field : List.of("item_id", "idle_item_id", "out_item_id")) {
            String value = textOrNull(shop, field);
            if (value == null) value = textOrNull(data, field);
            if (value != null) return "https://www.goofish.com/item?id=" + value;
        }
        return "https://www.goofish.com/item?id=" + productId;
    }

    private YikoujiaDO buildImportedYikoujia(YikoujiaImportRespVO product, Long userId, Long collectionId) {
        return new YikoujiaDO()
                .setUserId(userId)
                .setCollectionId(collectionId)
                .setAmount(product.getStock())
                .setPrice(product.getPrice())
                .setStatus(product.getLocalStatus())
                .setPicUrl(product.getImages())
                .setName(product.getTitle())
                .setIntroduction(product.getContent())
                .setProductId(product.getProductId());
    }

    private boolean isAuthorizedGoofishProduct(String productId) {
        int pageNo = 1;
        final int pageSize = 50;
        while (pageNo <= 200) {
            JSONObject body = new JSONObject();
            body.put("page_no", pageNo);
            body.put("page_size", pageSize);
            JsonNode data = requestGoofishApi("/api/open/product/list", body);
            JsonNode nodes = data.path("list");
            if (!nodes.isArray() || nodes.isEmpty()) {
                return false;
            }
            for (JsonNode node : nodes) {
                if (productId.equals(node.path("product_id").asText())) {
                    return true;
                }
            }
            long count = data.path("count").asLong(0);
            if ((long) pageNo * pageSize >= count) {
                return false;
            }
            pageNo++;
        }
        return false;
    }

    @Override
    public JsonNode requestGoofishProductDetail(String productId) {
        JSONObject body = new JSONObject();
        body.put("product_id", Long.parseLong(productId));
        return requestGoofishApi("/api/open/product/detail", body);
    }

    private JsonNode requestGoofishApi(String path, JSONObject body) {
        long timestamp = System.currentTimeMillis() / 1000L;
        String jsonBody = body.toString();
        String sign = genSign(timestamp, jsonBody);
        String apiUrl = domain + path + "?appid=" + apiKey + "&timestamp=" + timestamp + "&sign=" + sign;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(20_000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) {
                connection.disconnect();
                throw exception(GOOFISH_PRODUCT_QUERY_ERROR);
            }
            JsonNode result;
            try (var inputStream = connection.getInputStream()) {
                result = new ObjectMapper().readTree(inputStream);
            } finally {
                connection.disconnect();
            }
            if (result.path("code").asInt(-1) != 0 || result.path("data").isMissingNode()
                    || result.path("data").isNull()) {
                throw exception(GOOFISH_PRODUCT_QUERY_ERROR);
            }
            return result.path("data");
        } catch (IOException | NumberFormatException e) {
            throw exception(GOOFISH_PRODUCT_QUERY_ERROR);
        }
    }

    private String resolveGoofishProductId(String source) {
        String value = source == null ? "" : source.trim();
        if (RAW_PRODUCT_ID_PATTERN.matcher(value).matches()) {
            return value;
        }
        Matcher linkMatcher = LINK_PRODUCT_ID_PATTERN.matcher(value);
        if (linkMatcher.find()) {
            return linkMatcher.group(1);
        }
        Matcher embeddedMatcher = EMBEDDED_PRODUCT_ID_PATTERN.matcher(value);
        if (embeddedMatcher.find()) {
            return embeddedMatcher.group(1);
        }
        throw exception(GOOFISH_PRODUCT_ID_INVALID);
    }

    private String normalizeGoofishImageUrl(String image) {
        if (image == null || image.isBlank()) {
            return null;
        }
        String value = image.trim();
        if (value.startsWith("https://") || value.startsWith("http://")) {
            return value;
        }
        if (value.startsWith("//")) {
            return "https:" + value;
        }
        return domain + "/" + (value.startsWith("/") ? value.substring(1) : value);
    }

    private List<String> normalizeGoofishImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        return images.stream()
                .filter(image -> image != null && !image.isBlank())
                .map(this::normalizeGoofishImage)
                .toList();
    }

    private String normalizeGoofishImage(String imageUrl) {
        if (imageUrl.contains("/" + GOOFISH_NORMALIZED_IMAGE_DIRECTORY + "/")) {
            return imageUrl;
        }
        try {
            byte[] source = downloadImage(imageUrl);
            Optional<byte[]> normalized = GoofishImageOrientationNormalizer.normalize(source);
            if (normalized.isEmpty()) {
                return imageUrl;
            }
            String name = UUID.randomUUID() + ".jpg";
            return fileApi.createFile(normalized.get(), name, GOOFISH_NORMALIZED_IMAGE_DIRECTORY, "image/jpeg");
        } catch (Exception ex) {
            // Do not publish a known-orientation image unchanged: Goofish may stretch it permanently.
            throw exception(CREATE_PRODUCT_ERROR);
        }
    }

    private byte[] downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        String protocol = url.getProtocol();
        if (!"https".equalsIgnoreCase(protocol) && !"http".equalsIgnoreCase(protocol)) {
            throw new IOException("Unsupported image URL protocol");
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(20_000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Accept", "image/*");
        try {
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("Image download failed with HTTP " + status);
            }
            int contentLength = connection.getContentLength();
            if (contentLength > IMAGE_DOWNLOAD_MAX_BYTES) {
                throw new IOException("Image is too large");
            }
            try (InputStream input = connection.getInputStream()) {
                byte[] content = input.readNBytes(IMAGE_DOWNLOAD_MAX_BYTES + 1);
                if (content.length > IMAGE_DOWNLOAD_MAX_BYTES) {
                    throw new IOException("Image is too large");
                }
                return content;
            }
        } finally {
            connection.disconnect();
        }
    }

    private String textOrNull(JsonNode node, String field) {
        if (node == null || node.path(field).isMissingNode() || node.path(field).isNull()) {
            return null;
        }
        String value = node.path(field).asText();
        return value.isBlank() ? null : value;
    }


//    @Override
//    public PageResult<YikoujiaRespVO> getYikoujiaPage(YikoujiaPageReqVO pageReqVO) {
//        PageResult<YikoujiaDO> result = yikoujiaMapper.selectPage(pageReqVO);
//        PageResult<YikoujiaRespVO> pageResult = BeanUtils.toBean(result, YikoujiaRespVO.class);
//        List<YikoujiaRespVO> list = pageResult.getList();
//        List<Long> ids = list.stream().map(YikoujiaRespVO::getId).toList();
//        MPJLambdaWrapper<YikoujiaItemDO> wrapper = new MPJLambdaWrapper<YikoujiaItemDO>()
//                .selectAll(YikoujiaItemDO.class)
//                .select(CollectionDO::getName)
//                .leftJoin(CollectionDO.class, CollectionDO::getId, YikoujiaItemDO::getCollectionId)
//                .in(YikoujiaItemDO::getYkjId, ids);
//        List<YikoujiaItemVO> yikoujiaItemDOS = yikoujiaItemMapper.selectJoinList(YikoujiaItemVO.class, wrapper);
//        Map<Long, List<YikoujiaItemVO>> itemMap = yikoujiaItemDOS.stream().collect(Collectors.groupingBy(YikoujiaItemVO::getYkjId));
//        list.forEach(item -> {
//            List<YikoujiaItemVO> yikoujiaItems = itemMap.get(item.getId());
//            item.setItems(yikoujiaItems);
//        });
//        return pageResult;
//    }

    @Override
    public YikoujiaDO getYikoujia(Long id) {
        YikoujiaDO yikoujia = yikoujiaMapper.selectById(id);
        fillMissingPicUrlsForDO(yikoujia == null ? Collections.emptyList() : Collections.singletonList(yikoujia));
        return yikoujia;
    }

    @Override
    public PageResult<YikoujiaDO> getYikoujiaPage(YikoujiaPageReqVO pageReqVO) {
        PageResult<YikoujiaDO> pageResult = yikoujiaMapper.selectPage(pageReqVO);
        fillMissingPicUrlsForDO(pageResult.getList());
        return pageResult;
    }

    @Override
    public PageResult<YikoujiaRespVO> getYikoujiaPage1(YikoujiaPageReqVO pageReqVO) {
        MPJLambdaWrapperX<YikoujiaDO> wrapper = new MPJLambdaWrapperX<YikoujiaDO>()
                .selectAll(YikoujiaDO.class)
                .selectAs(CollectionDO::getName, YikoujiaRespVO::getCollectionName)
                .leftJoin(CollectionDO.class, CollectionDO::getId, YikoujiaDO::getCollectionId)
                .eqIfPresent(YikoujiaDO::getStatus, pageReqVO.getStatus())
                .likeIfPresent(YikoujiaDO::getName, pageReqVO.getKeyword())
                .orderByDesc(YikoujiaDO::getId);
        if (Boolean.TRUE.equals(pageReqVO.getPendingSettlement())) {
            wrapper.eq(YikoujiaDO::getStatus, 4).isNotNull(YikoujiaDO::getCollectionId);
        }
        PageResult<YikoujiaRespVO> pageResult = yikoujiaMapper.selectJoinPage(pageReqVO, YikoujiaRespVO.class, wrapper);
        fillMissingPicUrlsForRespVO(pageResult.getList());
        return pageResult;
    }

    @Override
    public List<YikoujiaRespVO> getMyYikoujiaList(YikoujiaPageReqVO pageReqVO) {
        MPJLambdaWrapperX<YikoujiaDO> wrapper = new MPJLambdaWrapperX<YikoujiaDO>()
                .selectAll(YikoujiaDO.class)
                .selectAs(CollectionDO::getCategoryName, YikoujiaRespVO::getCategoryName)
                .leftJoin(CollectionDO.class, CollectionDO::getId, YikoujiaDO::getCollectionId)
                .eq(YikoujiaDO::getUserId, pageReqVO.getUserId())
                .orderByDesc(YikoujiaDO::getId);
        wrapper.in(YikoujiaDO::getStatus, STATUS_OFF_SHELF, STATUS_ON_SALE);
        List<YikoujiaRespVO> result = yikoujiaMapper.selectJoinList(YikoujiaRespVO.class, wrapper);
        fillMissingPicUrlsForRespVO(result);
        return result;
    }

    /**
     * 兼容历史一口价记录：旧逻辑只复制 CollectionDO.picUrl，
     * 当藏品照片实际保存在 picUrls 时，一口价 pic_url 会为空。
     * 此处仅在接口返回前从关联藏品补图，不写回数据库。
     */
    private void fillMissingPicUrlsForDO(List<YikoujiaDO> list) {
        Map<Long, List<String>> pictureMap = getCollectionPictureMap(
                list.stream()
                        .filter(item -> isPicUrlEmpty(item.getPicUrl()))
                        .map(YikoujiaDO::getCollectionId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        );
        list.forEach(item -> {
            if (isPicUrlEmpty(item.getPicUrl())) {
                item.setPicUrl(pictureMap.getOrDefault(item.getCollectionId(), Collections.emptyList()));
            }
        });
    }

    private void fillMissingPicUrlsForRespVO(List<YikoujiaRespVO> list) {
        Map<Long, List<String>> pictureMap = getCollectionPictureMap(
                list.stream()
                        .filter(item -> isPicUrlEmpty(item.getPicUrl()))
                        .map(YikoujiaRespVO::getCollectionId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        );
        list.forEach(item -> {
            if (isPicUrlEmpty(item.getPicUrl())) {
                item.setPicUrl(pictureMap.getOrDefault(item.getCollectionId(), Collections.emptyList()));
            }
        });
    }

    private Map<Long, List<String>> getCollectionPictureMap(Set<Long> collectionIds) {
        if (collectionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return collectionMapper.selectByIds(collectionIds).stream()
                .collect(Collectors.toMap(
                        CollectionDO::getId,
                        YikoujiaConvert.INSTANCE::resolvePicUrls,
                        (first, ignored) -> first
                ));
    }

    private boolean isPicUrlEmpty(List<String> picUrls) {
        return picUrls == null || picUrls.stream().noneMatch(url -> url != null && !url.isBlank());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImageOrder(Long id, List<String> original, List<String> reordered) {
        YikoujiaDO row = yikoujiaMapper.selectOne(new LambdaQueryWrapper<YikoujiaDO>()
                .eq(YikoujiaDO::getId, id).last("FOR UPDATE"));
        if (row == null) throw exception(YIKOUJIA_NOT_EXISTS);
        fillMissingPicUrlsForDO(Collections.singletonList(row));
        if (!Objects.equals(row.getPicUrl(), original)) {
            throw new com.techtron.onebook.framework.common.exception.ServiceException(409, "图片已发生变化，请刷新后重新排序");
        }
        if (!isImagePermutation(original, reordered)) {
            throw new com.techtron.onebook.framework.common.exception.ServiceException(400, "只能调整现有图片顺序");
        }
        // Update only the image list; never invoke listing, stock or settlement transitions.
        yikoujiaMapper.updateById(new YikoujiaDO().setId(id).setPicUrl(new ArrayList<>(reordered)));
    }

    static boolean isImagePermutation(List<String> original, List<String> reordered) {
        if (original == null || reordered == null || original.isEmpty()
                || original.size() != reordered.size()) return false;
        Map<String, Integer> counts = new HashMap<>();
        for (String url : original) counts.merge(url, 1, Integer::sum);
        for (String url : reordered) {
            Integer count = counts.get(url);
            if (count == null || count == 0) return false;
            counts.put(url, count - 1);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateYikoujia(YikoujiaSaveReqVO updateReqVO) {

        if (updateReqVO.getStatus() == 3) {
            if (updateReqVO.getProductId() == null || updateReqVO.getProductId().isEmpty()) {
                // 上架
                YikoujiaDO yikoujiaDO = yikoujiaMapper.selectOne(new LambdaQueryWrapper<YikoujiaDO>()
                        .eq(YikoujiaDO::getId,updateReqVO.getId())
                        .eq(YikoujiaDO::getStatus, 0)
                        .last("FOR UPDATE")
                );
                if (yikoujiaDO == null) {
                    throw exception(YIKOUJIA_NOT_EXISTS);
                }
                // 更新
                YikoujiaDO updateObj = BeanUtils.toBean(updateReqVO, YikoujiaDO.class);
                updateObj.setPrice(yikoujiaDO.getPrice());
                String productId = createProduct(updateObj);
                updateObj.setStatus(3);
                updateObj.setProductId(productId);
                yikoujiaMapper.updateById(updateObj);
                upProduct(productId);
                subscribeMessageService.notifyProductListed(yikoujiaDO.getUserId(), yikoujiaDO.getName(),
                        yikoujiaDO.getPrice(), LocalDateTime.now(CHINA_ZONE));
            } else {
                // 更新商品信息
                YikoujiaDO yikoujiaDO = yikoujiaMapper.selectOne(new LambdaQueryWrapper<YikoujiaDO>()
                        .eq(YikoujiaDO::getId,updateReqVO.getId())
                        .eq(YikoujiaDO::getStatus, 0)
                        .last("FOR UPDATE")
                );
                if (yikoujiaDO == null) {
                    throw exception(YIKOUJIA_NOT_EXISTS);
                }
                // 更新
                YikoujiaDO updateObj = BeanUtils.toBean(updateReqVO, YikoujiaDO.class);
                updateObj.setPrice(yikoujiaDO.getPrice());
                String productId = updateProduct(updateObj);
                updateObj.setStatus(3);
                updateObj.setProductId(productId);
                yikoujiaMapper.updateById(updateObj);
                upProduct(productId);
                subscribeMessageService.notifyProductListed(yikoujiaDO.getUserId(), yikoujiaDO.getName(),
                        yikoujiaDO.getPrice(), LocalDateTime.now(CHINA_ZONE));
            }
        } else if (updateReqVO.getStatus() == 1) {
            // 校验是否为3未打款
            YikoujiaDO yikoujiaDO = validateYikoujiaExists(updateReqVO.getId(), 4);
            // 更新
            YikoujiaDO updateObj = BeanUtils.toBean(updateReqVO, YikoujiaDO.class);
            yikoujiaMapper.updateById(updateObj);
            if (yikoujiaDO.getCollectionId() != null) {
                String ykjFee = configApi.getConfigValueByKey("ykjfee");
                float rate = (10000 - Float.parseFloat(ykjFee) * 100) /10000;
                int price = (int)(yikoujiaDO.getPrice()*rate);
                PayWalletAddBalanceReqDTO payWalletAddBalanceReqDTO = new PayWalletAddBalanceReqDTO()
                        .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.INCOME.getType())
                        .setUserId(yikoujiaDO.getUserId()).setBizId(updateReqVO.getId().toString())
                        .setPrice(price);

                payWalletApi.addWalletBalance(payWalletAddBalanceReqDTO);
                List<Long> ids = Collections.singletonList(yikoujiaDO.getCollectionId());
                List<CollectionDO> list = collectionMapper.selectForUpdate0(ids, yikoujiaDO.getUserId())
                        .stream().map(item -> new CollectionDO()
                                .setId(item.getId())
                                .setTradeStatus(0)
                                .setRealStock(0)
                        ).toList();
                collectionMapper.updateBatch(list);
                // 发消息
                dealNotifyProducer.sendNotifySendMessage(NotifySceneEnum.YKJ_NOTIFY.getTemplateCode(), yikoujiaDO.getUserId(), yikoujiaDO.getName(), price);
                subscribeMessageService.notifyProductSold(yikoujiaDO.getUserId(), yikoujiaDO.getName(),
                        price, LocalDateTime.now(CHINA_ZONE));
            }
        } else if(updateReqVO.getStatus() == 2) {// 驳回
            // 校验是否为0
            YikoujiaDO yikoujiaDO = validateYikoujiaExists(updateReqVO.getId(), 0);
            // 更新
            YikoujiaDO updateObj = BeanUtils.toBean(updateReqVO, YikoujiaDO.class);
            updateObj.setProductId("");
            yikoujiaMapper.updateById(updateObj);
            if (yikoujiaDO.getCollectionId() != null) {
                List<Long> ids = Collections.singletonList(yikoujiaDO.getCollectionId());
                List<CollectionDO> list = collectionMapper.selectForUpdate0(ids, yikoujiaDO.getUserId())
                        .stream().map(item -> new CollectionDO()
                                .setId(item.getId())
                                .setTradeStatus(0)
                                .setStock(item.getRealStock())
                        ).toList();
                collectionMapper.updateBatch(list);
            }
        } else if(updateReqVO.getStatus() == 0) {//下架
            YikoujiaDO yikoujiaDO = validateYikoujiaExists(updateReqVO.getId(), 3);
            // 更新
            YikoujiaDO updateObj = BeanUtils.toBean(updateReqVO, YikoujiaDO.class);
            yikoujiaMapper.updateById(updateObj);
            downProduct(yikoujiaDO.getProductId());
        }
    }

    private YikoujiaDO validateYikoujiaExists(Long id, Integer status) {
        YikoujiaDO yikoujiaDO = yikoujiaMapper.selectOne(new LambdaQueryWrapper<YikoujiaDO>()
                .eq(YikoujiaDO::getId,id)
                .eq(YikoujiaDO::getStatus,status)
        );
        if (yikoujiaDO == null) {
            throw exception(YIKOUJIA_NOT_EXISTS);
        }
        return yikoujiaDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeUpdate() {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis() / 1000L;

        // 请求体JSON字符串
        JSONObject data = new JSONObject();
        data.put("update_time", Arrays.asList(timestamp - 60 * 2, timestamp));

        String jsonBody = data.toString();

        // 生成签名
        String sign = genSign(timestamp, jsonBody);

        // 拼接请求地址
        String apiUrl = domain + "/api/open/product/list?appid=" + apiKey + "&timestamp=" + timestamp + "&sign="
                + sign;

        try {
            // 创建URL对象
            URL url = new URL(apiUrl);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为POST
            connection.setRequestMethod("POST");

            // 设置请求头部
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // 启用输出流
            connection.setDoOutput(true);

            // 获取输出流并写入请求体
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            // 获取响应状态码
            int responseCode = connection.getResponseCode();
            System.out.println("API Response Code: " + responseCode);
            if (responseCode < 200 || responseCode >= 300) {
                connection.disconnect();
                throw exception(GOLD_FISH_ERROR);
            }

            // 读取响应内容
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();

            // 关闭连接
            connection.disconnect();


            ObjectMapper mapper = new ObjectMapper();
            GoldResultBO result = mapper.readValue(response.toString(), GoldResultBO.class);
            List<GoldfishBO> list = result.getData().getList();
            if (list.isEmpty()) {
                return 0;
            } else {
                return updateProduct(list);
            }

        } catch (IOException e) {
            System.out.println("API Error: " + e.toString());
            throw exception(GOLD_FISH_ERROR);
        }
    }

    private int updateProduct(List<GoldfishBO> list) {
        List<String> ids = list.stream().map(GoldfishBO::getProduct_id).toList();
        // 把list转换为key为id的map
        Map<String, GoldfishBO> map = list.stream().collect(Collectors.toMap(GoldfishBO::getProduct_id, item -> item));
        List<YikoujiaDO> yikoujiaDOS = yikoujiaMapper.selectList(new LambdaQueryWrapper<YikoujiaDO>()
                .in(YikoujiaDO::getProductId, ids)
                .in(YikoujiaDO::getStatus, Arrays.asList(
                        STATUS_OFF_SHELF, STATUS_MINIAPP_SOLD, STATUS_ON_SALE,
                        STATUS_GOOFISH_SOLD, STATUS_CHANNEL_CONFLICT))
        );
        if (yikoujiaDOS.isEmpty()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now(CHINA_ZONE);
        for (YikoujiaDO listing : yikoujiaDOS) {
            GoldfishBO product = map.get(listing.getProductId());
            if (product == null) {
                continue;
            }
            int goofishStatus = parseInteger(product.getProduct_status(), -1);
            int remoteStatus = getStatus(product.getProduct_status());
            Integer remotePrice = parseInteger(product.getPrice(), null);
            Integer remoteStock = parseInteger(product.getStock(), null);
            if (isGoofishSold(remoteStatus, remoteStock)) {
                syncGoofishSold(listing, goofishStatus, remotePrice, now);
            } else if (remoteStatus == STATUS_ON_SALE) {
                syncGoofishOnSale(listing, goofishStatus, remotePrice, now);
            } else if (remoteStatus == STATUS_OFF_SHELF) {
                syncGoofishOffShelf(listing, goofishStatus, remotePrice, now);
            } else {
                updateGoofishSnapshot(listing.getId(), goofishStatus, remotePrice, now,
                        "已记录闲鱼未识别状态，未改变本地售卖状态",
                        Objects.equals(listing.getStatus(), STATUS_ON_SALE));
            }
        }
        return yikoujiaDOS.size();
    }

    @Override
    public boolean verifyGoofishCallback(String appId, long timestamp, String sign, String rawBody) {
        if (!String.valueOf(apiKey).equals(appId) || sign == null || sign.isBlank()
                || rawBody == null || timestamp <= 0) {
            return false;
        }
        long now = System.currentTimeMillis() / 1000L;
        if (Math.abs(now - timestamp) > GOOFISH_CALLBACK_MAX_SKEW_SECONDS) {
            return false;
        }
        String expected = generateGoofishSign(apiKey, apiKeySecret, timestamp, rawBody);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                sign.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncGoofishProduct(String productId) {
        if (productId == null || !RAW_PRODUCT_ID_PATTERN.matcher(productId).matches()) {
            return;
        }
        JsonNode data = requestGoofishProductDetail(productId);
        syncGoofishProductSnapshot(productId, data.path("product_status").asInt(-1),
                data.path("price").isNumber() ? data.path("price").asInt() : null,
                resolveGoofishStock(data));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncGoofishOrder(String orderNo, String productId) {
        String resolvedProductId = productId;
        if (resolvedProductId == null || !RAW_PRODUCT_ID_PATTERN.matcher(resolvedProductId).matches()) {
            if (orderNo == null || orderNo.isBlank()) {
                return;
            }
            JSONObject body = new JSONObject();
            body.put("order_no", orderNo);
            JsonNode order = requestGoofishApi("/api/open/order/detail", body);
            resolvedProductId = order.path("goods").path("product_id").asText();
        }
        syncGoofishProduct(resolvedProductId);
    }

    private void syncGoofishProductSnapshot(String productId, int goofishStatus, Integer remotePrice,
                                             Integer remoteStock) {
        List<YikoujiaDO> listings = yikoujiaMapper.selectList(new LambdaQueryWrapper<YikoujiaDO>()
                .eq(YikoujiaDO::getProductId, productId)
                .in(YikoujiaDO::getStatus, STATUS_OFF_SHELF, STATUS_MINIAPP_SOLD, STATUS_ON_SALE,
                        STATUS_GOOFISH_SOLD, STATUS_CHANNEL_CONFLICT));
        int remoteStatus = getStatus(String.valueOf(goofishStatus));
        LocalDateTime now = LocalDateTime.now(CHINA_ZONE);
        for (YikoujiaDO listing : listings) {
            if (isGoofishSold(remoteStatus, remoteStock)) {
                syncGoofishSold(listing, goofishStatus, remotePrice, now);
            } else if (remoteStatus == STATUS_ON_SALE) {
                syncGoofishOnSale(listing, goofishStatus, remotePrice, now);
            } else if (remoteStatus == STATUS_OFF_SHELF) {
                syncGoofishOffShelf(listing, goofishStatus, remotePrice, now);
            } else {
                updateGoofishSnapshot(listing.getId(), goofishStatus, remotePrice, now,
                        "回调后查询到闲鱼未识别状态，未改变本地售卖状态",
                        Objects.equals(listing.getStatus(), STATUS_ON_SALE));
            }
        }
    }

    private Integer resolveGoofishStock(JsonNode data) {
        if (data.path("stock").isNumber()) {
            return data.path("stock").asInt();
        }
        JsonNode skuItems = data.path("sku_items");
        if (!skuItems.isArray() || skuItems.isEmpty()) {
            return null;
        }
        int total = 0;
        boolean found = false;
        for (JsonNode sku : skuItems) {
            if (sku.path("stock").isNumber()) {
                total += Math.max(sku.path("stock").asInt(), 0);
                found = true;
            }
        }
        return found ? total : null;
    }

    static boolean isGoofishSold(int remoteStatus, Integer remoteStock) {
        return remoteStatus == STATUS_GOOFISH_SOLD || (remoteStock != null && remoteStock <= 0);
    }

    /**
     * 闲鱼先返回已售时，原子地将小程序商品从在售改为闲鱼成交。
     * 如果本地已经由小程序成交，不重复结算，而是进入冲突待人工核对。
     */
    private void syncGoofishSold(YikoujiaDO listing, int goofishStatus, Integer remotePrice,
                                 LocalDateTime now) {
        if (Objects.equals(listing.getStatus(), STATUS_ON_SALE)) {
            LambdaUpdateWrapper<YikoujiaDO> update = snapshotUpdate(listing.getId(), goofishStatus,
                    remotePrice, now, true)
                    .set(YikoujiaDO::getStatus, STATUS_GOOFISH_SOLD)
                    .set(YikoujiaDO::getSaleChannel, SALE_CHANNEL_GOOFISH)
                    .set(YikoujiaDO::getSoldAt, now)
                    .set(YikoujiaDO::getSyncRemark, "闲鱼已售出，小程序已同步下架")
                    .eq(YikoujiaDO::getStatus, STATUS_ON_SALE);
            yikoujiaMapper.update(null, update);
            return;
        }
        if (Objects.equals(listing.getStatus(), STATUS_MINIAPP_SOLD)
                && SALE_CHANNEL_MINIAPP.equals(listing.getSaleChannel())) {
            LambdaUpdateWrapper<YikoujiaDO> update = snapshotUpdate(listing.getId(), goofishStatus,
                    remotePrice, now, false)
                    .set(YikoujiaDO::getStatus, STATUS_CHANNEL_CONFLICT)
                    .set(YikoujiaDO::getSaleChannel, SALE_CHANNEL_CONFLICT)
                    .set(YikoujiaDO::getSyncRemark,
                            "小程序和闲鱼均返回成交，已禁止自动重复结算，请人工核对")
                    .eq(YikoujiaDO::getStatus, STATUS_MINIAPP_SOLD)
                    .eq(YikoujiaDO::getSaleChannel, SALE_CHANNEL_MINIAPP);
            yikoujiaMapper.update(null, update);
            return;
        }
        updateGoofishSnapshot(listing.getId(), goofishStatus, remotePrice, now,
                Objects.equals(listing.getStatus(), STATUS_CHANNEL_CONFLICT)
                        ? listing.getSyncRemark() : "闲鱼已售出", false);
    }

    private void syncGoofishOnSale(YikoujiaDO listing, int goofishStatus, Integer remotePrice,
                                   LocalDateTime now) {
        if (Objects.equals(listing.getStatus(), STATUS_MINIAPP_SOLD)
                && SALE_CHANNEL_MINIAPP.equals(listing.getSaleChannel())) {
            // 小程序已成交但闲鱼仍显示在售时进行补偿下架，不允许恢复本地在售。
            downProduct(listing.getProductId());
            updateGoofishSnapshot(listing.getId(), goofishStatus, remotePrice, now,
                    "小程序已成交，已再次通知闲鱼下架", false);
            return;
        }
        updateGoofishSnapshot(listing.getId(), goofishStatus, remotePrice, now,
                Objects.equals(listing.getStatus(), STATUS_ON_SALE) ? "小程序与闲鱼双端在售" : listing.getSyncRemark(),
                Objects.equals(listing.getStatus(), STATUS_ON_SALE));
    }

    private void syncGoofishOffShelf(YikoujiaDO listing, int goofishStatus, Integer remotePrice,
                                     LocalDateTime now) {
        if (!Objects.equals(listing.getStatus(), STATUS_ON_SALE)) {
            updateGoofishSnapshot(listing.getId(), goofishStatus, remotePrice, now,
                    listing.getSyncRemark(), false);
            return;
        }
        int updated = yikoujiaMapper.update(null, snapshotUpdate(listing.getId(), goofishStatus,
                        remotePrice, now, false)
                .set(YikoujiaDO::getStatus, STATUS_OFF_SHELF)
                .set(YikoujiaDO::getSaleChannel, null)
                .set(YikoujiaDO::getSoldAt, null)
                .set(YikoujiaDO::getSyncRemark, "闲鱼已下架，小程序已同步下架")
                .eq(YikoujiaDO::getStatus, STATUS_ON_SALE));
        if (updated > 0 && listing.getCollectionId() != null) {
            CollectionDO collection = collectionMapper.selectByIdForUpdate(listing.getCollectionId());
            if (collection != null && Objects.equals(collection.getUserId(), listing.getUserId())) {
                collectionMapper.updateById(new CollectionDO()
                        .setId(collection.getId())
                        .setTradeStatus(0)
                        .setStock(collection.getRealStock()));
            }
        }
    }

    private void updateGoofishSnapshot(Long id, int goofishStatus, Integer remotePrice,
                                       LocalDateTime now, String remark, boolean updatePrice) {
        LambdaUpdateWrapper<YikoujiaDO> update = snapshotUpdate(id, goofishStatus, remotePrice, now,
                updatePrice);
        if (remark != null) {
            update.set(YikoujiaDO::getSyncRemark, remark);
        }
        yikoujiaMapper.update(null, update);
    }

    private LambdaUpdateWrapper<YikoujiaDO> snapshotUpdate(Long id, int goofishStatus,
                                                           Integer remotePrice, LocalDateTime now,
                                                           boolean updatePrice) {
        LambdaUpdateWrapper<YikoujiaDO> update = new LambdaUpdateWrapper<YikoujiaDO>()
                .eq(YikoujiaDO::getId, id)
                .set(YikoujiaDO::getGoofishStatus, goofishStatus)
                .set(YikoujiaDO::getLastSyncTime, now);
        if (updatePrice && remotePrice != null && remotePrice >= 0) {
            update.set(YikoujiaDO::getPrice, remotePrice);
        }
        return update;
    }

    private Integer parseInteger(String value, Integer defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private int getStatus(String productStatus) {
        return switch (productStatus) {
            case "23", "33" -> 4;
            case "22" -> 3;
            case "31", "36", "-1" -> 0;
            default -> -1;
        };
    }

    // md5加密
    private static String genMd5(String str) {
        StringBuilder result = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(str.getBytes(StandardCharsets.UTF_8));
            for (byte b : digest) {
                result.append(String.format("%02x", b & 0xff));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }

    // 生成签名
    private String genSign(long timestamp, String jsonStr) {
        return generateGoofishSign(apiKey, apiKeySecret, timestamp, jsonStr);
    }

    static String generateGoofishSign(long appKey, String appSecret, long timestamp, String jsonStr) {
        String data = appKey + "," + genMd5(jsonStr) + "," + timestamp + "," + appSecret;

        // 商务对接模式 拼接字符串
        // String data = apiKey + "," + genMd5(jsonStr) + "," + timestamp + "," + seller_id + "," + apiKeySecret;

        // 生成签名
        return genMd5(data);
    }

    public void test() {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis() / 1000L;

        // 请求体JSON字符串
        JSONObject data = new JSONObject();

        String jsonBody = data.toString();

        // 生成签名
        String sign = genSign(timestamp, jsonBody);

        // 拼接请求地址
        String apiUrl = domain + "/api/open/product/list?appid=" + apiKey + "&timestamp=" + timestamp + "&sign="
                + sign;

        try {
            // 创建URL对象
            URL url = new URL(apiUrl);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为POST
            connection.setRequestMethod("POST");

            // 设置请求头部
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // 启用输出流
            connection.setDoOutput(true);

            // 获取输出流并写入请求体
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            // 获取响应状态码
            int responseCode = connection.getResponseCode();
            System.out.println("API Response Code: " + responseCode);

            // 读取响应内容
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();

            // 关闭连接
            connection.disconnect();


            System.out.println(response.toString());


        } catch (IOException e) {
            System.out.println("API Error: " + e.toString());
            throw exception(GOLD_FISH_ERROR);
        }
    }

    private String createProduct(YikoujiaDO yikoujiaDO) {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis() / 1000L;

        // 请求体JSON字符串
        JSONObject data = new JSONObject();
        data.put("item_biz_type", 2);
        data.put("sp_biz_type", 28);
        data.put("channel_cat_id", "59f5b7a94c42a8181921b9e373cead75");
        data.put("price", yikoujiaDO.getPrice());
        data.put("stock", 1);
        data.put("express_fee", 0);
        JSONObject shop = new JSONObject();
        shop.put("user_name", "青竹使者二");
        shop.put("province", 330000);
        shop.put("city", 330100);
        shop.put("district", 330114);
        shop.put("title", yikoujiaDO.getName());
        shop.put("content", yikoujiaDO.getIntroduction());
        shop.put("images", normalizeGoofishImages(yikoujiaDO.getPicUrl()));
        data.put("publish_shop", List.of(shop));

        String jsonBody = data.toString();

        // 生成签名
        String sign = genSign(timestamp, jsonBody);

        // 拼接请求地址
        String apiUrl = domain + "/api/open/product/create?appid=" + apiKey + "&timestamp=" + timestamp + "&sign="
                + sign;

        try {
            // 创建URL对象
            URL url = new URL(apiUrl);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为POST
            connection.setRequestMethod("POST");

            // 设置请求头部
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // 启用输出流
            connection.setDoOutput(true);

            // 获取输出流并写入请求体
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            // 获取响应状态码
            int responseCode = connection.getResponseCode();
            System.out.println("API Response Code: " + responseCode);

            // 读取响应内容
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();

            // 关闭连接
            connection.disconnect();

            System.out.println(response.toString());

            ObjectMapper mapper = new ObjectMapper();
            CreateProductBO result = mapper.readValue(response.toString(), CreateProductBO.class);
            System.out.println(result.getData().getProduct_id());
            return result.getData().getProduct_id();

        } catch (IOException e) {
            System.out.println("API Error: " + e.toString());
            throw exception(CREATE_PRODUCT_ERROR);
        }
    }

    private void upProduct(String productId) {
        // Editing a live listing does not require publishing it again.
        if (requestGoofishProductDetail(productId).path("product_status").asInt(-1) == 22) {
            return;
        }
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis() / 1000L;

        // 请求体JSON字符串
        JSONObject data = new JSONObject();
        data.put("product_id", Long.parseLong(productId));
        data.put("user_name", List.of("青竹使者二"));

        String jsonBody = data.toString();

        // 生成签名
        String sign = genSign(timestamp, jsonBody);

        // 拼接请求地址
        String apiUrl = domain + "/api/open/product/publish?appid=" + apiKey + "&timestamp=" + timestamp + "&sign="
                + sign;

        try {
            // 创建URL对象
            URL url = new URL(apiUrl);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为POST
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(20_000);

            // 设置请求头部
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // 启用输出流
            connection.setDoOutput(true);

            // 获取输出流并写入请求体
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            // 获取响应状态码
            int responseCode = connection.getResponseCode();
            System.out.println("API Response Code: " + responseCode);

            // 读取响应内容
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();

            // 关闭连接
            connection.disconnect();

            System.out.println(response.toString());
            JsonNode responseJson = new ObjectMapper().readTree(response.toString());
            if (responseJson.path("code").asInt(-1) != 0) {
                // Another publisher may have listed it after our first read. Verify the
                // actual state; never treat every 100001 business error as success.
                if (responseJson.path("code").asInt(-1) == 100001
                        && requestGoofishProductDetail(productId).path("product_status").asInt(-1) == 22) {
                    return;
                }
                throw exception(UP_PRODUCT_ERROR);
            }

        } catch (IOException e) {
            System.out.println("API Error: " + e.toString());
            throw exception(UP_PRODUCT_ERROR);
        }
    }

    private String updateProduct(YikoujiaDO yikoujiaDO) {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis() / 1000L;

        // 请求体JSON字符串
        JSONObject data = new JSONObject();
        data.put("product_id", Long.parseLong(yikoujiaDO.getProductId()));
        data.put("item_biz_type", 2);
        data.put("sp_biz_type", 28);
        data.put("channel_cat_id", "59f5b7a94c42a8181921b9e373cead75");
        data.put("price", yikoujiaDO.getPrice());
        data.put("stock", 1);
        data.put("express_fee", 0);
        JSONObject shop = new JSONObject();
        shop.put("user_name", "青竹使者二");
        shop.put("province", 330000);
        shop.put("city", 330100);
        shop.put("district", 330114);
        shop.put("title", yikoujiaDO.getName());
        shop.put("content", yikoujiaDO.getIntroduction());
        shop.put("images", normalizeGoofishImages(yikoujiaDO.getPicUrl()));
        data.put("publish_shop", List.of(shop));

        String jsonBody = data.toString();

        // 生成签名
        String sign = genSign(timestamp, jsonBody);

        // 拼接请求地址
        String apiUrl = domain + "/api/open/product/edit?appid=" + apiKey + "&timestamp=" + timestamp + "&sign="
                + sign;

        try {
            // 创建URL对象
            URL url = new URL(apiUrl);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为POST
            connection.setRequestMethod("POST");

            // 设置请求头部
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // 启用输出流
            connection.setDoOutput(true);

            // 获取输出流并写入请求体
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            // 获取响应状态码
            int responseCode = connection.getResponseCode();
            System.out.println("API Response Code: " + responseCode);

            // 读取响应内容
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();

            // 关闭连接
            connection.disconnect();

            System.out.println(response.toString());

            ObjectMapper mapper = new ObjectMapper();
            CreateProductBO result = mapper.readValue(response.toString(), CreateProductBO.class);
            System.out.println(result.getData().getProduct_id());
            return result.getData().getProduct_id();

        } catch (IOException e) {
            System.out.println("API Error: " + e.toString());
            throw exception(CREATE_PRODUCT_ERROR);
        }
    }

    @Override
    public void downProduct(String productId) {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis() / 1000L;

        // 请求体JSON字符串
        JSONObject data = new JSONObject();
        data.put("product_id", Long.parseLong(productId));

        String jsonBody = data.toString();

        // 生成签名
        String sign = genSign(timestamp, jsonBody);

        // 拼接请求地址
        String apiUrl = domain + "/api/open/product/downShelf?appid=" + apiKey + "&timestamp=" + timestamp + "&sign="
                + sign;

        try {
            // 创建URL对象
            URL url = new URL(apiUrl);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为POST
            connection.setRequestMethod("POST");

            // 设置请求头部
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // 启用输出流
            connection.setDoOutput(true);

            // 获取输出流并写入请求体
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            // 获取响应状态码
            int responseCode = connection.getResponseCode();
            System.out.println("API Response Code: " + responseCode);
            if (responseCode < 200 || responseCode >= 300) {
                connection.disconnect();
                throw exception(UP_PRODUCT_ERROR);
            }

            // 读取响应内容
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();

            // 关闭连接
            connection.disconnect();

            System.out.println(response.toString());
            JsonNode responseJson = new ObjectMapper().readTree(response.toString());
            if (responseJson.has("code") && responseJson.path("code").asInt(-1) != 0) {
                throw exception(UP_PRODUCT_ERROR);
            }

        } catch (IOException e) {
            System.out.println("API Error: " + e.toString());
            throw exception(UP_PRODUCT_ERROR);
        }
    }

}
