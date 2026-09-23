package com.techtron.onebook.module.app.controller.app.storageplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "客户端 - 寄存容量套餐 Response VO")
@Data
public class AppStoragePlanRespVO {

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "藏品数量下限", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer minCount;

    @Schema(description = "藏品数量上限", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    private Integer maxCount;

    @Schema(description = "月费（能量石）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer monthlyPrice;

    @Schema(description = "年费（能量石）", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer yearlyPrice;

}