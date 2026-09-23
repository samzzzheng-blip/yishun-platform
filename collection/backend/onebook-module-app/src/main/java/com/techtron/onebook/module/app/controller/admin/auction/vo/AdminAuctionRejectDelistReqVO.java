package com.techtron.onebook.module.app.controller.admin.auction.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminAuctionRejectDelistReqVO {

    @NotNull
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String reason;
}
