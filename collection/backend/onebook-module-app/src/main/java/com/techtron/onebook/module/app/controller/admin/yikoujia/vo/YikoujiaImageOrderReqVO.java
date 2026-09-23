package com.techtron.onebook.module.app.controller.admin.yikoujia.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class YikoujiaImageOrderReqVO {
    @NotNull
    private Long id;
    @NotEmpty
    private List<String> originalPicUrl;
    @NotEmpty
    private List<String> picUrl;
}
