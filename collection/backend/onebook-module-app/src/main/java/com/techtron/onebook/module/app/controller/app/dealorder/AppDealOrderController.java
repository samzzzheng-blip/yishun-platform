package com.techtron.onebook.module.app.controller.app.dealorder;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.module.app.controller.admin.dealorder.vo.DealOrderRespVO;
import com.techtron.onebook.module.app.service.dealorder.DealOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 批量交易成交")
@RestController
@RequestMapping("/app/deal-order")
@Validated
public class AppDealOrderController {

    @Resource
    private DealOrderService dealOrderService;

    @GetMapping("/list")
    @Operation(summary = "创建批量交易成交")
    @PermitAll
    public CommonResult<List<DealOrderRespVO>> getDealOrderList(@Valid @RequestParam("categoryId") Long categoryId) {
        return success(dealOrderService.getDealOrderList(categoryId));
    }

}