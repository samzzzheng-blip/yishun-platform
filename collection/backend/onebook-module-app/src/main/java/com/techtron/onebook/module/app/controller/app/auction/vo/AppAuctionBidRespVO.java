package com.techtron.onebook.module.app.controller.app.auction.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppAuctionBidRespVO {
    private Long bidId;
    private Integer currentPrice;
    private Integer nextBidPrice;
    private Integer bidCount;
}
