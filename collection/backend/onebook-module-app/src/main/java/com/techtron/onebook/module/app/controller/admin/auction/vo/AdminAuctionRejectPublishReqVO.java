package com.techtron.onebook.module.app.controller.admin.auction.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminAuctionRejectPublishReqVO {
    @NotNull
    private Long id;

    @NotBlank(message = "请填写驳回原因")
    @Size(max = 200, message = "驳回原因不能超过200字")
    private String reason;
}
