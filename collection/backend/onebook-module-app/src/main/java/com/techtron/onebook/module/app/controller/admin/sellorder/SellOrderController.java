package com.techtron.onebook.module.app.controller.admin.sellorder;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderRespVO;
import com.techtron.onebook.module.app.service.sellorder.SellOrderService;
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

@Tag(name = "管理后台 - 批量交易卖单")
@RestController
@RequestMapping("/app/sell-order")
@Validated
public class SellOrderController {

    @Resource
    private SellOrderService sellOrderService;

    @GetMapping("/page")
    @Operation(summary = "获得批量交易卖单分页")
    @PreAuthorize("@ss.hasPermission('app:sell-order:query')")
    public CommonResult<PageResult<SellOrderRespVO>> getSellOrderPage(@Valid SellOrderPageReqVO pageReqVO) {
        return success(sellOrderService.getSellOrderPage(pageReqVO));
    }



}