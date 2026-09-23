package com.techtron.onebook.module.app.controller.app.collection.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "客户端 - 藏品登记创建 Response VO")
@Data
public class AppCollectionCreateRespVO {

    @Schema(description = "藏品编号", example = "12422")
    private Long collectionId;

    @Schema(description = "寄存档位ID", example = "0")
    private Long storagePlanId;

    @Schema(description = "是否需要升级寄存套餐", example = "false")
    private Boolean needUpgrade;

    @Schema(description = "月费价格（能量石）", example = "5")
    private Integer monthlyPrice;

    @Schema(description = "年费价格（能量石）", example = "50")
    private Integer yearlyPrice;

    @Schema(description = "用户当前寄存套餐ID", example = "0")
    private Long storageId;

    private LocalDateTime expireTime;

    public static AppCollectionCreateRespVO success(Long collectionId, LocalDateTime expireTime) {
        AppCollectionCreateRespVO resp = new AppCollectionCreateRespVO();
        resp.setCollectionId(collectionId);
        resp.setStoragePlanId(0L);
        resp.setNeedUpgrade(false);
        resp.setExpireTime(expireTime);
        return resp;
    }

    public static AppCollectionCreateRespVO needUpgrade(Long storagePlanId, Integer monthlyPrice, Integer yearlyPrice, Long storageId, LocalDateTime expireTime) {
        AppCollectionCreateRespVO resp = new AppCollectionCreateRespVO();
        resp.setCollectionId(null);
        resp.setStoragePlanId(storagePlanId);
        resp.setNeedUpgrade(true);
        resp.setMonthlyPrice(monthlyPrice);
        resp.setYearlyPrice(yearlyPrice);
        resp.setStorageId(storageId);
        resp.setExpireTime(expireTime);
        return resp;
    }

}