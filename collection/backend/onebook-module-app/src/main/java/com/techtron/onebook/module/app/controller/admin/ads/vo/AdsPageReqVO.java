package com.techtron.onebook.module.app.controller.admin.ads.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 广告分页 Request VO")
@Data
public class AdsPageReqVO extends PageParam {

    @Schema(description = "图片地址", example = "https://www.iocoder.cn")
    private String picUrl;

    @Schema(description = "状态", example = "2")
    private Integer status;

}