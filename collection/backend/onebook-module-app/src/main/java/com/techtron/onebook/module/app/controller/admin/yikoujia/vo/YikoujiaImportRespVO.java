package com.techtron.onebook.module.app.controller.admin.yikoujia.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 闲鱼商品导入预览 Response VO")
@Data
public class YikoujiaImportRespVO {

    private String productId;
    private String title;
    private Integer price;
    private Integer stock;
    private String content;
    private List<String> images;
    private String sellerName;
    private Integer productStatus;
    private Integer localStatus;
    private boolean alreadyImported;

}
