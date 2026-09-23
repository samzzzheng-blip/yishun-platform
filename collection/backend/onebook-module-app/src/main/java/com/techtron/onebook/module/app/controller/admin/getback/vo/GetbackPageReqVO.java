package com.techtron.onebook.module.app.controller.admin.getback.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.techtron.onebook.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.techtron.onebook.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 取回分页 Request VO")
@Data
public class GetbackPageReqVO extends PageParam {

    @Schema(description = "状态", example = "1")
    private Integer status;

}