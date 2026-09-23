package com.techtron.onebook.module.app.controller.admin.exchange.vo;

import com.techtron.onebook.module.app.dal.dataobject.exchange.ExchangeQuestionDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 兑换品新增/修改 Request VO")
@Data
public class ExchangeSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12970")
    private Long id;

    @Schema(description = "图片", example = "https://www.iocoder.cn")
    private String picUrl;

    @Schema(description = "兑换品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "兑换品名称不能为空")
    private String name;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "兑换数量")
    private Integer amount;

    private Integer ordinalPosition;

    private List<ExchangeQuestionDO> questions;

}