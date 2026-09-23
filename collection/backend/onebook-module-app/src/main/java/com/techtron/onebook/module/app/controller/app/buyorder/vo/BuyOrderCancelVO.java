package com.techtron.onebook.module.app.controller.app.buyorder.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 批量交易买单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BuyOrderCancelVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30278")
    @ExcelProperty("编号")
    private Long id;

    private Long userId;

}
