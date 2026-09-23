package com.techtron.onebook.module.app.controller.admin.ads.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 广告新增/修改 Request VO")
@Data
public class AdsSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8084")
    private Long id;

    @Schema(description = "图片地址", example = "https://www.iocoder.cn")
    private String picUrl;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

    /** 广告跳转的一口价商品编号，空表示不跳转 */
    private Long targetProductId;

}