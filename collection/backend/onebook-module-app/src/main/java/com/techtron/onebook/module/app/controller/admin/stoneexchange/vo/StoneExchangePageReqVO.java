package com.techtron.onebook.module.app.controller.admin.stoneexchange.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.techtron.onebook.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.techtron.onebook.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 能量石兑换分页 Request VO")
@Data
public class StoneExchangePageReqVO extends PageParam {

}