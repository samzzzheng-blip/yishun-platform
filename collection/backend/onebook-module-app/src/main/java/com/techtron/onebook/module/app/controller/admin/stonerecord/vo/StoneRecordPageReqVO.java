package com.techtron.onebook.module.app.controller.admin.stonerecord.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 能量石记录分页 Request VO")
@Data
public class StoneRecordPageReqVO extends PageParam {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "类型")
    private Integer type;

}
