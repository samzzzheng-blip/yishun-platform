package com.techtron.onebook.module.app.controller.admin.yikoujia.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 一口价分页 Request VO")
@Data
public class YikoujiaPageReqVO extends PageParam {

    public static final String SORT_FIELD_PRICE = "price";
    public static final String SORT_FIELD_CREATE_TIME = "createTime";

    private Long userId;

    private Integer status;

    private Boolean pendingSettlement;

    @Schema(description = "排序字段", example = "price") // 参见 AppProductSpuPageReqVO.SORT_FIELD_XXX 常量
    private String sortField;

    @Schema(description = "排序方式", example = "true")
    private Boolean sortAsc;

    private String keyword;
    
    private Integer statusList;

}