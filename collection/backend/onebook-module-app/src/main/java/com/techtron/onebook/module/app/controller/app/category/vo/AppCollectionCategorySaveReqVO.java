package com.techtron.onebook.module.app.controller.app.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "用户 APP - 藏品分类新增/修改 Request VO")
@Data
public class AppCollectionCategorySaveReqVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20103")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "分类名称不能为空")
    private String name;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "14726")
    @NotNull(message = "用户id不能为空")
    private Long userId;

}