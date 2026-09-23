package com.techtron.onebook.module.app.controller.admin.getback.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 取回新增/修改 Request VO")
@Data
public class GetbackUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31577")
    private Long id;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    private String deliverCode;

    private String expressCompany;

    private Long userId;

}