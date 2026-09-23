package com.techtron.onebook.module.app.controller.app.ykjorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 一口价买单新增/修改 Request VO")
@Data
public class YkjOrderSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15044")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "14233")
    private Long userId;

    @Schema(description = "一口价商品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "25496")
    @NotNull(message = "一口价商品id不能为空")
    private Long ykjId;

    /** WAREHOUSE 入库；SHIP 直接寄出。旧订单为空时按入库处理。 */
    private String fulfillmentType;
    private String receiverName;
    private String receiverMobile;
    private String receiverAreaName;
    private String receiverDetailAddress;

}
