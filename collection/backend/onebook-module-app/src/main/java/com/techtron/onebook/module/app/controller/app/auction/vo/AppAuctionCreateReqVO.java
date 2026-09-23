package com.techtron.onebook.module.app.controller.app.auction.vo;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppAuctionCreateReqVO {
    @NotNull private Long collectionId;
    @NotNull(message = "起拍价必须为1元")
    @Min(value = 100, message = "起拍价必须为1元")
    @Max(value = 100, message = "起拍价必须为1元")
    private Integer startPrice;
    @NotNull @Min(1) @Max(100000000) private Integer minIncrement;
    @NotNull @Future private LocalDateTime endTime;
}
