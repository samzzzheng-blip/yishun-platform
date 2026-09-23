package com.techtron.onebook.module.app.controller.admin.collection.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 藏品变更记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CollectionRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "用户id", example = "5458")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "手机号", example = "13800138000")
    @ExcelProperty("手机号")
    private String mobile;

    @Schema(description = "藏品分类id", example = "24845")
    @ExcelProperty("藏品分类id")
    private Long categoryId;

    @Schema(description = "藏品分类名称", example = "字画")
    @ExcelProperty("藏品分类名称")
    private String categoryName;

    @Schema(description = "数量")
    @ExcelProperty("数量")
    private Integer amount;

    @Schema(description = "类型：1-用户新增 2-管理员新增 3-删除 4-兑换能量石 5-买 6-卖 7-取回")
    @ExcelProperty("类型")
    private Integer type;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}