package com.techtron.onebook.module.app.controller.admin.stonerecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 能量石记录 Response VO")
@Data
public class StoneRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer amount;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    private Integer balance;

}
