package com.techtron.onebook.module.app.service.ykjorder;

import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.dal.dataobject.ykjorder.YkjOrderDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import com.techtron.onebook.module.app.dal.mysql.ykjorder.YkjOrderMapper;
import com.techtron.onebook.module.app.mq.producer.DealNotifyProducer;
import com.techtron.onebook.module.app.service.category.CollectionCategoryService;
import com.techtron.onebook.module.app.service.yikoujia.YikoujiaService;
import com.techtron.onebook.module.infra.api.config.ConfigApi;
import com.techtron.onebook.module.pay.api.wallet.PayWalletApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class YkjOrderServiceImplTest {

    @InjectMocks
    private YkjOrderServiceImpl service;

    @Mock
    private YikoujiaMapper yikoujiaMapper;
    @Mock
    private YkjOrderMapper ykjOrderMapper;
    @Mock
    private ConfigApi configApi;
    @Mock
    private PayWalletApi payWalletApi;
    @Mock
    private DealNotifyProducer dealNotifyProducer;
    @Mock
    private CollectionMapper collectionMapper;
    @Mock
    private CollectionCategoryService collectionCategoryService;
    @Mock
    private YikoujiaService yikoujiaService;
    @Mock
    private com.techtron.onebook.module.app.service.notify.AppSubscribeMessageService subscribeMessageService;
    @Mock
    private com.techtron.onebook.module.app.service.getback.GetbackService getbackService;
    @Mock
    private com.techtron.onebook.module.pay.api.order.PayOrderApi payOrderApi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(collectionCategoryService.getPurchaseCategory(any())).thenReturn(
                new com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO().setId(50L).setName("自定义"));
    }

    @Test
    void shouldRecognizePlatformSelfOperatedProduct() {
        YikoujiaDO product = new YikoujiaDO();

        assertTrue(YkjOrderServiceImpl.isPlatformSelfOperated(product));
        assertTrue(YkjOrderServiceImpl.hasValidOwnership(product));
    }

    @Test
    void shouldRecognizeUserConsignmentProduct() {
        YikoujiaDO product = new YikoujiaDO().setUserId(10L).setCollectionId(20L);

        assertFalse(YkjOrderServiceImpl.isPlatformSelfOperated(product));
        assertTrue(YkjOrderServiceImpl.hasValidOwnership(product));
    }

    @Test
    void shouldRejectPartiallyMissingOwnership() {
        assertFalse(YkjOrderServiceImpl.hasValidOwnership(new YikoujiaDO().setUserId(10L)));
        assertFalse(YkjOrderServiceImpl.hasValidOwnership(new YikoujiaDO().setCollectionId(20L)));
    }

    @Test
    void platformSelfOperatedPaymentShouldStayInMerchantAndCreateBuyerCollection() {
        YikoujiaDO product = new YikoujiaDO()
                .setId(40L)
                .setName("平台自营藏品")
                .setPrice(370_000)
                .setStatus(3);
        YkjOrderDO order = new YkjOrderDO().setId(30L).setYkjId(40L).setUserId(20L);
        when(yikoujiaMapper.selectOne(any())).thenReturn(product);

        service.updateOrder(order);

        verify(payWalletApi, never()).addWalletBalance(any());
        verify(configApi, never()).getConfigValueByKey(any());
        verify(dealNotifyProducer, never()).sendNotifySendMessage(any(), any(), any(), any());
        verify(collectionMapper).insert(any(CollectionDO.class));
        verify(collectionMapper).insert(org.mockito.ArgumentMatchers.<CollectionDO>argThat(c ->
                "自定义".equals(c.getCategoryName()) && Long.valueOf(50L).equals(c.getCategoryId())));
        verify(collectionMapper, never()).updateById(any(CollectionDO.class));
        verify(getbackService, never()).createGetback(any());
    }

    @Test void directShippingCreatesGetbackWithPurchasedCollectionAndSnapshot() {
        when(yikoujiaMapper.selectOne(any())).thenReturn(new YikoujiaDO().setId(40L)
                .setName("自营卡片").setPrice(100).setStatus(3).setAmount(1));
        org.mockito.Mockito.doAnswer(inv -> {
            ((CollectionDO) inv.getArgument(0)).setId(80L); return 1;
        }).when(collectionMapper).insert(any(CollectionDO.class));
        when(getbackService.createGetback(any())).thenReturn(90L);
        var order = shippingOrder();
        service.updateOrder(order);
        verify(getbackService).createGetback(org.mockito.ArgumentMatchers.argThat(req ->
                req.getUserId().equals(20L) && req.getCollectionIds().equals(java.util.List.of(80L))
                && "收件人".equals(req.getReceiverName()) && "详细地址".equals(req.getReceiverDetailAddress())));
        verify(ykjOrderMapper).updateById(org.mockito.ArgumentMatchers.<YkjOrderDO>argThat(row ->
                Long.valueOf(90L).equals(row.getGetbackId()) && Long.valueOf(30L).equals(row.getId())));
    }

    @Test void purchasePreservesSourceStandardCategory() {
        when(yikoujiaMapper.selectOne(any())).thenReturn(new YikoujiaDO().setId(40L)
                .setUserId(99L).setCollectionId(80L).setName("签名卡片").setPrice(100).setStatus(3));
        when(collectionMapper.selectById(80L)).thenReturn(new CollectionDO().setId(80L).setCategoryId(680L));
        when(collectionCategoryService.getPurchaseCategory(680L)).thenReturn(
                new com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO().setId(680L).setName("签名卡砖"));
        when(configApi.getConfigValueByKey("ykjfee")).thenReturn("0");
        service.updateOrder(new YkjOrderDO().setId(30L).setYkjId(40L).setUserId(20L));
        verify(collectionMapper).updateById(org.mockito.ArgumentMatchers.<CollectionDO>argThat(c ->
                Long.valueOf(680L).equals(c.getCategoryId()) && "签名卡砖".equals(c.getCategoryName())
                && Long.valueOf(20L).equals(c.getUserId())));
    }

    @Test void consignmentShippingUsesTransferredCollection() {
        when(yikoujiaMapper.selectOne(any())).thenReturn(new YikoujiaDO().setId(40L)
                .setUserId(99L).setCollectionId(80L).setName("寄售卡片").setPrice(100).setStatus(3));
        when(configApi.getConfigValueByKey("ykjfee")).thenReturn("0");
        when(getbackService.createGetback(any())).thenReturn(90L);
        service.updateOrder(shippingOrder());
        verify(collectionMapper, never()).insert(any(CollectionDO.class));
        verify(getbackService).createGetback(org.mockito.ArgumentMatchers.argThat(req ->
                req.getUserId().equals(20L) && req.getCollectionIds().equals(java.util.List.of(80L))));
    }

    @Test void fulfillmentValidationRejectsIncompleteOrUnknownChoices() {
        var order = shippingOrder();
        YkjOrderServiceImpl.validateFulfillment(order);
        order.setReceiverName(" ");
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> YkjOrderServiceImpl.validateFulfillment(order));
        order.setFulfillmentType("OTHER");
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> YkjOrderServiceImpl.validateFulfillment(order));
    }

    @Test void legacyOrdersDefaultToWarehouseAndDiscardShippingAddress() {
        var order = shippingOrder().setFulfillmentType(null);
        YkjOrderServiceImpl.validateFulfillment(order);
        org.junit.jupiter.api.Assertions.assertEquals("WAREHOUSE", order.getFulfillmentType());
        org.junit.jupiter.api.Assertions.assertNull(order.getReceiverName());
    }

    @Test void duplicatePaidCallbackDoesNotCreateAnotherGetback() {
        when(ykjOrderMapper.selectOne(any())).thenReturn(shippingOrder().setStatus(1).setPayOrderId(100L));
        service.updateOrderPaid(30L, 100L);
        verify(getbackService, never()).createGetback(any());
        verify(collectionMapper, never()).insert(any(CollectionDO.class));
    }

    private YkjOrderDO shippingOrder() {
        return new YkjOrderDO().setId(30L).setYkjId(40L).setUserId(20L).setFulfillmentType("SHIP")
                .setReceiverName("收件人").setReceiverMobile("13800000000")
                .setReceiverAreaName("浙江省杭州市").setReceiverDetailAddress("详细地址");
    }

    @Test void unpaidOrderSavesChoiceWithoutCreatingCollectionOrGetback() {
        when(yikoujiaMapper.selectOne(any())).thenReturn(new YikoujiaDO().setId(40L)
                .setName("卡片").setPrice(100).setStatus(3));
        org.mockito.Mockito.doAnswer(inv -> {
            ((YkjOrderDO) inv.getArgument(0)).setId(30L); return 1;
        }).when(ykjOrderMapper).insert(any(YkjOrderDO.class));
        when(payOrderApi.createOrder(any())).thenReturn(100L);
        var req = new com.techtron.onebook.module.app.controller.app.ykjorder.vo.YkjOrderSaveReqVO();
        req.setUserId(20L); req.setYkjId(40L); req.setFulfillmentType("SHIP");
        req.setReceiverName("收件人"); req.setReceiverMobile("13800000000");
        req.setReceiverAreaName("浙江省杭州市"); req.setReceiverDetailAddress("详细地址");
        org.junit.jupiter.api.Assertions.assertEquals(100L, service.createYkjOrder(req));
        verify(ykjOrderMapper).insert(org.mockito.ArgumentMatchers.<YkjOrderDO>argThat(row ->
                "SHIP".equals(row.getFulfillmentType()) && Integer.valueOf(0).equals(row.getStatus())
                        && "详细地址".equals(row.getReceiverDetailAddress())));
        verify(getbackService, never()).createGetback(any());
        verify(collectionMapper, never()).insert(any(CollectionDO.class));
    }

    @Test void missingAddressCannotStartPayment() {
        var req = new com.techtron.onebook.module.app.controller.app.ykjorder.vo.YkjOrderSaveReqVO();
        req.setUserId(20L); req.setYkjId(40L); req.setFulfillmentType("SHIP");
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.createYkjOrder(req));
        verify(payOrderApi, never()).createOrder(any());
        verify(ykjOrderMapper, never()).insert(any(YkjOrderDO.class));
    }
}
