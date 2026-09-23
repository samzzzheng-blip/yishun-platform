package com.techtron.onebook.module.app.controller.app.auction.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/** Public bid record. Internal user identifiers are deliberately excluded. */
@Data
@AllArgsConstructor
public class AppAuctionBidRecordRespVO {
    private Integer amount;
    private Integer status;
    private LocalDateTime createTime;
}
