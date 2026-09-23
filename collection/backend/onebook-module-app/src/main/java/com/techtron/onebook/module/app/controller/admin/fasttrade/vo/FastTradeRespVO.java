package com.techtron.onebook.module.app.controller.admin.fasttrade.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.techtron.onebook.framework.excel.core.annotations.DictFormat;
import com.techtron.onebook.framework.excel.core.convert.DictConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 快速变现 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FastTradeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11958")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "9084")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "藏品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "7691")
    @ExcelProperty("藏品id")
    private Long collectionId;

    @Schema(description = "数量")
    @ExcelProperty("数量")
    private Integer amount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("collection_trade_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    private String picUrl;

    private Integer price;

    private List<FastTradeItemVO> items;

}
