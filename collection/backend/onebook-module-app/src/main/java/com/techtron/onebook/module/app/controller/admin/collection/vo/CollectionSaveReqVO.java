package com.techtron.onebook.module.app.controller.admin.collection.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 藏品登记新增/修改 Request VO")
@Data
public class CollectionSaveReqVO {

    @Schema(description = "藏品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12422")
    private Long id;

    @Schema(description = "品名", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "品名不能为空")
    private String name;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "5458")
    private Long userId;

    @Schema(description = "分类id", requiredMode = Schema.RequiredMode.REQUIRED, example = "24845")
    @NotNull(message = "分类id不能为空")
    private Long categoryId;

    @Schema(description = "分类名", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    private String categoryName;

    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    private String picUrl;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "数量")
    private Integer stock;

    private Integer realStock;

    private List<String> picUrls;

}