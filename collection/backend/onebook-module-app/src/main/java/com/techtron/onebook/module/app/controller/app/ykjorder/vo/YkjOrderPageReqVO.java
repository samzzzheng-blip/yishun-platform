package com.techtron.onebook.module.app.controller.app.ykjorder.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.techtron.onebook.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 一口价买单分页 Request VO")
@Data
public class YkjOrderPageReqVO extends PageParam {

    @Schema(description = "用户id", example = "14233")
    private Long userId;

    @Schema(description = "一口价商品id", example = "25496")
    private Long ykjId;

    @Schema(description = "支付状态（0未支付，1已支付）", example = "1")
    private Integer status;

    @Schema(description = "支付订单编号", example = "6667")
    private Long payOrderId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}