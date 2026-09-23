package com.techtron.onebook.module.app.service.auction;

import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionDO;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionMapper;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionSettlementMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuctionDisplayPhotosTest {
    private final AuctionMapper mapper = mock(AuctionMapper.class);
    private final AuctionServiceImpl service = new AuctionServiceImpl();
    AuctionDisplayPhotosTest() {
        ReflectionTestUtils.setField(service, "auctionMapper", mapper);
        ReflectionTestUtils.setField(service, "auctionSettlementMapper", mock(AuctionSettlementMapper.class));
        when(mapper.selectById(1L)).thenReturn(AuctionDO.builder().id(1L).currentPrice(100)
                .picUrl("https://example.com/original.jpg").build());
    }
    @Test void savesOrderedPhotosWithoutChangingOriginalOrStatus() {
        var images = List.of("https://example.com/b.jpg", "https://example.com/a.jpg");
        service.updateDisplayPhotos(1L, images);
        verify(mapper).updateById(argThat((AuctionDO row) -> images.equals(row.getDisplayPicUrls())
                && row.getPicUrls() == null && row.getPicUrl() == null && row.getStatus() == null));
    }
    @Test void invalidPhotosCannotBeSaved() {
        assertThrows(IllegalArgumentException.class, () -> service.updateDisplayPhotos(1L, List.of()));
        assertThrows(IllegalArgumentException.class, () -> service.updateDisplayPhotos(1L, List.of("javascript:bad")));
        assertThrows(IllegalArgumentException.class, () -> service.updateDisplayPhotos(1L,
                java.util.Collections.nCopies(10, "https://example.com/a.jpg")));
        verify(mapper, never()).updateById(any(AuctionDO.class));
    }
    @Test void responseSeparatesOriginalAndDisplayPhotos() {
        var original = mapper.selectById(1L);
        original.setDisplayPicUrls(List.of("https://example.com/display.jpg"));
        var response = service.getAuction(1L);
        assertEquals(original.getDisplayPicUrls(), response.getDisplayPicUrls());
        assertEquals("https://example.com/original.jpg", response.getPicUrl());
    }
    @Test void missingDisplayPhotosDoesNotCopyOriginalIntoDisplayField() {
        assertNull(service.getAuction(1L).getDisplayPicUrls());
    }
}
