package com.techtron.onebook.module.app.service.auction;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionDO;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionMapper;
import com.techtron.onebook.module.app.service.yikoujia.YikoujiaService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionManagedSyncTest {
    private final AuctionMapper mapper = mock(AuctionMapper.class);
    private final YikoujiaService api = mock(YikoujiaService.class);
    private final AuctionServiceImpl service = new AuctionServiceImpl();
    AuctionManagedSyncTest() {
        ReflectionTestUtils.setField(service, "auctionMapper", mapper);
        ReflectionTestUtils.setField(service, "yikoujiaService", api);
        when(mapper.selectByIdForUpdate(5L)).thenReturn(AuctionDO.builder().id(5L).status(1)
                .goofishProductId("1080821222235").goofishManagedProductId("1746190442089157")
                .endTime(LocalDateTime.now().plusDays(1)).build());
    }
    private AuctionDO synced() {
        var captor = ArgumentCaptor.forClass(AuctionDO.class);
        verify(mapper).update(captor.capture(), any(UpdateWrapper.class));
        return captor.getValue();
    }
    @Test void syncsPhotosButNeverOverwritesAuctionPriceOrBidCount() throws Exception {
        when(api.requestGoofishProductDetail(anyString())).thenReturn(AuctionProductSnapshotTest.fixture());
        service.closeAuction(5L);
        var update = synced();
        assertNotNull(update.getGoofishDetail());
        assertEquals(1, update.getDisplayPicUrls().size());
        assertNull(update.getCurrentPrice());
        assertNull(update.getBidCount());
        assertNull(update.getStatus());
    }
    @Test void failurePreservesPriorSnapshotAndPictures() {
        when(api.requestGoofishProductDetail(anyString())).thenThrow(new IllegalStateException("unavailable"));
        service.closeAuction(5L);
        var update = synced();
        assertNull(update.getGoofishDetail());
        assertNull(update.getDisplayPicUrls());
        assertNull(update.getStatus());
        assertTrue(update.getSyncError().contains("失败"));
    }
    @Test void soldInformationOnlyEntersHumanConfirmation() throws Exception {
        when(api.requestGoofishProductDetail(anyString())).thenReturn(AuctionProductSnapshotTest.fixture().put("sold", 1));
        service.closeAuction(5L);
        var update = synced();
        assertEquals(2, update.getStatus());
        assertNull(update.getCurrentPrice());
    }
    @Test void bindingExistingAuctionPersistsSeparateManagedId() throws Exception {
        when(api.requestGoofishProductDetail(anyString())).thenReturn(AuctionProductSnapshotTest.fixture());
        service.bindManagedProduct(1L, 5L, "1746190442089157");
        var captor = ArgumentCaptor.forClass(AuctionDO.class);
        verify(mapper).updateById(captor.capture());
        assertEquals("1746190442089157", captor.getValue().getGoofishManagedProductId());
        assertNull(captor.getValue().getGoofishProductId());
    }
}
