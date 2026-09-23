package com.techtron.onebook.module.app.controller.app.ykjorder.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 一口价买单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class YkjOrderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15044")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "14233")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "一口价商品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "25496")
    @ExcelProperty("一口价商品id")
    private Long ykjId;

    @Schema(description = "支付状态（0未支付，1已支付）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("支付状态（0未支付，1已支付）")
    private Integer status;

    @Schema(description = "支付订单编号", example = "6667")
    @ExcelProperty("支付订单编号")
    private Long payOrderId;

    @Schema(description = "成交金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "19900")
    private Integer price;

    /** WAREHOUSE 入库；SHIP 直接寄出。旧订单为空时按入库处理。 */
    private String fulfillmentType;
    private String receiverName;
    private String receiverMobile;
    private String receiverAreaName;
    private String receiverDetailAddress;
    private Long getbackId;

    @Schema(description = "商品名称", example = "签名藏品")
    private String name;

    @Schema(description = "商品图片")
    private List<String> picUrl;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
