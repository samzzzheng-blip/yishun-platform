package com.techtron.onebook.module.app.controller.admin.exchangelog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 兑换记录新增/修改 Request VO")
@Data
public class ExchangeLogSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28096")
    private Long id;

    @Schema(description = "兑换品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "213")
    private Long exchangeId;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "3207")
    private Long userId;

    @Schema(description = "兑换品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String exchangeName;

    @Schema(description = "兑换数量")
    private Integer amount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

    @Schema(description = "手机号")
    private String mobile;

    private String answer;

    private String deliverCode;

}