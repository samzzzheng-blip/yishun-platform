package com.techtron.onebook.module.app.controller.app.auction.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppAuctionRespVO {
    private String shareText;
    private java.util.List<String> displayPicUrls;
    private Long id;
    private Long sellerId;
    private Long collectionId;
    private String collectionName;
    private String categoryName;
    private String picUrl;
    private List<String> picUrls;
    private Integer amount;
    private Integer startPrice;
    private Integer minIncrement;
    private Integer currentPrice;
    private Integer nextBidPrice;
    private Long highestBidderId;
    private Integer bidCount;
    private String goofishProductId;
    private String goofishManagedProductId;
    private String goofishDetail;
    private String goofishUrl;
    private String syncError;
    private LocalDateTime lastSyncTime;
    private Integer status;
    private LocalDateTime reviewTime;
    private String reviewRejectReason;
    private Integer delistStatus;
    private LocalDateTime delistApplyTime;
    private LocalDateTime delistAuditTime;
    private String delistRejectReason;
    private LocalDateTime endTime;
    private LocalDateTime settledTime;
    private Integer settlementStatus;
    private Integer grossAmount;
    private Integer feeRate;
    private Integer feeAmount;
    private Integer sellerIncome;
    private LocalDateTime createTime;
}
