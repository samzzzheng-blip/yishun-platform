package com.techtron.onebook.module.app.controller.admin.exchangelog.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.techtron.onebook.framework.excel.core.annotations.DictFormat;
import com.techtron.onebook.framework.excel.core.convert.DictConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 兑换记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ExchangeLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28096")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "兑换品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "213")
    @ExcelProperty("兑换品id")
    private Long exchangeId;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "3207")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "兑换品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("兑换品名称")
    private String exchangeName;

    @Schema(description = "兑换数量")
    @ExcelProperty("兑换数量")
    private Integer amount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("exchange_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "手机号")
    @ExcelProperty("手机号")
    private String mobile;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    private String answer;

    private String deliverCode;

}
