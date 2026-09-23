package com.techtron.onebook.module.app.controller.app.buyorder;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderCancelVO;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderRespVO;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.buyorder.BuyOrderDO;
import com.techtron.onebook.module.app.service.buyorder.BuyOrderService;
import com.techtron.onebook.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 批量交易买单")
@RestController
@RequestMapping("/app/buy-order")
@Validated
public class AppBuyOrderController {

    @Resource
    private BuyOrderService buyOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建批量交易买单")
    public CommonResult<Long> createBuyOrder(@Valid @RequestBody BuyOrderSaveReqVO createReqVO) {
        createReqVO.setUserId(getLoginUserId());
        createReqVO.setDealAmount(createReqVO.getAmount());
        return success(buyOrderService.createBuyOrder(createReqVO));
    }

    @PostMapping("/update-paid")
    @Operation(summary = "更新订单为已支付") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    @PermitAll
    public CommonResult<Boolean> updateOrderPaid(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        buyOrderService.updateOrderPaid(Long.valueOf(notifyReqDTO.getMerchantOrderId()),
                notifyReqDTO.getPayOrderId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得批量交易买单分页")
    public CommonResult<PageResult<BuyOrderRespVO>> getBuyOrderPage(@Valid BuyOrderPageReqVO pageReqVO) {
        pageReqVO.setUserId(getLoginUserId());
        return success(buyOrderService.getBuyOrderPage(pageReqVO));
    }

    @PostMapping("/cancel")
    public CommonResult<Boolean> cancel(@RequestBody BuyOrderCancelVO reqVO) {
        reqVO.setUserId(getLoginUserId());
        buyOrderService.cancel(reqVO);
        return success(true);
    }

}