package com.techtron.onebook.module.app.controller.app.yikoujia.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "一口价新增/修改 Request VO")
@Data
public class AppYikoujiaSaveReqVO {
    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "12449")
    private Long userId;

    @Schema(description = "藏品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "7691")
    private List<Long> collectionIds;

    @Schema(description = "价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "18643")
    @NotNull(message = "价格，单位：分不能为空")
    private Integer price;

}