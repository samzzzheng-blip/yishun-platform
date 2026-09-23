package com.techtron.onebook.module.app.controller.admin.storageplan.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 寄存容量套餐分页 Request VO")
@Data
public class StoragePlanPageReqVO extends PageParam {

}