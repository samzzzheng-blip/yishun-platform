package com.techtron.onebook.module.app.controller.admin.exchange.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.techtron.onebook.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.techtron.onebook.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 兑换品分页 Request VO")
@Data
public class ExchangePageReqVO extends PageParam {

    @Schema(description = "兑换品名称", example = "张三")
    private String name;

}