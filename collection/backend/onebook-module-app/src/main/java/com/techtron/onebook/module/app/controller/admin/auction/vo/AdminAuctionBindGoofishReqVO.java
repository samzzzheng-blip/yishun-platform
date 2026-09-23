package com.techtron.onebook.module.app.controller.admin.auction.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminAuctionBindGoofishReqVO {
    @NotNull
    private Long id;

    @NotBlank(message = "请填写闲鱼竞拍链接")
    @Size(max = 1000, message = "闲鱼竞拍链接过长")
    private String goofishUrl;
    @Size(min = 1, max = 9)
    private java.util.List<@NotBlank @Size(max = 2048) @jakarta.validation.constraints.Pattern(regexp = "https?://[^\\s]+") String> displayPicUrls;
}
