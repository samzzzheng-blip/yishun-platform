package com.techtron.onebook.module.app.controller.admin.auction.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminAuctionConfirmSaleReqVO {
    @NotNull
    private Long id;

    /** 闲鱼订单实际已付款金额，单位：分 */
    @NotNull
    @Min(1)
    @Max(100_000_000)
    private Integer grossAmount;

    /** 二次追溯所需的人工核对说明，例如闲鱼订单号后四位 */
    @NotBlank
    @Size(max = 200)
    private String remark;
}
