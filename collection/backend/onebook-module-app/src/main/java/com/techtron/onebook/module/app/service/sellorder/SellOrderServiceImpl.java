package com.techtron.onebook.module.app.service.sellorder;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderCancelVO;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderRespVO;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderSaveReqVO;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.UserTradeInfoRespVO;
import com.techtron.onebook.module.app.dal.dataobject.buyorder.BuyOrderDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionRecordDO;
import com.techtron.onebook.module.app.dal.dataobject.dealorder.DealOrderDO;
import com.techtron.onebook.module.app.dal.dataobject.energystone.EnergyStoneDO;
import com.techtron.onebook.module.app.dal.dataobject.sellorder.SellOrderDO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.dal.mysql.buyorder.BuyOrderMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionRecordMapper;
import com.techtron.onebook.module.app.dal.mysql.dealorder.DealOrderMapper;
import com.techtron.onebook.module.app.dal.mysql.energystone.EnergyStoneMapper;
import com.techtron.onebook.module.app.dal.mysql.sellorder.SellOrderMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import com.techtron.onebook.module.app.enums.NotifySceneEnum;
import com.techtron.onebook.module.app.mq.producer.NotifyProducer;
import com.techtron.onebook.module.app.service.collection.CollectionService;
import com.techtron.onebook.module.infra.api.config.ConfigApi;
import com.techtron.onebook.module.pay.api.wallet.PayWalletApi;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import com.techtron.onebook.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionSummaryRespVO;
import com.techtron.onebook.module.pay.enums.wallet.PayWalletBizTypeEnum;
import com.techtron.onebook.module.pay.service.wallet.PayWalletTransactionService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.techtron.onebook.framework.common.enums.UserTypeEnum.MEMBER;
import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.*;

/**
 * 批量交易卖单 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class SellOrderServiceImpl implements SellOrderService {

    @Resource
    private SellOrderMapper sellOrderMapper;

    @Resource
    private BuyOrderMapper buyOrderMapper;

    @Resource
    private CollectionService collectionService;
    @Autowired
    private PayWalletApi payWalletApi;
    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private CollectionRecordMapper collectionRecordMapper;
    @Autowired
    private PayWalletTransactionService payWalletTransactionService;

    @Resource
    private NotifyProducer notifyProducer;
    @Resource
    private DealOrderMapper dealOrderMapper;
    @Resource
    private EnergyStoneMapper energyStoneMapper;

    @Resource
    private ConfigApi configApi;
    @Autowired
    private YikoujiaMapper yikoujiaMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSellOrder(SellOrderSaveReqVO createReqVO) {

        List<CollectionDO> list = collectionMapper.selectForUpdate(createReqVO.getCollectionIds(), createReqVO.getUserId());
        if (list == null || list.isEmpty()) {
            throw exception(SELL_NO_COLLECTION);
        }
        // 计算出售藏品总数量
        Integer totalStock = list.stream().reduce(0, (sum, c) -> sum + (c.getStock() != null ? c.getStock() : 0), Integer::sum);
        Long categoryId = list.get(0).getCategoryId();
        // 修改藏品的userid为0
        List<CollectionDO> updateCollectionList = list.stream().map(collectionDO -> new CollectionDO()
                .setId(collectionDO.getId()).setUserId(0L)).toList();
        collectionMapper.updateBatch(updateCollectionList);
        // 复制分类根据原分类来卖
        if (createReqVO.getCopyId() != null) {
            createReqVO.setCategoryId(createReqVO.getCopyId());
        }
        String batchfee = configApi.getConfigValueByKey("batchfee");
        float rate = (10000 - Float.parseFloat(batchfee) * 100) /10000;
        // 查询buyorder
        List<BuyOrderDO> buyOrderList = buyOrderMapper.selectListForUpdate(createReqVO.getCategoryId(),createReqVO.getUserId(),createReqVO.getPrice());
        if (buyOrderList !=null &&!buyOrderList.isEmpty()) {

            // 交易逻辑
            AtomicInteger amount = new AtomicInteger(list.size());
            for (int i = 0; i < buyOrderList.size(); i++) {
                BuyOrderDO buyOrderDO = buyOrderList.get(i);
                int buyOrderDOAmount = buyOrderDO.getAmount();
                int amountP = amount.get();
                if (amountP > 0) {
                    if (buyOrderDOAmount >= amountP) {
                        buyOrderDO.setAmount(buyOrderDOAmount - amountP);
                        amount.set(0);

                        // 成交后藏品转移到购买者
                        List<CollectionDO> newList = list.stream().map(collectionDO -> new CollectionDO()
                                .setId(collectionDO.getId()).setUserId(buyOrderDO.getUserId())).toList();
                        collectionMapper.updateBatch(newList);
                        // 新增藏品变更记录（买）
                        collectionRecordMapper.insert(CollectionRecordDO.builder()
                                .userId(buyOrderDO.getUserId())
                                .categoryId(categoryId)
                                .amount(newList.size())
                                .type(5)
                                .build());
                        list = null;
                        // 结算钱
                        int totalPrice = (int)(createReqVO.getPrice()*amountP * rate);
                        PayWalletAddBalanceReqDTO payWalletAddBalanceReqDTO = new PayWalletAddBalanceReqDTO()
                                .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.INCOME.getType())
                                .setUserId(createReqVO.getUserId()).setBizId(buyOrderDO.getId().toString())
                                .setPrice(totalPrice);

                        payWalletApi.addWalletBalance(payWalletAddBalanceReqDTO);
                        buyOrderDO.setDealPrice(buyOrderDO.getDealPrice() + createReqVO.getPrice()*amountP);
                        // 等于也要退款
                        int refundAmount = buyOrderDO.getPrice()*buyOrderDO.getDealAmount() - buyOrderDO.getDealPrice();
                        if(buyOrderDOAmount == amountP && refundAmount > 0) {
                            PayWalletAddBalanceReqDTO refund = new PayWalletAddBalanceReqDTO()
                                    .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.UPDATE_BALANCE.getType())
                                    .setUserId(buyOrderDO.getUserId()).setBizId(buyOrderDO.getId().toString())
                                    .setPrice(refundAmount);

                            payWalletApi.addWalletBalance(refund);
                        }
                        // 成交记录
                        dealOrderMapper.insert(new DealOrderDO().setAmount(amountP).setCategoryId(createReqVO.getCategoryId()).setPrice(createReqVO.getPrice()));
                        // mq发通知
                        notifyProducer.sendNotifySendMessage(NotifySceneEnum.BUY_NOTIFY.getTemplateCode(), buyOrderDO.getUserId(), amountP, createReqVO.getPrice(),null);
                        notifyProducer.sendNotifySendMessage(NotifySceneEnum.SELL_NOTIFY.getTemplateCode(), createReqVO.getUserId(), amountP, createReqVO.getPrice(),totalPrice);
                        break;
                    } else {
                        buyOrderDO.setAmount(0);
                        amount.set(amountP - buyOrderDOAmount);

                        // 成交后藏品转移到购买者

                        List<CollectionDO> selledList = list.subList(0, buyOrderDOAmount);
                        list = list.subList(buyOrderDOAmount, list.size());

                        List<CollectionDO> newList = selledList.stream().map(collectionDO -> new CollectionDO()
                                .setId(collectionDO.getId()).setUserId(buyOrderDO.getUserId())).toList();
                        collectionMapper.updateBatch(newList);
                        // 新增藏品变更记录（买）
                        collectionRecordMapper.insert(CollectionRecordDO.builder()
                                .userId(buyOrderDO.getUserId())
                                .categoryId(categoryId)
                                .amount(newList.size())
                                .type(5)
                                .build());

                        // 结算钱
                        int totalPrice = (int)(createReqVO.getPrice()*buyOrderDOAmount* rate);
                        PayWalletAddBalanceReqDTO payWalletAddBalanceReqDTO = new PayWalletAddBalanceReqDTO()
                                .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.INCOME.getType())
                                .setUserId(createReqVO.getUserId()).setBizId(buyOrderDO.getId().toString())
                                .setPrice(totalPrice);

                        payWalletApi.addWalletBalance(payWalletAddBalanceReqDTO);
                        // 退款
                        buyOrderDO.setDealPrice(buyOrderDO.getDealPrice() + createReqVO.getPrice()*buyOrderDOAmount);
                        int refundAmount = buyOrderDO.getPrice()*buyOrderDO.getDealAmount() - buyOrderDO.getDealPrice();
                        if (refundAmount > 0) {
                            PayWalletAddBalanceReqDTO refund = new PayWalletAddBalanceReqDTO()
                                    .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.UPDATE_BALANCE.getType())
                                    .setUserId(buyOrderDO.getUserId()).setBizId(buyOrderDO.getId().toString())
                                    .setPrice(refundAmount);

                            payWalletApi.addWalletBalance(refund);
                        }
                        // 成交记录
                        dealOrderMapper.insert(new DealOrderDO().setAmount(buyOrderDOAmount).setCategoryId(createReqVO.getCategoryId()).setPrice(createReqVO.getPrice()));
                        // mq发通知
                        notifyProducer.sendNotifySendMessage(NotifySceneEnum.BUY_NOTIFY.getTemplateCode(), buyOrderDO.getUserId(), buyOrderDOAmount, createReqVO.getPrice(),null);
                        notifyProducer.sendNotifySendMessage(NotifySceneEnum.SELL_NOTIFY.getTemplateCode(), createReqVO.getUserId(), buyOrderDOAmount, createReqVO.getPrice(),totalPrice);
                    }
                }
            }

            createReqVO.setAmount(amount.get());
            // 成交，更新buyOrder
            List<BuyOrderDO> updateList = buyOrderList.stream().map(buyOrderDO -> new BuyOrderDO()
                    .setAmount(buyOrderDO.getAmount())
                    .setId(buyOrderDO.getId())
                    .setDealPrice(buyOrderDO.getDealPrice())
            ).toList();
            buyOrderMapper.updateBatch(updateList);
        }
        if (list != null) {
            // 插入sellorder
            List<SellOrderDO> sellOrderDOS = list.stream().map(collectionDO -> new SellOrderDO()
                    .setUserId(createReqVO.getUserId()).setCollectionId(collectionDO.getId())
                    .setCategoryId(createReqVO.getCategoryId())
                    .setPrice(createReqVO.getPrice())
                    .setAmount(1)
                    .setDealAmount(1)
            ).toList();
            sellOrderMapper.insertBatch(sellOrderDOS);
        }

        // 新增藏品变更记录（卖）
        collectionRecordMapper.insert(CollectionRecordDO.builder()
                .userId(createReqVO.getUserId())
                .categoryId(categoryId)
                .amount(-totalStock)
                .type(6)
                .build());

        // 返回
        return 0L;
    }


    @Override
    public void updateSellOrder(SellOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateSellOrderExists(updateReqVO.getId());
        // 更新
        SellOrderDO updateObj = BeanUtils.toBean(updateReqVO, SellOrderDO.class);
        sellOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteSellOrder(Long id) {
        // 校验存在
        validateSellOrderExists(id);
        // 删除
        sellOrderMapper.deleteById(id);
    }

    @Override
        public void deleteSellOrderListByIds(List<Long> ids) {
        // 删除
        sellOrderMapper.deleteByIds(ids);
        }


    private void validateSellOrderExists(Long id) {
        if (sellOrderMapper.selectById(id) == null) {
            throw exception(BATCH_TRADE_NOT_EXISTS);
        }
    }

    @Override
    public SellOrderDO getSellOrder(Long id) {
        return sellOrderMapper.selectById(id);
    }

    private SellOrderServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    @Override
    public UserTradeInfoRespVO getUserTradeInfo(Long userId) {
        // 获取在售商品数量
        Long sellAmount = yikoujiaMapper.selectCount(
                new LambdaQueryWrapper<YikoujiaDO>()
                        .eq(YikoujiaDO::getUserId, userId)
                        .eq(YikoujiaDO::getStatus, 3)
        );

        // 获取昨日收益
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        AppPayWalletTransactionSummaryRespVO summary = payWalletTransactionService.getWalletTransactionSummary(userId, MEMBER.getValue(), new LocalDateTime[]{yesterday,yesterday}, PayWalletBizTypeEnum.INCOME.getType());
        // 获取能量石
        EnergyStoneDO energyStoneDO = energyStoneMapper.selectOne("user_id", userId);



        return new UserTradeInfoRespVO()
                .setSellAmount(Math.toIntExact(sellAmount))
                .setIncome(summary.getTotalIncome()).setStoneAmount(energyStoneDO == null ? 0 : energyStoneDO.getAmount());
    }


    @Override
    public PageResult<SellOrderRespVO> getSellOrderPage(SellOrderPageReqVO pageReqVO) {
        return sellOrderMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(SellOrderCancelVO reqVO) {
        SellOrderDO sellOrderDO = sellOrderMapper.selectOne(new LambdaQueryWrapper<SellOrderDO>()
                .eq(SellOrderDO::getId, reqVO.getId())
                .eq(SellOrderDO::getUserId, reqVO.getUserId())
        );
        if (sellOrderDO == null) {
            throw exception(ORDER_NOT_EXISTS);
        } else {
            collectionMapper.updateById(new CollectionDO().setId(sellOrderDO.getCollectionId()).setUserId(reqVO.getUserId()));
            sellOrderMapper.deleteById(sellOrderDO.getId());
        }
    }



}