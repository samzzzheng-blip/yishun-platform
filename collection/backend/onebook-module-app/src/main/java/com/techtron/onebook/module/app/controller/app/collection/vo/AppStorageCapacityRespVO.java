package com.techtron.onebook.module.app.controller.app.collection.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "客户端 - 用户空间容量 Response VO")
@Data
public class AppStorageCapacityRespVO {

    @Schema(description = "用户当前寄存套餐ID", example = "0")
    private Long storageId;

    @Schema(description = "空间容量（当前套餐上限，0表示不限）", example = "100")
    private Integer capacity;

    @Schema(description = "当前藏品数量", example = "12")
    private Integer realAmount;

    private LocalDateTime expireTime;

}
