package com.techtron.onebook.module.app.controller.admin.auction.vo;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminAuctionShareReqVO {
    @NotNull private Long id;
    @NotBlank @Size(max = 3000) private String shareText;
    @AssertTrue(message = "请填写闲鱼分享文案或完整的闲鱼小程序链接")
    public boolean isShareValid() {
        if (shareText != null && shareText.length() <= 3000
                && shareText.trim().matches("^#小程序://闲鱼/[^\\s]+$")) return true;
        return shareText != null && shareText.length() <= 3000 && !shareText.contains("#小程序://")
                && java.util.regex.Pattern.compile("https?://(?:m\\.tb\\.cn|(?:www\\.|m\\.)?goofish\\.com)/[^\\s]+")
                .matcher(shareText).find();
    }
}
