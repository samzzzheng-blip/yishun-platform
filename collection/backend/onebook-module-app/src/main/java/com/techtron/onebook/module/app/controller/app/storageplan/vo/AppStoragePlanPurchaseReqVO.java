package com.techtron.onebook.module.app.controller.app.storageplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "客户端 - 寄存容量套餐购买 Request VO")
@Data
public class AppStoragePlanPurchaseReqVO {

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "套餐ID不能为空")
    private Long planId;

    @Schema(description = "购买类型：1-月费，2-年费", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "购买类型不能为空")
    private Integer type;

}
