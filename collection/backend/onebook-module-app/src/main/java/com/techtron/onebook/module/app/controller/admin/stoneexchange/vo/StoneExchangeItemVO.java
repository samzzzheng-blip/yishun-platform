package com.techtron.onebook.module.app.controller.admin.stoneexchange.vo;

import com.techtron.onebook.module.app.dal.dataobject.stoneexchange.StoneExchangeItemDO;
import lombok.Data;

import java.util.List;

@Data
public class StoneExchangeItemVO extends StoneExchangeItemDO {
    private String name;

    private List<String> picUrls;
}
