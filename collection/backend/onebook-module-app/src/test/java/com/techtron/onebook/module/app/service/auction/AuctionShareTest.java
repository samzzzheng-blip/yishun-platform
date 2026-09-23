package com.techtron.onebook.module.app.service.auction;

import com.techtron.onebook.module.app.controller.admin.auction.vo.AdminAuctionShareReqVO;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionDO;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionShareTest {
    @Test void acceptsOriginalShareAndRejectsWrongLinkTypes() {
        var req = new AdminAuctionShareReqVO();
        req.setShareText("【闲鱼】https://m.tb.cn/abc?tk=xyz 拍品分享");
        assertTrue(req.isShareValid());
        req.setShareText("#小程序://闲鱼/AQ8Fyn9vNCBpqUD");
        assertTrue(req.isShareValid());
        for (String invalid : new String[]{"#小程序://其他/abc", "#小程序://闲鱼/", "123456", "https://m.tb.cn.evil.com/abc"}) {
            req.setShareText(invalid);
            assertFalse(req.isShareValid());
        }
    }
    @Test void updatesShareWithoutChangingAuctionOrPhotos() {
        var mapper = mock(AuctionMapper.class);
        when(mapper.selectById(1L)).thenReturn(AuctionDO.builder().id(1L).build());
        var service = new AuctionServiceImpl();
        ReflectionTestUtils.setField(service, "auctionMapper", mapper);
        var req = new AdminAuctionShareReqVO();
        req.setId(1L);
        req.setShareText("【闲鱼】https://m.tb.cn/abc?tk=xyz 拍品分享");
        service.updateShareText(req);
        verify(mapper).updateById(argThat((AuctionDO row) -> req.getShareText().equals(row.getShareText())
                && row.getStatus() == null && row.getDisplayPicUrls() == null && row.getGoofishUrl() == null));
    }
}
