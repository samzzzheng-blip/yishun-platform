package com.techtron.onebook.module.app.service.yikoujia.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoofishAuctionBO {
    private String productId;
    private String itemUrl;
    private Integer currentPrice;
    private Integer bidCount;
    private Integer productStatus;
    private LocalDateTime endTime;
}
