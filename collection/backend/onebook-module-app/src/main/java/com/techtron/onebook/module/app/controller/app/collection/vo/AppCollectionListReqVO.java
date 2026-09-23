package com.techtron.onebook.module.app.controller.app.collection.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 藏品列表Request VO")
@Data
public class AppCollectionListReqVO {

    @Schema(description = "用户id", example = "5458")
    private Long userId;

    @Schema(description = "分类id", example = "24845")
    private Long categoryId;

    @Schema(description = "状态", example = "1")
    private Integer status;

    private Integer noEqStatus;

    private List<Long> collectionIds;

    private String collectionName;
}