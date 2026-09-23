package com.techtron.onebook.module.app.service.ykjorder;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.json.JsonUtils;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategorySaveReqVO;
import com.techtron.onebook.module.app.controller.app.ykjorder.vo.YkjOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.ykjorder.vo.YkjOrderSaveReqVO;
import com.techtron.onebook.module.app.convert.ykjorder.YkjOrderConvert;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.dal.dataobject.ykjorder.YkjOrderDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import com.techtron.onebook.module.app.dal.mysql.ykjorder.YkjOrderMapper;
import com.techtron.onebook.module.app.enums.NotifySceneEnum;
import com.techtron.onebook.module.app.mq.producer.DealNotifyProducer;
import com.techtron.onebook.module.app.service.category.CollectionCategoryService;
import com.techtron.onebook.module.app.service.notify.AppSubscribeMessageService;
import com.techtron.onebook.module.app.service.yikoujia.YikoujiaService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static com.techtron.onebook.framework.common.enums.UserTypeEnum.MEMBER;
import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.*;

/**
 * 一口价买单 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
@Slf4j
public class YkjOrderServiceImpl implements YkjOrderService {

    @Resource
    private YkjOrderMapper ykjOrderMapper;

    @Resource
    private PayOrderApi payOrderApi;

    @Resource
    private YikoujiaMapper yikoujiaMapper;

    @Resource
    private ConfigApi configApi;

    @Resource
    private PayWalletApi payWalletApi;

    @Resource
    private DealNotifyProducer dealNotifyProducer;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private CollectionCategoryService collectionCategoryService;

    @Resource
    private YikoujiaService yikoujiaService;

    @Resource
    private AppSubscribeMessageService subscribeMessageService;

    @Resource
    private com.techtron.onebook.module.app.service.getback.GetbackService getbackService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createYkjOrder(YkjOrderSaveReqVO createReqVO) {
        // 插入
        YkjOrderDO ykjOrder = BeanUtils.toBean(createReqVO, YkjOrderDO.class);
        ykjOrder.setId(null);
        validateFulfillment(ykjOrder);
        ykjOrder.setStatus(0);
        YikoujiaDO yikoujiaDO = yikoujiaMapper.selectOne(
                new LambdaQueryWrapper<YikoujiaDO>()
                        .eq(YikoujiaDO::getId, createReqVO.getYkjId())
                        .eq(YikoujiaDO::getStatus, 3)
                        .last("FOR UPDATE")
        );
        if (yikoujiaDO == null || !hasValidOwnership(yikoujiaDO)) {
            throw exception(YKJ_COLLECTION_NOT_EXISTS);
        }
        ykjOrder.setPrice(yikoujiaDO.getPrice());
        ykjOrderMapper.insert(ykjOrder);
        createPayOrder(ykjOrder);
        // 返回
        return ykjOrder.getPayOrderId();
    }

    private void createPayOrder(YkjOrderDO order) {
        // 创建支付单，用于后续的支付
        PayOrderCreateReqDTO payOrderCreateReqDTO = YkjOrderConvert.INSTANCE.convert(
                order);
        Long payOrderId = payOrderApi.createOrder(payOrderCreateReqDTO);

        // 更新到交易单上
        ykjOrderMapper.updateById(new YkjOrderDO().setId(order.getId()).setPayOrderId(payOrderId));
        order.setPayOrderId(payOrderId);
    }

    @Override
    public void updateYkjOrder(YkjOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateYkjOrderExists(updateReqVO.getId());
        // 更新
        YkjOrderDO updateObj = BeanUtils.toBean(updateReqVO, YkjOrderDO.class);
        ykjOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteYkjOrder(Long id) {
        // 校验存在
        validateYkjOrderExists(id);
        // 删除
        ykjOrderMapper.deleteById(id);
    }

    @Override
        public void deleteYkjOrderListByIds(List<Long> ids) {
        // 删除
        ykjOrderMapper.deleteByIds(ids);
        }


    private void validateYkjOrderExists(Long id) {
        if (ykjOrderMapper.selectById(id) == null) {
            throw exception(YKJ_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public YkjOrderDO getYkjOrder(Long id) {
        return ykjOrderMapper.selectById(id);
    }

    @Override
    public PageResult<YkjOrderDO> getYkjOrderPage(YkjOrderPageReqVO pageReqVO) {
        return ykjOrderMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderPaid(Long id, Long payOrderId) {
        // 1.1 校验订单是否存在
        YkjOrderDO order = validateOrderExists(id);
        // 1.2 校验订单已支付
        if (ObjectUtil.equal(1, order.getStatus())) {
            // 特殊：支付单号相同，直接返回，说明重复回调
            if (ObjectUtil.equals(order.getPayOrderId(), payOrderId)) {
                log.warn("[updateOrderPaid][order({}) 已支付，且支付单号相同({})，直接返回]", order.getId(), payOrderId);
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

    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(YkjOrderDO createReqVO) {
        validateFulfillment(createReqVO);
        YikoujiaDO yikoujiaDO = yikoujiaMapper.selectOne(
                new LambdaQueryWrapper<YikoujiaDO>()
                        .eq(YikoujiaDO::getId, createReqVO.getYkjId())
                        .eq(YikoujiaDO::getStatus, 3)
                        .last("for update")
        );
        if (yikoujiaDO == null || !hasValidOwnership(yikoujiaDO)) {
            throw exception(YKJ_COLLECTION_NOT_EXISTS);
        }

        CollectionDO sourceCollection = yikoujiaDO.getCollectionId() == null ? null
                : collectionMapper.selectById(yikoujiaDO.getCollectionId());
        CollectionCategoryDO purchaseCategory = collectionCategoryService.getPurchaseCategory(
                sourceCollection == null ? null : sourceCollection.getCategoryId());

        // 小程序内成交后必须同步下架闲鱼商品，避免同一件藏品被重复购买。
        // 调用失败时抛出异常，保留本地挂售状态，交由支付回调重试。
        if (yikoujiaDO.getProductId() != null && !yikoujiaDO.getProductId().isBlank()) {
            yikoujiaService.downProduct(yikoujiaDO.getProductId());
        }

        boolean platformSelfOperated = isPlatformSelfOperated(yikoujiaDO);
        int sellerIncome = 0;
        // 平台自营商品的支付款由小程序绑定的微信支付商户号直接收取，
        // 不再转入任何用户钱包。只有用户寄售商品才执行手续费结算。
        if (!platformSelfOperated) {
            String ykjFee = configApi.getConfigValueByKey("ykjfee");
            float rate = (10000 - Float.parseFloat(ykjFee) * 100) / 10000;
            sellerIncome = (int) (yikoujiaDO.getPrice() * rate);
            PayWalletAddBalanceReqDTO payWalletAddBalanceReqDTO = new PayWalletAddBalanceReqDTO()
                    .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.INCOME.getType())
                    .setUserId(yikoujiaDO.getUserId()).setBizId(yikoujiaDO.getId().toString())
                    .setPrice(sellerIncome);
            payWalletApi.addWalletBalance(payWalletAddBalanceReqDTO);
        }

        LocalDateTime soldAt = LocalDateTime.now();
        yikoujiaMapper.updateById(new YikoujiaDO()
                .setId(yikoujiaDO.getId())
                .setStatus(1)
                .setSaleChannel("MINIAPP")
                .setSoldAt(soldAt)
                .setLastSyncTime(soldAt)
                .setSyncRemark(yikoujiaDO.getProductId() == null || yikoujiaDO.getProductId().isBlank()
                        ? "小程序已成交"
                        : "小程序已成交，闲鱼商品已同步下架"));

        ykjOrderMapper.updateById(new YkjOrderDO().setId(createReqVO.getId()).setStatus(1));

        int amount = yikoujiaDO.getAmount() == null || yikoujiaDO.getAmount() <= 0
                ? 1 : yikoujiaDO.getAmount();
        CollectionDO buyerCollection = new CollectionDO()
                .setUserId(createReqVO.getUserId())
                .setStock(amount)
                .setRealStock(amount)
                .setCategoryId(purchaseCategory.getId())
                .setCategoryName(purchaseCategory.getName())
                .setStatus(1)
                .setTradeStatus(0)
                .setGetbackStatus(0)
                .setPicUrl(yikoujiaDO.getPicUrl() == null || yikoujiaDO.getPicUrl().isEmpty()
                        ? null : yikoujiaDO.getPicUrl().get(0))
                .setPicUrls(yikoujiaDO.getPicUrl())
                .setName(yikoujiaDO.getName());
        if (platformSelfOperated) {
            collectionMapper.insert(buyerCollection);
        } else {
            buyerCollection.setId(yikoujiaDO.getCollectionId());
            collectionMapper.updateById(buyerCollection);
            dealNotifyProducer.sendNotifySendMessage(NotifySceneEnum.YKJ_NOTIFY.getTemplateCode(),
                    yikoujiaDO.getUserId(), yikoujiaDO.getName(), sellerIncome);
            subscribeMessageService.notifyProductSold(yikoujiaDO.getUserId(), yikoujiaDO.getName(),
                    yikoujiaDO.getPrice(), soldAt);
        }

        subscribeMessageService.notifyPaymentSuccess(createReqVO.getUserId(), yikoujiaDO.getPrice(), soldAt,
                yikoujiaDO.getName(), amount);

        if ("SHIP".equals(createReqVO.getFulfillmentType())) {
            var request = new com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackSaveReqVO();
            request.setUserId(createReqVO.getUserId());
            request.setCollectionIds(List.of(buyerCollection.getId()));
            request.setReceiverName(createReqVO.getReceiverName());
            request.setReceiverMobile(createReqVO.getReceiverMobile());
            request.setReceiverAreaName(createReqVO.getReceiverAreaName());
            request.setReceiverDetailAddress(createReqVO.getReceiverDetailAddress());
            Long getbackId = getbackService.createGetback(request);
            ykjOrderMapper.updateById(new YkjOrderDO().setId(createReqVO.getId()).setGetbackId(getbackId));
        }

    }

    static void validateFulfillment(YkjOrderDO order) {
        if (order.getFulfillmentType() == null) order.setFulfillmentType("WAREHOUSE");
        if (!List.of("WAREHOUSE", "SHIP").contains(order.getFulfillmentType())) {
            throw new IllegalArgumentException("请选择入库或直接寄出");
        }
        if ("SHIP".equals(order.getFulfillmentType())) {
            String[] values = {order.getReceiverName(), order.getReceiverMobile(),
                    order.getReceiverAreaName(), order.getReceiverDetailAddress()};
            int[] limits = {20, 20, 100, 200};
            for (int i = 0; i < values.length; i++) {
                if (values[i] == null || values[i].isBlank() || values[i].length() > limits[i]) {
                    throw new IllegalArgumentException("请填写完整有效的收货地址");
                }
            }
        } else {
            order.setReceiverName(null).setReceiverMobile(null)
                    .setReceiverAreaName(null).setReceiverDetailAddress(null);
        }
    }

    static boolean isPlatformSelfOperated(YikoujiaDO product) {
        return product.getUserId() == null && product.getCollectionId() == null;
    }

    static boolean hasValidOwnership(YikoujiaDO product) {
        return isPlatformSelfOperated(product)
                || (product.getUserId() != null && product.getCollectionId() != null);
    }

    @NotNull
    private YkjOrderDO validateOrderExists(Long id) {
        // 校验订单是否存在
        YkjOrderDO order = ykjOrderMapper.selectOne(new LambdaQueryWrapper<YkjOrderDO>()
                .eq(YkjOrderDO::getId, id).last("FOR UPDATE"));
        if (order == null) {
            throw exception(YKJ_ORDER_NOT_EXISTS );
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
    private PayOrderRespDTO validatePayOrderPaid(YkjOrderDO order, Long payOrderId) {
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
        if (ObjectUtil.notEqual(payOrder.getPrice(), order.getPrice())) {
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

    private YkjOrderServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
