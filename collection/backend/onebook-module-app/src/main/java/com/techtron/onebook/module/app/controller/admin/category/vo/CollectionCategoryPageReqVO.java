package com.techtron.onebook.module.app.controller.admin.category.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 藏品分类分页 Request VO")
@Data
public class CollectionCategoryPageReqVO extends PageParam {

}