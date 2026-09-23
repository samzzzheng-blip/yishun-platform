package com.techtron.onebook.module.app.controller.admin.auction;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.app.auction.vo.AppAuctionPageReqVO;
import com.techtron.onebook.module.app.controller.app.auction.vo.AppAuctionRespVO;
import com.techtron.onebook.module.app.controller.admin.auction.vo.AdminAuctionRejectDelistReqVO;
import com.techtron.onebook.module.app.controller.admin.auction.vo.AdminAuctionBindGoofishReqVO;
import com.techtron.onebook.module.app.controller.admin.auction.vo.AdminAuctionRejectPublishReqVO;
import com.techtron.onebook.module.app.controller.admin.auction.vo.AdminAuctionConfirmSaleReqVO;
import com.techtron.onebook.module.app.service.auction.AuctionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 竞价管理")
@RestController
@RequestMapping("/app/auction")
public class AuctionController {
    @Resource private AuctionService auctionService;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('app:auction:query')")
    public CommonResult<PageResult<AppAuctionRespVO>> page(@Valid AppAuctionPageReqVO req) {
        return success(auctionService.getAuctionPage(req));
    }
    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('app:auction:query')")
    public CommonResult<AppAuctionRespVO> get(@RequestParam Long id) {
        return success(auctionService.getAuction(id));
    }
    @PostMapping("/close-expired")
    @PreAuthorize("@ss.hasPermission('app:auction:update')")
    public CommonResult<Integer> closeExpired() {
        return success(auctionService.closeExpiredAuctions(100));
    }
    @PostMapping("/sync")
    @PreAuthorize("@ss.hasPermission('app:auction:update')")
    public CommonResult<Boolean> sync(@RequestParam Long id) {
        auctionService.syncAuction(id);
        return success(true);
    }
    @PostMapping("/bind-goofish")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@ss.hasPermission('app:auction:update')")
    public CommonResult<Boolean> bindGoofish(@Valid @RequestBody AdminAuctionBindGoofishReqVO req) {
        auctionService.bindGoofishAuction(getLoginUserId(), req.getId(), req.getGoofishUrl());
        if (req.getDisplayPicUrls() != null) {
            auctionService.updateDisplayPhotos(req.getId(), req.getDisplayPicUrls());
        }
        return success(true);
    }
    @PostMapping("/bind-managed-product")
    @PreAuthorize("@ss.hasPermission('app:auction:update')")
    public CommonResult<Boolean> bindManagedProduct(@RequestParam Long id, @RequestParam String productId) {
        auctionService.bindManagedProduct(getLoginUserId(), id, productId);
        return success(true);
    }
    @PostMapping("/display-photos")
    @PreAuthorize("@ss.hasPermission('app:auction:update')")
    public CommonResult<Boolean> updatePhotos(@Valid @RequestBody
            com.techtron.onebook.module.app.controller.admin.auction.vo.AdminAuctionPhotosReqVO req) {
        auctionService.updateDisplayPhotos(req.getId(), req.getDisplayPicUrls());
        return success(true);
    }
    @PostMapping("/share-text")
    @PreAuthorize("@ss.hasPermission('app:auction:update')")
    public CommonResult<Boolean> updateShareText(@Valid @RequestBody
            com.techtron.onebook.module.app.controller.admin.auction.vo.AdminAuctionShareReqVO req) {
        auctionService.updateShareText(req);
        return success(true);
    }
    @PostMapping("/reject-publish")
    @PreAuthorize("@ss.hasPermission('app:auction:update')")
    public CommonResult<Boolean> rejectPublish(@Valid @RequestBody AdminAuctionRejectPublishReqVO req) {
        auctionService.rejectPublish(getLoginUserId(), req.getId(), req.getReason());
        return success(true);
    }
    @PostMapping("/approve-delist")
    @PreAuthorize("@ss.hasPermission('app:auction:update')")
    public CommonResult<Boolean> approveDelist(@RequestParam Long id) {
        auctionService.approveDelist(getLoginUserId(), id);
        return success(true);
    }
    @PostMapping("/reject-delist")
    @PreAuthorize("@ss.hasPermission('app:auction:update')")
    public CommonResult<Boolean> rejectDelist(@Valid @RequestBody AdminAuctionRejectDelistReqVO req) {
        auctionService.rejectDelist(getLoginUserId(), req.getId(), req.getReason());
        return success(true);
    }
    @PostMapping("/confirm-sale")
    @PreAuthorize("@ss.hasPermission('app:auction:update')")
    public CommonResult<Boolean> confirmSale(@Valid @RequestBody AdminAuctionConfirmSaleReqVO req) {
        auctionService.confirmSale(getLoginUserId(), req.getId(), req.getGrossAmount(), req.getRemark());
        return success(true);
    }
    @PostMapping("/confirm-unsold")
    @PreAuthorize("@ss.hasPermission('app:auction:update')")
    public CommonResult<Boolean> confirmUnsold(@RequestParam Long id) {
        auctionService.confirmUnsold(getLoginUserId(), id);
        return success(true);
    }
}
