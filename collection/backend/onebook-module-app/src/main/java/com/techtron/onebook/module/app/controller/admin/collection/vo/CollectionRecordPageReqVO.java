package com.techtron.onebook.module.app.controller.admin.collection.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 藏品变更记录分页 Request VO")
@Data
public class CollectionRecordPageReqVO extends PageParam {

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "藏品分类id", example = "24845")
    private Long categoryId;

    @Schema(description = "类型", example = "1")
    private Integer type;

}