package com.techtron.onebook.module.app.controller.admin.fasttrade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 快速变现新增/修改 Request VO")
@Data
public class FastTradeSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11958")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "9084")
    private Long userId;

    @Schema(description = "藏品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "7691")
    private List<Long> collectionIds;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

    private Integer price;

}