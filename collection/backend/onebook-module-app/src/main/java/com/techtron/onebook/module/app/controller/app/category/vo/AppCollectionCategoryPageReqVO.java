package com.techtron.onebook.module.app.controller.app.category.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 藏品分类分页 Request VO")
@Data
public class AppCollectionCategoryPageReqVO extends PageParam {

    @Schema(description = "用户id", example = "14726")
    private Long userId;

}