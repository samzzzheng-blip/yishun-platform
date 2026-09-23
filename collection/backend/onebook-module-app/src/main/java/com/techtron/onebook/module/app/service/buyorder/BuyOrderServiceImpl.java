package com.techtron.onebook.module.app.service.buyorder;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.json.JsonUtils;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderCancelVO;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderRespVO;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderSaveReqVO;
import com.techtron.onebook.module.app.convert.buyorder.BuyOrderConvert;
import com.techtron.onebook.module.app.dal.dataobject.buyorder.BuyOrderDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionRecordDO;
import com.techtron.onebook.module.app.dal.dataobject.dealorder.DealOrderDO;
import com.techtron.onebook.module.app.dal.dataobject.sellorder.SellOrderDO;
import com.techtron.onebook.module.app.dal.mysql.buyorder.BuyOrderMapper;
import com.techtron.onebook.module.app.dal.mysql.category.CollectionCategoryMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionRecordMapper;
import com.techtron.onebook.module.app.dal.mysql.dealorder.DealOrderMapper;
import com.techtron.onebook.module.app.dal.mysql.sellorder.SellOrderMapper;
import com.techtron.onebook.module.app.enums.NotifySceneEnum;
import com.techtron.onebook.module.app.mq.producer.NotifyProducer;
import com.techtron.onebook.module.app.service.collection.CollectionService;
import com.techtron.onebook.module.infra.api.config.ConfigApi;
import com.techtron.onebook.module.pay.api.order.PayOrderApi;
import com.techtron.onebook.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.techtron.onebook.module.pay.api.order.dto.PayOrderRespDTO;
import com.techtron.onebook.module.pay.api.wallet.PayWalletApi;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import com.techtron.onebook.module.pay.enums.order.PayOrderStatusEnum;
import com.techtron.onebook.module.pay.enums.wallet.PayWalletBizTypeEnum;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.techtron.onebook.framework.common.enums.UserTypeEnum.MEMBER;
import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.*;

/**
 * 批量交易买单 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
@Slf4j
public class BuyOrderServiceImpl implements BuyOrderService {

    @Resource
    private BuyOrderMapper buyOrderMapper;

    @Resource
    private SellOrderMapper sellOrderMapper;

    @Resource
    private PayOrderApi payOrderApi;

    @Autowired
    private PayWalletApi payWalletApi;
    @Autowired
    private CollectionService collectionService;

    @Resource
    private NotifyProducer notifyProducer;

    @Resource
    private DealOrderMapper dealOrderMapper;

    @Resource
    private CollectionCategoryMapper collectionCategoryMapper;

    @Resource
    private ConfigApi configApi;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private CollectionRecordMapper collectionRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBuyOrder(BuyOrderSaveReqVO createReqVO) {
        // 插入
        BuyOrderDO buyOrder = BeanUtils.toBean(createReqVO, BuyOrderDO.class);
        buyOrderMapper.insert(buyOrder);
        createPayOrder(buyOrder);

        // 返回
        return buyOrder.getPayOrderId();
    }

    private void createPayOrder(BuyOrderDO order) {
        // 创建支付单，用于后续的支付
        PayOrderCreateReqDTO payOrderCreateReqDTO = BuyOrderConvert.INSTANCE.convert(
                order);
        Long payOrderId = payOrderApi.createOrder(payOrderCreateReqDTO);

        // 更新到交易单上
        buyOrderMapper.updateById(new BuyOrderDO().setId(order.getId()).setPayOrderId(payOrderId));
        order.setPayOrderId(payOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderPaid(Long id, Long payOrderId) {
        // 1.1 校验订单是否存在
        BuyOrderDO order = validateOrderExists(id);
        // 1.2 校验订单已支付
        if (ObjectUtil.equal(1, order.getStatus())) {
            // 特殊：支付单号相同，直接返回，说明重复回调
            if (ObjectUtil.equals(order.getPayOrderId(), payOrderId)) {
                log.warn("[updateOrderPaid][order({}) 已支付，且支付单号相同({})，直接返回]", order, payOrderId);
                return;
            }
            log.error("[updateOrderPaid][order({}) 支付单不匹配({})，请进行处理！order 数据是：{}]",
                    id, payOrderId, JsonUtils.toJsonString(order));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }

        // 2. 校验支付订单的合法性
        PayOrderRespDTO payOrder = validatePayOrderPaid(order, payOrderId);

        // 3. 处理交易
        getSelf().updateOrder(order);


    }

    @NotNull
    private BuyOrderDO validateOrderExists(Long id) {
        // 校验订单是否存在
        BuyOrderDO order = buyOrderMapper.selectById(id);
        if (order == null) {
            throw exception(BATCH_TRADE_NOT_EXISTS );
        }
        return order;
    }

    /**
     * 校验支付订单的合法性
     *
     * @param order      交易订单
     * @param payOrderId 支付订单编号
     * @return 支付订单
     */
    private PayOrderRespDTO validatePayOrderPaid(BuyOrderDO order, Long payOrderId) {
        // 1. 校验支付单是否存在
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null) {
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 不存在，请进行处理！]", order.getId(), payOrderId);
            throw exception(ORDER_NOT_FOUND);
        }

        // 2.1 校验支付单已支付
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 未支付，请进行处理！payOrder 数据是：{}]",
                    order.getId(), payOrderId, JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS);
        }
        // 2.2 校验支付金额一致
        if (ObjectUtil.notEqual(payOrder.getPrice(), order.getAmount()*order.getPrice())) {
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 支付金额不匹配，请进行处理！order 数据是：{}，payOrder 数据是：{}]",
                    order.getId(), payOrderId, JsonUtils.toJsonString(order), JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH);
        }
        // 2.2 校验支付订单匹配（二次）
        if (ObjectUtil.notEqual(payOrder.getMerchantOrderId(), order.getId().toString())) {
            log.error("[validatePayOrderPaid][order({}) 支付单不匹配({})，请进行处理！payOrder 数据是：{}]",
                    order.getId(), payOrderId, JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }
        return payOrder;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(BuyOrderDO createReqVO) {
        String batchfee = configApi.getConfigValueByKey("batchfee");
        float rate = (10000 - Float.parseFloat(batchfee) * 100) /10000;
        // 查询sellorder
        List<SellOrderDO> sellyOrderList = sellOrderMapper.selectListForUpdate(createReqVO.getCategoryId(),createReqVO.getUserId(),createReqVO.getPrice());
        if (sellyOrderList != null && !sellyOrderList.isEmpty()) {
            // 交易逻辑
            int amount = createReqVO.getAmount();
            for (int i = 0; i < sellyOrderList.size(); i++) {
                SellOrderDO sellOrderDO = sellyOrderList.get(i);
                int sellOrderAmount = sellOrderDO.getAmount();
                int amountP = amount;
                if (amountP > 0) {
                    if (sellOrderAmount >= amountP) {
                        sellOrderDO.setAmount(sellOrderAmount - amountP);
                        amount = 0;

                        // 成交后藏品转移到购买者
                        collectionMapper.updateById(new CollectionDO().setId(sellOrderDO.getCollectionId()).setUserId(createReqVO.getUserId()));
                        // 新增藏品变更记录（买）
                        collectionRecordMapper.insert(CollectionRecordDO.builder()
                                .userId(createReqVO.getUserId())
                                .categoryId(createReqVO.getCategoryId())
                                .amount(1)
                                .type(5)
                                .build());
                        // 结算钱
                        int totalPrice = (int)(sellOrderDO.getPrice()*amountP*rate);
                        PayWalletAddBalanceReqDTO payWalletAddBalanceReqDTO = new PayWalletAddBalanceReqDTO()
                                .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.INCOME.getType())
                                .setUserId(sellOrderDO.getUserId()).setBizId(createReqVO.getId().toString())
                                .setPrice(totalPrice);

                        payWalletApi.addWalletBalance(payWalletAddBalanceReqDTO);
                        createReqVO.setDealPrice(createReqVO.getDealPrice() + sellOrderDO.getPrice()*amountP);
                        int refundAmount = createReqVO.getPrice()*createReqVO.getDealAmount() - createReqVO.getDealPrice();
                        // 退款
                        if (refundAmount > 0) {
                            PayWalletAddBalanceReqDTO refund = new PayWalletAddBalanceReqDTO()
                                    .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.UPDATE_BALANCE.getType())
                                    .setUserId(createReqVO.getUserId()).setBizId(createReqVO.getId().toString())
                                    .setPrice(refundAmount );

                            payWalletApi.addWalletBalance(refund);
                        }
                        // 成交记录
                        dealOrderMapper.insert(new DealOrderDO().setAmount(amountP).setCategoryId(createReqVO.getCategoryId()).setPrice(sellOrderDO.getPrice()));
                        // mq发通知
                        notifyProducer.sendNotifySendMessage(NotifySceneEnum.BUY_NOTIFY.getTemplateCode(), createReqVO.getUserId(), amountP, sellOrderDO.getPrice(), null);
                        notifyProducer.sendNotifySendMessage(NotifySceneEnum.SELL_NOTIFY.getTemplateCode(), sellOrderDO.getUserId(), amountP, sellOrderDO.getPrice(),totalPrice);
                        break;
                    } else {
                        sellOrderDO.setAmount(0);
                        amount = amountP - sellOrderAmount;
                        // 成交后藏品转移到购买者
                        collectionMapper.updateById(new CollectionDO().setId(sellOrderDO.getCollectionId()).setUserId(createReqVO.getUserId()));
                        // 新增藏品变更记录（买）
                        collectionRecordMapper.insert(CollectionRecordDO.builder()
                                .userId(createReqVO.getUserId())
                                .categoryId(createReqVO.getCategoryId())
                                .amount(1)
                                .type(5)
                                .build());
                        // 结算钱
                        int totalPrice = (int)(sellOrderDO.getPrice()*sellOrderAmount*rate);
                        PayWalletAddBalanceReqDTO payWalletAddBalanceReqDTO = new PayWalletAddBalanceReqDTO()
                                .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.INCOME.getType())
                                .setUserId(sellOrderDO.getUserId()).setBizId(createReqVO.getId().toString())
                                .setPrice(totalPrice);
                        payWalletApi.addWalletBalance(payWalletAddBalanceReqDTO);
                        createReqVO.setDealPrice(createReqVO.getDealPrice() + sellOrderDO.getPrice()*sellOrderAmount);
                        // 成交记录
                        dealOrderMapper.insert(new DealOrderDO().setAmount(sellOrderAmount).setCategoryId(createReqVO.getCategoryId()).setPrice(sellOrderDO.getPrice()));
                        // mq发通知
                        notifyProducer.sendNotifySendMessage(NotifySceneEnum.BUY_NOTIFY.getTemplateCode(), createReqVO.getUserId(), sellOrderAmount, sellOrderDO.getPrice(),null);
                        notifyProducer.sendNotifySendMessage(NotifySceneEnum.SELL_NOTIFY.getTemplateCode(), sellOrderDO.getUserId(), sellOrderAmount, sellOrderDO.getPrice(),totalPrice);
                    }
                }
            }
            createReqVO.setAmount(amount);
            // 成交，更新sellOrder
            List<SellOrderDO> updateList = sellyOrderList.stream().map(sellOrderDO -> new SellOrderDO()
                    .setAmount(sellOrderDO.getAmount()).setId(sellOrderDO.getId())).toList();
            sellOrderMapper.updateBatch(updateList);
        }
        // 更新buyorder
        // status
        buyOrderMapper.updateById(new BuyOrderDO()
                .setId(createReqVO.getId())
                .setAmount(createReqVO.getAmount())
                .setStatus(1)
                .setDealPrice(createReqVO.getDealPrice()));
    }

    @Override
    public void updateBuyOrder(BuyOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateBuyOrderExists(updateReqVO.getId());
        // 更新
        BuyOrderDO updateObj = BeanUtils.toBean(updateReqVO, BuyOrderDO.class);
        buyOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteBuyOrder(Long id) {
        // 校验存在
        validateBuyOrderExists(id);
        // 删除
        buyOrderMapper.deleteById(id);
    }

    @Override
        public void deleteBuyOrderListByIds(List<Long> ids) {
        // 删除
        buyOrderMapper.deleteByIds(ids);
        }


    private void validateBuyOrderExists(Long id) {
        if (buyOrderMapper.selectById(id) == null) {
            throw exception(BATCH_TRADE_NOT_EXISTS);
        }
    }

    @Override
    public BuyOrderDO getBuyOrder(Long id) {
        return buyOrderMapper.selectById(id);
    }

    private BuyOrderServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    @Override
    public PageResult<BuyOrderRespVO> getBuyOrderPage(BuyOrderPageReqVO pageReqVO) {
        return buyOrderMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(BuyOrderCancelVO reqVO) {
        BuyOrderDO buyOrderDO = buyOrderMapper.selectOne(new LambdaQueryWrapper<BuyOrderDO>()
                .eq(BuyOrderDO::getId, reqVO.getId())
                .eq(BuyOrderDO::getUserId, reqVO.getUserId())
                .eq(BuyOrderDO::getStatus, 1)
        );
        if (buyOrderDO == null) {
            throw exception(ORDER_NOT_EXISTS);
        } else {
            buyOrderMapper.deleteById(buyOrderDO.getId());
            PayWalletAddBalanceReqDTO refund = new PayWalletAddBalanceReqDTO()
                    .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.UPDATE_BALANCE.getType())
                    .setUserId(reqVO.getUserId()).setBizId(reqVO.getId().toString())
                    .setPrice(buyOrderDO.getPrice() * buyOrderDO.getAmount() - buyOrderDO.getDealPrice());

            payWalletApi.addWalletBalance(refund);
        }
    }



}