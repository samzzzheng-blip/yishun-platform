package com.techtron.onebook.module.app.controller.admin.dealorder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.techtron.onebook.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.techtron.onebook.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 批量交易成交分页 Request VO")
@Data
public class DealOrderPageReqVO extends PageParam {

    @Schema(description = "藏品分类id", example = "2763")
    private Long categoryId;

    @Schema(description = "数量")
    private Integer amount;

    @Schema(description = "价格，单位：分", example = "10799")
    private Integer price;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}