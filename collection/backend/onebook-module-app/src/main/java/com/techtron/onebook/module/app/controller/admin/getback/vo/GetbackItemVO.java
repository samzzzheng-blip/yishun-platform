package com.techtron.onebook.module.app.controller.admin.getback.vo;

import com.techtron.onebook.module.app.dal.dataobject.getback.GetbackItemDO;
import lombok.Data;

import java.util.List;

@Data
public class GetbackItemVO extends GetbackItemDO {
    private String name;

    private List<String> picUrls;
}
