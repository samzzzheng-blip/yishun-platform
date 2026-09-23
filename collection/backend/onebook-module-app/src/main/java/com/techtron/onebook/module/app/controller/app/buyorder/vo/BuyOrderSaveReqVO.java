package com.techtron.onebook.module.app.controller.app.buyorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 批量交易买单新增/修改 Request VO")
@Data
public class BuyOrderSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30278")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "21315")
    private Long userId;

    @Schema(description = "藏品分类id", requiredMode = Schema.RequiredMode.REQUIRED, example = "3008")
    @NotNull(message = "藏品分类id不能为空")
    private Long categoryId;

    @Schema(description = "剩余未成交数量")
    private Integer amount;

    @Schema(description = "交易数量")
    private Integer dealAmount;

    @Schema(description = "价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "20287")
    @NotNull(message = "价格，单位：分不能为空")
    private Integer price;

    private Integer dealPrice;

}