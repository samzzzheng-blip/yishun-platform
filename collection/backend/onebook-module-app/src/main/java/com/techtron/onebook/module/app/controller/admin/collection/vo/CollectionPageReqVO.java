package com.techtron.onebook.module.app.controller.admin.collection.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 藏品登记分页 Request VO")
@Data
public class CollectionPageReqVO extends PageParam {

    @Schema(description = "品名", example = "赵六")
    private String name;

    @Schema(description = "用户id", example = "5458")
    private Long userId;

    @Schema(description = "分类id", example = "24845")
    private Long categoryId;

    @Schema(description = "状态", example = "1")
    private Integer status;

    private String userName;

}