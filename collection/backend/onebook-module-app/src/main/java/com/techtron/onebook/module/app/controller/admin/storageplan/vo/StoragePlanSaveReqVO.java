package com.techtron.onebook.module.app.controller.admin.storageplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 寄存容量套餐新增/修改 Request VO")
@Data
public class StoragePlanSaveReqVO {

    @Schema(description = "套餐ID", example = "1")
    private Long id;

    @Schema(description = "藏品数量下限", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "藏品数量下限不能为空")
    private Integer minCount;

    @Schema(description = "藏品数量上限", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    @NotNull(message = "藏品数量上限不能为空")
    private Integer maxCount;

    @Schema(description = "月费（能量石）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "月费不能为空")
    private Integer monthlyPrice;

    @Schema(description = "年费（能量石）", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    @NotNull(message = "年费不能为空")
    private Integer yearlyPrice;

}