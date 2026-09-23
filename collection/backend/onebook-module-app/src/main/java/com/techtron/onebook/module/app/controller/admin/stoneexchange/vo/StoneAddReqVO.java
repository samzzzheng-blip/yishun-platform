package com.techtron.onebook.module.app.controller.admin.stoneexchange.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 能量石兑换新增/修改 Request VO")
@Data
public class StoneAddReqVO {

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "15166")
    private String userId;

    private Integer amount;

}