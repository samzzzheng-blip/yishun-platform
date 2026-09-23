package com.techtron.onebook.module.app.controller.admin.fasttrade.vo;

import com.techtron.onebook.module.app.dal.dataobject.fasttrade.FastTradeItemDO;
import lombok.Data;

import java.util.List;

@Data
public class FastTradeItemVO extends FastTradeItemDO {
    private String name;

    private List<String> picUrls;
}
