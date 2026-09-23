package com.techtron.onebook.module.app.controller.app.sellorder;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.*;
import com.techtron.onebook.module.app.service.sellorder.SellOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 批量交易卖单")
@RestController
@RequestMapping("/app/sell-order")
@Validated
public class AppSellOrderController {

    @Resource
    private SellOrderService sellOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建批量交易卖单")
    public CommonResult<Long> createSellOrder(@Valid @RequestBody SellOrderSaveReqVO createReqVO) {
        createReqVO.setUserId(getLoginUserId());
        createReqVO.setDealAmount(createReqVO.getAmount());
        return success(sellOrderService.createSellOrder(createReqVO));
    }

    @GetMapping("/get-user-trade-info")
    public CommonResult<UserTradeInfoRespVO> getUserTradeInfo() {
        return success(sellOrderService.getUserTradeInfo(getLoginUserId()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得批量交易卖单分页")
    public CommonResult<PageResult<SellOrderRespVO>> getSellOrderPage(@Valid SellOrderPageReqVO pageReqVO) {
        pageReqVO.setUserId(getLoginUserId());
        return success(sellOrderService.getSellOrderPage(pageReqVO));
    }

    @PostMapping("/cancel")
    public CommonResult<Boolean> cancel(@RequestBody SellOrderCancelVO reqVO) {
        reqVO.setUserId(getLoginUserId());
        sellOrderService.cancel(reqVO);
        return success(true);
    }
}