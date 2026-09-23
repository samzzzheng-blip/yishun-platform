package com.techtron.onebook.module.app.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "管理后台 - 藏品分类新增/修改 Request VO")
@Data
public class CollectionCategorySaveReqVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27522")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "分类名称不能为空")
    private String name;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "8230")
    private Long userId;

    private Long copyId;

    private String picUrl;

    private Integer exchangeRate;

}