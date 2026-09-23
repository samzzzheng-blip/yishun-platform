package com.techtron.onebook.module.app.controller.app.collectiontransfer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 转移记录新增/修改 Request VO")
@Data
public class CollectionTransferSaveReqVO {

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "17382")
    private Long userId;

    @Schema(description = "藏品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "7691")
    private List<Long> collectionIds;

    @Schema(description = "被转移用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "19917")
    @NotNull(message = "被转移用户id不能为空")
    private Long toUserId;

}