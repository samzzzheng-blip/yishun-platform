package com.techtron.onebook.module.app.controller.admin.ads.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 广告 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AdsRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8084")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "图片地址", example = "https://www.iocoder.cn")
    @ExcelProperty("图片地址")
    private String picUrl;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态")
    private Integer status;

    /** 广告跳转的一口价商品编号，空表示不跳转 */
    private Long targetProductId;

    private String targetProductName;

}
