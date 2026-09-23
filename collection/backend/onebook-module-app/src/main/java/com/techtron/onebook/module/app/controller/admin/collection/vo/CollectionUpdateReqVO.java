package com.techtron.onebook.module.app.controller.admin.collection.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 藏品登记审核Request VO")
@Data
public class CollectionUpdateReqVO {

    @Schema(description = "藏品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12422")
    private Long id;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "数量")
    private Integer stock;

}