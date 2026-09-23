package com.techtron.onebook.module.app.controller.admin.dealorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 批量交易成交新增/修改 Request VO")
@Data
public class DealOrderSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4935")
    private Long id;

    @Schema(description = "藏品分类id", requiredMode = Schema.RequiredMode.REQUIRED, example = "2763")
    @NotNull(message = "藏品分类id不能为空")
    private Long categoryId;

    @Schema(description = "数量")
    private Integer amount;

    @Schema(description = "价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10799")
    @NotNull(message = "价格，单位：分不能为空")
    private Integer price;

}