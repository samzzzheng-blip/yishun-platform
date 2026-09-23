package com.techtron.onebook.module.app.controller.admin.collection.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 藏品登记 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CollectionRespVO {

    @Schema(description = "藏品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12422")
    @ExcelProperty("藏品编号")
    private Long id;

    @Schema(description = "品名", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("品名")
    private String name;

    @Schema(description = "分类名", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("分类名")
    private String categoryName;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "5458")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "分类id", requiredMode = Schema.RequiredMode.REQUIRED, example = "24845")
    @ExcelProperty("分类id")
    private Long categoryId;

    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @ExcelProperty("图片地址")
    private String picUrl;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "数量")
    @ExcelProperty("数量")
    private Integer stock;

    private String mobile;

    private String creatorUserName;

    private Long creator;

    private Integer tradeStatus;

    private Integer getbackStatus;

    private List<String> picUrls;

}
