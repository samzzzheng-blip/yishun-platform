package com.techtron.onebook.module.app.controller.app.sellorder.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.techtron.onebook.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 批量交易卖单分页 Request VO")
@Data
public class SellOrderPageReqVO extends PageParam {

    @Schema(description = "用户id", example = "27589")
    private Long userId;

    @Schema(description = "用户手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "藏品分类id", requiredMode = Schema.RequiredMode.REQUIRED, example = "3008")
    private Long categoryId;

    @Schema(description = "剩余未成交数量")
    private Integer amount;

    @Schema(description = "交易数量")
    private Integer dealAmount;

    @Schema(description = "价格，单位：分", example = "30988")
    private Integer price;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}