package com.techtron.onebook.module.app.controller.admin.stoneexchange.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 能量石兑换 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StoneExchangeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1732")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "15166")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    private List<StoneExchangeItemVO> items;

}
