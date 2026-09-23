package com.techtron.onebook.module.app.controller.admin.exchangelog.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 兑换记录分页 Request VO")
@Data
public class ExchangeLogPageReqVO extends PageParam {

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "手机号")
    private String mobile;

    private Long userId;

}