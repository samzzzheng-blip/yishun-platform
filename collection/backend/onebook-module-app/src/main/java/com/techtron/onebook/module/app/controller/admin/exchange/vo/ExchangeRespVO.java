package com.techtron.onebook.module.app.controller.admin.exchange.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.techtron.onebook.module.app.dal.dataobject.exchange.ExchangeQuestionDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 兑换品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ExchangeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12970")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "图片", example = "https://www.iocoder.cn")
    @ExcelProperty("图片")
    private String picUrl;

    @Schema(description = "兑换品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("兑换品名称")
    private String name;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "库存")
    @ExcelProperty("库存")
    private Integer stock;

    @Schema(description = "兑换数量")
    @ExcelProperty("兑换数量")
    private Integer amount;

    private Integer ordinalPosition;

    private List<ExchangeQuestionDO> questions;


}
