package com.techtron.onebook.module.app.controller.app.category.vo;

import lombok.Data;

import java.util.List;

@Data
public class AppTradeInfoRespVO {
    private List<TradeInfo> sellList;
    private List<TradeInfo> buyList;
}
