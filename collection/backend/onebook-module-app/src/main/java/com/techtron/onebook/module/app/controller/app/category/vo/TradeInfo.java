package com.techtron.onebook.module.app.controller.app.category.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TradeInfo {
    private Integer price;
    private Integer totalAmount;
    private LocalDateTime time;
}
