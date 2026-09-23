package com.techtron.onebook.module.app.controller.admin.yikoujia.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 确认导入闲鱼商品 Request VO")
@Data
public class YikoujiaImportReqVO {

    @Schema(description = "闲鱼商品链接或商品 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请输入闲鱼商品链接或商品ID")
    private String source;

    @Schema(description = "是否为平台自营商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean selfOperated;

    @Schema(description = "商品所属会员 ID；非自营商品时必填")
    private Long userId;

}
