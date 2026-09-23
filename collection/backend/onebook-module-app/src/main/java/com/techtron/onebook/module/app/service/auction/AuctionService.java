package com.techtron.onebook.module.app.service.auction;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.app.auction.vo.*;
public interface AuctionService {
    void bindManagedProduct(Long adminUserId, Long auctionId, String productId);
    void updateShareText(com.techtron.onebook.module.app.controller.admin.auction.vo.AdminAuctionShareReqVO req);
    void updateDisplayPhotos(Long auctionId, java.util.List<String> photos);
    Long createAuction(Long sellerId, AppAuctionCreateReqVO req);
    void cancelAuction(Long sellerId, Long auctionId);
    void requestDelist(Long sellerId, Long auctionId);
    void bindGoofishAuction(Long adminUserId, Long auctionId, String goofishUrl);
    void rejectPublish(Long adminUserId, Long auctionId, String reason);
    void approveDelist(Long adminUserId, Long auctionId);
    void rejectDelist(Long adminUserId, Long auctionId, String reason);
    void confirmSale(Long adminUserId, Long auctionId, Integer grossAmount, String remark);
    void confirmUnsold(Long adminUserId, Long auctionId);
    AppAuctionRespVO getAuction(Long id);
    PageResult<AppAuctionRespVO> getAuctionPage(AppAuctionPageReqVO req);
    PageResult<AppAuctionRespVO> getMyAuctionPage(Long userId, AppAuctionPageReqVO req);
    int closeExpiredAuctions(int limit);
    int syncManagedAuctions(int limit);
    void closeAuction(Long auctionId);
    void syncAuction(Long auctionId);
}
