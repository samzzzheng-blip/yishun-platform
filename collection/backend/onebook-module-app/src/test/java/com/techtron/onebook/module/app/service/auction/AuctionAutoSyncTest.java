package com.techtron.onebook.module.app.service.auction;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionMapper;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionDO;
import com.techtron.onebook.module.app.job.AuctionCloseJob;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class AuctionAutoSyncTest {
    @Test void jobRefreshesManagedItemsAndStillClosesExpiredItems() {
        var service = mock(AuctionService.class);
        var job = new AuctionCloseJob();
        ReflectionTestUtils.setField(job, "auctionService", service);
        job.execute("");
        verify(service).syncManagedAuctions(50);
        verify(service).closeExpiredAuctions(100);
    }
    @Test void badItemDoesNotBlockRemainingItemsAndBatchIsBounded() {
        var mapper = mock(AuctionMapper.class);
        var self = mock(AuctionService.class);
        var service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", mapper);
        ReflectionTestUtils.setField(service, "self", self);
        when(mapper.selectManagedSyncIds(eq(50), any(java.time.LocalDateTime.class))).thenReturn(List.of(new AuctionDO().setId(1L), new AuctionDO().setId(2L)));
        doThrow(new IllegalStateException()).when(self).closeAuction(1L);
        assertEquals(1, service.syncManagedAuctions(100));
        verify(self).closeAuction(2L);
        verify(mapper).selectManagedSyncIds(eq(50), argThat(now ->
                java.time.Duration.between(now, java.time.LocalDateTime.now()).abs().getSeconds() < 5));
    }
}
