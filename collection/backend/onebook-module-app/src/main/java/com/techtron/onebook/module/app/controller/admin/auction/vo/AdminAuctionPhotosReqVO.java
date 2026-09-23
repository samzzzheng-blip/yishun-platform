package com.techtron.onebook.module.app.controller.admin.auction.vo;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class AdminAuctionPhotosReqVO {
    @NotNull private Long id;
    @NotNull @Size(min = 1, max = 9)
    private List<@NotBlank @Size(max = 2048) @Pattern(regexp = "https?://[^\\s]+") String> displayPicUrls;
}
