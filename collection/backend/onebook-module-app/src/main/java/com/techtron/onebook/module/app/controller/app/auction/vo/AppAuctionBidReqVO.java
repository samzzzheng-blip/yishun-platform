package com.techtron.onebook.module.app.controller.app.auction.vo;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AppAuctionBidReqVO {
    @NotNull private Long auctionId;
    @NotNull @Min(1) @Max(100000000) private Integer amount;
    @NotBlank @Size(max = 64) private String requestId;
}
