package com.techtron.onebook.module.app.controller.admin.storageplan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 寄存容量套餐 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StoragePlanRespVO {

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("套餐ID")
    private Long id;

    @Schema(description = "藏品数量下限", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("藏品数量下限")
    private Integer minCount;

    @Schema(description = "藏品数量上限", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    @ExcelProperty("藏品数量上限")
    private Integer maxCount;

    @Schema(description = "月费（能量石）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @ExcelProperty("月费")
    private Integer monthlyPrice;

    @Schema(description = "年费（能量石）", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    @ExcelProperty("年费")
    private Integer yearlyPrice;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

}