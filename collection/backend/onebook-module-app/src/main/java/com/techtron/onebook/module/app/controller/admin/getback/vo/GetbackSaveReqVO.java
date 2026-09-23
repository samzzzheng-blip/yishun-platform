package com.techtron.onebook.module.app.controller.admin.getback.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 取回新增/修改 Request VO")
@Data
public class GetbackSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31577")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "19406")
    private Long userId;

    @Schema(description = "藏品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "7691")
    private List<Long> collectionIds;

    @Schema(description = "收件人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "天呈")
    @NotEmpty(message = "收件人名称不能为空")
    private String receiverName;

    @Schema(description = "收件人手机", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "收件人手机不能为空")
    private String receiverMobile;

    @Schema(description = "收件人地区", example = "24519")
    private String receiverAreaName;

    @Schema(description = "收件人详细地址")
    private String receiverDetailAddress;

}