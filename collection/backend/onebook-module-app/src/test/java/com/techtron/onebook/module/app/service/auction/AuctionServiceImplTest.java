package com.techtron.onebook.module.app.service.auction;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionDO;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionSettlementDO;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionMapper;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionSettlementMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.controller.app.auction.vo.AppAuctionRespVO;
import com.techtron.onebook.module.app.service.notify.AppSubscribeMessageService;
import com.techtron.onebook.module.infra.api.config.ConfigApi;
import com.techtron.onebook.module.pay.api.wallet.PayWalletApi;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionServiceImplTest {

    @Test
    void auctionEligibilityAllowsCustomCategory() {
        CollectionDO collection = CollectionDO.builder().name("普通商品").categoryName("自定义收藏").build();
        CollectionCategoryDO category = CollectionCategoryDO.builder().userId(100L).build();

        assertTrue(AuctionServiceImpl.isAuctionEligible(collection, category));
    }

    @Test
    void auctionEligibilityAllowsSignedProduct() {
        CollectionDO collection = CollectionDO.builder().name("声优亲笔签名卡").categoryName("评级卡砖").build();
        CollectionCategoryDO category = CollectionCategoryDO.builder().userId(0L).build();

        assertTrue(AuctionServiceImpl.isAuctionEligible(collection, category));
    }

    @Test
    void auctionEligibilityRejectsOrdinaryCardBrick() {
        CollectionDO collection = CollectionDO.builder().name("普通角色卡").categoryName("评级卡砖").build();
        CollectionCategoryDO category = CollectionCategoryDO.builder().userId(0L).build();

        assertFalse(AuctionServiceImpl.isAuctionEligible(collection, category));
    }

    @Test
    void closeExpiredAuctionsContinuesAfterOneAuctionFails() {
        AuctionMapper mapper = mock(AuctionMapper.class);
        AuctionService self = mock(AuctionService.class);
        AuctionServiceImpl service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", mapper);
        ReflectionTestUtils.setField(service, "self", self);
        when(mapper.selectSyncIds(100)).thenReturn(List.of(
                AuctionDO.builder().id(1L).build(), AuctionDO.builder().id(2L).build()));
        doThrow(new IllegalStateException("broken auction")).when(self).closeAuction(1L);

        assertEquals(1, service.closeExpiredAuctions(100));
        verify(self).closeAuction(1L);
        verify(self).closeAuction(2L);
    }

    @Test
    void requestDelistMarksPublishedAuctionForAdminReview() {
        AuctionMapper mapper = mock(AuctionMapper.class);
        AuctionServiceImpl service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", mapper);
        when(mapper.selectByIdForUpdate(1L)).thenReturn(
                AuctionDO.builder().id(1L).sellerId(100L).status(1).delistStatus(0).build());

        service.requestDelist(100L, 1L);

        verify(mapper).markDelistRequested(1L);
    }

    @Test
    void confirmSaleLocksFeeSnapshotAndCreditsNetAmountOnce() {
        AuctionMapper auctionMapper = mock(AuctionMapper.class);
        AuctionSettlementMapper settlementMapper = mock(AuctionSettlementMapper.class);
        ConfigApi configApi = mock(ConfigApi.class);
        PayWalletApi payWalletApi = mock(PayWalletApi.class);
        AuctionServiceImpl service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", auctionMapper);
        ReflectionTestUtils.setField(service, "auctionSettlementMapper", settlementMapper);
        ReflectionTestUtils.setField(service, "configApi", configApi);
        ReflectionTestUtils.setField(service, "payWalletApi", payWalletApi);
        ReflectionTestUtils.setField(service, "subscribeMessageService", mock(AppSubscribeMessageService.class));
        when(auctionMapper.selectByIdForUpdate(1L)).thenReturn(AuctionDO.builder()
                .id(1L).sellerId(100L).status(2).build());
        when(configApi.getConfigValueByKey("auctionfee")).thenReturn("6");
        when(auctionMapper.update(any(AuctionDO.class), any(UpdateWrapper.class))).thenReturn(1);

        service.confirmSale(7L, 1L, 10001, "已核对付款且无退款");

        ArgumentCaptor<AuctionSettlementDO> settlementCaptor = ArgumentCaptor.forClass(AuctionSettlementDO.class);
        verify(settlementMapper).insert(settlementCaptor.capture());
        assertEquals(10001, settlementCaptor.getValue().getGrossAmount());
        assertEquals(6, settlementCaptor.getValue().getFeeRate());
        assertEquals(600, settlementCaptor.getValue().getFeeAmount());
        assertEquals(9401, settlementCaptor.getValue().getSellerIncome());
        assertEquals(7L, settlementCaptor.getValue().getConfirmUserId());

        ArgumentCaptor<PayWalletAddBalanceReqDTO> walletCaptor =
                ArgumentCaptor.forClass(PayWalletAddBalanceReqDTO.class);
        verify(payWalletApi).addWalletBalance(walletCaptor.capture());
        assertEquals(100L, walletCaptor.getValue().getUserId());
        assertEquals(9401, walletCaptor.getValue().getPrice());
    }

    @Test
    void approveDelistRestoresCollectionAfterAdminConfirmsManualGoofishDelist() {
        AuctionMapper auctionMapper = mock(AuctionMapper.class);
        CollectionMapper collectionMapper = mock(CollectionMapper.class);
        AuctionServiceImpl service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", auctionMapper);
        ReflectionTestUtils.setField(service, "auctionSettlementMapper", mock(AuctionSettlementMapper.class));
        ReflectionTestUtils.setField(service, "collectionMapper", collectionMapper);
        ReflectionTestUtils.setField(service, "subscribeMessageService", mock(AppSubscribeMessageService.class));
        when(auctionMapper.selectByIdForUpdate(1L)).thenReturn(AuctionDO.builder()
                .id(1L).sellerId(100L).collectionId(9L).status(1).delistStatus(1)
                .goofishProductId("goofish-1").build());
        when(collectionMapper.selectByIdForUpdate(9L)).thenReturn(
                CollectionDO.builder().id(9L).userId(0L).tradeStatus(2).build());

        service.approveDelist(7L, 1L);

        ArgumentCaptor<CollectionDO> collectionCaptor = ArgumentCaptor.forClass(CollectionDO.class);
        verify(collectionMapper).updateById(collectionCaptor.capture());
        assertTrue(Objects.equals(collectionCaptor.getValue().getUserId(), 100L));
        assertTrue(Objects.equals(collectionCaptor.getValue().getTradeStatus(), 0));

        ArgumentCaptor<AuctionDO> auctionCaptor = ArgumentCaptor.forClass(AuctionDO.class);
        verify(auctionMapper).updateById(auctionCaptor.capture());
        assertTrue(Objects.equals(auctionCaptor.getValue().getStatus(), 5));
        assertTrue(Objects.equals(auctionCaptor.getValue().getDelistStatus(), 2));
        assertTrue(Objects.equals(auctionCaptor.getValue().getDelistAuditUserId(), 7L));
    }

    @Test
    void bindGoofishAuctionAcceptsAuctionItemIdWithoutCallingOrdinaryProductApi() {
        AuctionMapper auctionMapper = mock(AuctionMapper.class);
        AppSubscribeMessageService subscribeMessageService = mock(AppSubscribeMessageService.class);
        AuctionServiceImpl service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", auctionMapper);
        ReflectionTestUtils.setField(service, "subscribeMessageService", subscribeMessageService);
        AuctionDO auction = AuctionDO.builder()
                .id(1L).sellerId(100L).collectionName("测试拍品").status(0).startPrice(5000)
                .endTime(LocalDateTime.now().plusDays(2)).build();
        auction.setCreateTime(LocalDateTime.now());
        when(auctionMapper.selectById(1L)).thenReturn(auction);
        when(auctionMapper.update(any(AuctionDO.class), any(UpdateWrapper.class))).thenReturn(1);

        service.bindGoofishAuction(7L, 1L, "1079935170548");

        ArgumentCaptor<AuctionDO> captor = ArgumentCaptor.forClass(AuctionDO.class);
        verify(auctionMapper).update(captor.capture(), any(UpdateWrapper.class));
        assertEquals(1, captor.getValue().getStatus());
        assertEquals("1079935170548", captor.getValue().getGoofishProductId());
        assertEquals("https://www.goofish.com/item?id=1079935170548", captor.getValue().getGoofishUrl());
        assertEquals(5000, captor.getValue().getCurrentPrice());
        assertEquals(0, captor.getValue().getBidCount());
        assertEquals(7L, captor.getValue().getReviewUserId());
        assertTrue(captor.getValue().getEndTime().isAfter(LocalDateTime.now().plusHours(47)));
    }

    @Test
    void bindGoofishAuctionRestartsExpiredRequestedDurationFromBindingTime() {
        AuctionMapper auctionMapper = mock(AuctionMapper.class);
        AuctionServiceImpl service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", auctionMapper);
        ReflectionTestUtils.setField(service, "subscribeMessageService", mock(AppSubscribeMessageService.class));
        LocalDateTime submittedAt = LocalDateTime.now().minusDays(4);
        AuctionDO auction = AuctionDO.builder()
                .id(1L).sellerId(100L).collectionName("历史待发布拍品").status(0).startPrice(5000)
                .endTime(submittedAt.plusDays(3)).build();
        auction.setCreateTime(submittedAt);
        when(auctionMapper.selectById(1L)).thenReturn(auction);
        when(auctionMapper.update(any(AuctionDO.class), any(UpdateWrapper.class))).thenReturn(1);

        service.bindGoofishAuction(7L, 1L, "1079935170548");

        ArgumentCaptor<AuctionDO> captor = ArgumentCaptor.forClass(AuctionDO.class);
        verify(auctionMapper).update(captor.capture(), any(UpdateWrapper.class));
        LocalDateTime managedEndTime = captor.getValue().getEndTime();
        assertTrue(managedEndTime.isAfter(LocalDateTime.now().plusHours(71)));
        assertTrue(managedEndTime.isBefore(LocalDateTime.now().plusHours(73)));
    }

    @Test
    void parsesAuctionItemIdFromFullLink() {
        assertEquals("1667121973675845", AuctionServiceImpl.resolveGoofishAuctionItemId(
                "https://www.goofish.com/item?id=1667121973675845"));
    }

    @Test
    void confirmUnsoldRestoresCollectionWithoutCreatingSettlement() {
        AuctionMapper auctionMapper = mock(AuctionMapper.class);
        AuctionSettlementMapper settlementMapper = mock(AuctionSettlementMapper.class);
        CollectionMapper collectionMapper = mock(CollectionMapper.class);
        AuctionServiceImpl service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", auctionMapper);
        ReflectionTestUtils.setField(service, "auctionSettlementMapper", settlementMapper);
        ReflectionTestUtils.setField(service, "collectionMapper", collectionMapper);
        ReflectionTestUtils.setField(service, "subscribeMessageService", mock(AppSubscribeMessageService.class));
        when(auctionMapper.selectByIdForUpdate(1L)).thenReturn(AuctionDO.builder()
                .id(1L).sellerId(100L).collectionId(9L).collectionName("测试拍品").status(2).build());
        when(collectionMapper.selectByIdForUpdate(9L)).thenReturn(
                CollectionDO.builder().id(9L).userId(0L).tradeStatus(2).build());
        when(auctionMapper.update(any(AuctionDO.class), any(UpdateWrapper.class))).thenReturn(1);

        service.confirmUnsold(7L, 1L);

        verify(settlementMapper, never()).insert(any(AuctionSettlementDO.class));
        ArgumentCaptor<CollectionDO> collectionCaptor = ArgumentCaptor.forClass(CollectionDO.class);
        verify(collectionMapper).updateById(collectionCaptor.capture());
        assertEquals(100L, collectionCaptor.getValue().getUserId());
        assertEquals(0, collectionCaptor.getValue().getTradeStatus());
        ArgumentCaptor<AuctionDO> auctionCaptor = ArgumentCaptor.forClass(AuctionDO.class);
        verify(auctionMapper).update(auctionCaptor.capture(), any(UpdateWrapper.class));
        assertEquals(4, auctionCaptor.getValue().getStatus());
    }

    @Test
    void expiredAuctionAlwaysWaitsForManualResultAndDoesNotRestoreInventory() {
        AuctionMapper auctionMapper = mock(AuctionMapper.class);
        CollectionMapper collectionMapper = mock(CollectionMapper.class);
        AuctionServiceImpl service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", auctionMapper);
        ReflectionTestUtils.setField(service, "collectionMapper", collectionMapper);
        when(auctionMapper.selectByIdForUpdate(1L)).thenReturn(AuctionDO.builder()
                .id(1L).sellerId(100L).collectionId(9L).status(1).delistStatus(0)
                .goofishProductId("1079935170548").endTime(LocalDateTime.now().minusMinutes(1)).build());

        service.closeAuction(1L);

        ArgumentCaptor<AuctionDO> captor = ArgumentCaptor.forClass(AuctionDO.class);
        verify(auctionMapper).update(captor.capture(), any(UpdateWrapper.class));
        assertEquals(2, captor.getValue().getStatus());
        assertTrue(captor.getValue().getSyncError().contains("确认已售出或流拍"));
        verifyNoInteractions(collectionMapper);
    }

    @Test
    void getAuctionBackfillsHistoricalPicturesFromCollection() {
        AuctionMapper auctionMapper = mock(AuctionMapper.class);
        CollectionMapper collectionMapper = mock(CollectionMapper.class);
        AuctionServiceImpl service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", auctionMapper);
        ReflectionTestUtils.setField(service, "auctionSettlementMapper", mock(AuctionSettlementMapper.class));
        ReflectionTestUtils.setField(service, "collectionMapper", collectionMapper);
        when(auctionMapper.selectById(1L)).thenReturn(AuctionDO.builder()
                .id(1L).collectionId(9L).currentPrice(5000).picUrl(null).picUrls(List.of()).build());
        when(collectionMapper.selectByIds(anyCollection())).thenReturn(List.of(CollectionDO.builder()
                .id(9L).picUrls(List.of("https://img.example/cover.jpg", "https://img.example/back.jpg")).build()));

        AppAuctionRespVO result = service.getAuction(1L);

        assertEquals("https://img.example/cover.jpg", result.getPicUrl());
        assertEquals(List.of("https://img.example/cover.jpg", "https://img.example/back.jpg"), result.getPicUrls());
    }

    @Test
    void getAuctionPrefersSnapshotPicturesOverCollectionFallback() {
        AuctionMapper auctionMapper = mock(AuctionMapper.class);
        CollectionMapper collectionMapper = mock(CollectionMapper.class);
        AuctionServiceImpl service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", auctionMapper);
        ReflectionTestUtils.setField(service, "auctionSettlementMapper", mock(AuctionSettlementMapper.class));
        ReflectionTestUtils.setField(service, "collectionMapper", collectionMapper);
        when(auctionMapper.selectById(1L)).thenReturn(AuctionDO.builder()
                .id(1L).collectionId(9L).currentPrice(5000)
                .picUrls(List.of("https://img.example/snapshot.jpg")).build());

        AppAuctionRespVO result = service.getAuction(1L);

        assertEquals("https://img.example/snapshot.jpg", result.getPicUrl());
        assertEquals(List.of("https://img.example/snapshot.jpg"), result.getPicUrls());
        verify(collectionMapper, never()).selectByIds(anyCollection());
    }
}
