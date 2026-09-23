package com.techtron.onebook.module.app.controller.app.stonerecord.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppStoneRecordRespVO {
    private Long id;
    private Integer amount;
    private Integer type;
    private Integer balance;
    private Long exchangeLogId;
    private String exchangeName;
    private Integer exchangeQuantity;
    private LocalDateTime createTime;
}
