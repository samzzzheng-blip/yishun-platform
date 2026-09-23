package com.techtron.onebook.module.app.controller.admin.dealorder.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 批量交易成交 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DealOrderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4935")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "藏品分类id", requiredMode = Schema.RequiredMode.REQUIRED, example = "2763")
    @ExcelProperty("藏品分类id")
    private Long categoryId;

    @Schema(description = "数量")
    @ExcelProperty("数量")
    private Integer amount;

    @Schema(description = "价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10799")
    @ExcelProperty("价格，单位：分")
    private Integer price;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
