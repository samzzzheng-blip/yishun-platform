package com.techtron.onebook.module.app.controller.app.stonerecord.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AppStoneRecordPageReqVO extends PageParam {
    @Pattern(regexp = "all|income|expense")
    private String direction = "all";
}
