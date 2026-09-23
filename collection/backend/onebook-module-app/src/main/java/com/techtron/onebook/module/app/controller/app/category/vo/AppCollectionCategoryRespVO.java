package com.techtron.onebook.module.app.controller.app.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import cn.idev.excel.annotation.*;

@Schema(description = "用户 APP - 藏品分类 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AppCollectionCategoryRespVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20103")
    @ExcelProperty("分类编号")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("分类名称")
    private String name;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "14726")
    @ExcelProperty("用户id")
    private Long userId;

    private Long copyId;

    private Integer stock;

    private String picUrl;

    private Integer tradeStatus;

    private Integer getbackStatus;

}
