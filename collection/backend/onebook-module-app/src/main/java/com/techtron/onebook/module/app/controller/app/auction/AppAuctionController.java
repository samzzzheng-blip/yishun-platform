package com.techtron.onebook.module.app.controller.app.auction;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.app.auction.vo.*;
import com.techtron.onebook.module.app.service.auction.AuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "APP - 竞价市场")
@RestController
@RequestMapping("/app/auction")
@Validated
public class AppAuctionController {
    @Resource private AuctionService auctionService;

    @PostMapping("/create")
    @Operation(summary = "提交藏品闲鱼竞拍申请")
    public CommonResult<Long> create(@Valid @RequestBody AppAuctionCreateReqVO req) {
        return success(auctionService.createAuction(getLoginUserId(), req));
    }
    @DeleteMapping("/cancel")
    @Operation(summary = "取消无出价的竞价")
    public CommonResult<Boolean> cancel(@RequestParam Long id) {
        auctionService.cancelAuction(getLoginUserId(), id);
        return success(true);
    }
    @PostMapping("/request-delist")
    @Operation(summary = "申请下架已发布的闲鱼竞拍")
    public CommonResult<Boolean> requestDelist(@RequestParam Long id) {
        auctionService.requestDelist(getLoginUserId(), id);
        return success(true);
    }
    @GetMapping("/get") @PermitAll
    public CommonResult<AppAuctionRespVO> get(@RequestParam Long id) {
        return success(sanitize(auctionService.getAuction(id)));
    }
    @GetMapping("/page") @PermitAll
    public CommonResult<PageResult<AppAuctionRespVO>> page(@Valid AppAuctionPageReqVO req) {
        req.setStatus(1);
        PageResult<AppAuctionRespVO> page = auctionService.getAuctionPage(req);
        page.getList().forEach(this::sanitize);
        return success(page);
    }
    @GetMapping("/my-page")
    public CommonResult<PageResult<AppAuctionRespVO>> myPage(@Valid AppAuctionPageReqVO req) {
        return success(auctionService.getMyAuctionPage(getLoginUserId(), req));
    }
    private AppAuctionRespVO sanitize(AppAuctionRespVO auction) {
        auction.setSellerId(null);
        auction.setHighestBidderId(null);
        return auction;
    }
}
