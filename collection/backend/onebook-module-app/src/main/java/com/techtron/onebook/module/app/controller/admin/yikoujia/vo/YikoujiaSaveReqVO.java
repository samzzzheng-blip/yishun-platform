package com.techtron.onebook.module.app.controller.admin.yikoujia.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 一口价新增/修改 Request VO")
@Data
public class YikoujiaSaveReqVO {
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "132")
    private Long id;

    @Schema(description = "状态（0待交易 1待结算 2已结算）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    private String productId;

    private List<String> picUrl;

    private String name;

    private Integer price;

    private String introduction;
}