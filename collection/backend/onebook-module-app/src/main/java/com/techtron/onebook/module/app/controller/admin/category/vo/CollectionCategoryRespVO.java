package com.techtron.onebook.module.app.controller.admin.category.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 藏品分类 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CollectionCategoryRespVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27522")
    @ExcelProperty("分类编号")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("分类名称")
    private String name;

    private String picUrl;

    private Integer exchangeRate;


}
