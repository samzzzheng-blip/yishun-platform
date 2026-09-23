package com.techtron.onebook.module.app.controller.app.sellorder.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 批量交易卖单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SellOrderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6528")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "27589")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "藏品分类id", requiredMode = Schema.RequiredMode.REQUIRED, example = "3008")
    @ExcelProperty("藏品分类id")
    private Long categoryId;

    @Schema(description = "剩余未成交数量")
    @ExcelProperty("剩余未成交数量")
    private Integer amount;

    @Schema(description = "交易数量")
    @ExcelProperty("交易数量")
    private Integer dealAmount;

    @Schema(description = "价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "30988")
    @ExcelProperty("价格，单位：分")
    private Integer price;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "用户手机号")
    @ExcelProperty("用户手机号")
    private String mobile;

    @Schema(description = "分类名称")
    @ExcelProperty("分类名称")
    private String categoryName;

}
