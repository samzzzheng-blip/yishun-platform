package com.techtron.onebook.module.app.controller.admin.stoneexchange.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 能量石兑换新增/修改 Request VO")
@Data
public class StoneExchangeSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1732")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "15166")
    private Long userId;

    private List<Long> collectionIds;

}