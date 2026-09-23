package com.techtron.onebook.module.app.controller.app.auction.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import lombok.Data;

@Data
public class AppAuctionPageReqVO extends PageParam {
    private String keyword;
    private Integer status;
    private Integer delistStatus;
    /** currentPrice, endTime or createTime */
    private String sortField;
    private Boolean sortAsc;
}
