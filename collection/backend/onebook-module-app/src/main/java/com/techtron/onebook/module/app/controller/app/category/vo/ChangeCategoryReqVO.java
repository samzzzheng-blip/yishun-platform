package com.techtron.onebook.module.app.controller.app.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 APP - 批量修改藏品分类 Request VO")
@Data
public class ChangeCategoryReqVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "分类编号不能为空")
    private Long categoryId;

    @Schema(description = "藏品编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "藏品编号列表不能为空")
    private List<Long> collectionIds;

}