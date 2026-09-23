package com.techtron.onebook.module.app.controller.admin.buyorder;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderRespVO;
import com.techtron.onebook.module.app.service.buyorder.BuyOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 批量交易买单")
@RestController
@RequestMapping("/app/buy-order")
@Validated
public class BuyOrderController {

    @Resource
    private BuyOrderService buyOrderService;



    @GetMapping("/page")
    @Operation(summary = "获得批量交易买单分页")
    @PreAuthorize("@ss.hasPermission('app:buy-order:query')")
    public CommonResult<PageResult<BuyOrderRespVO>> getBuyOrderPage(@Valid BuyOrderPageReqVO pageReqVO) {
        return success(buyOrderService.getBuyOrderPage(pageReqVO));
    }


}