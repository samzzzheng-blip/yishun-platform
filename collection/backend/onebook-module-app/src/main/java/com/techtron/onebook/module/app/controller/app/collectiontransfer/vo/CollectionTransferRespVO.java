package com.techtron.onebook.module.app.controller.app.collectiontransfer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 转移记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CollectionTransferRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17219")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "17382")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "藏品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "383")
    @ExcelProperty("藏品id")
    private Long collectionId;

    @Schema(description = "数量")
    @ExcelProperty("数量")
    private Integer amount;

    @Schema(description = "被转移用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "19917")
    @ExcelProperty("被转移用户id")
    private Long toUserId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
