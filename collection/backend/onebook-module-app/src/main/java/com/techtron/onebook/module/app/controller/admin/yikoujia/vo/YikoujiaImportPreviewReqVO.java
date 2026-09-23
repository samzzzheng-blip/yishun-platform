package com.techtron.onebook.module.app.controller.admin.yikoujia.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 闲鱼商品导入预览 Request VO")
@Data
public class YikoujiaImportPreviewReqVO {

    @Schema(description = "闲鱼商品链接或商品 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请输入闲鱼商品链接或商品ID")
    private String source;

}
