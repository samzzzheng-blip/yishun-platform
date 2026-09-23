package com.techtron.onebook.module.app.controller.app.ykjorder;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.app.ykjorder.vo.YkjOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.ykjorder.vo.YkjOrderRespVO;
import com.techtron.onebook.module.app.controller.app.ykjorder.vo.YkjOrderSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.dal.dataobject.ykjorder.YkjOrderDO;
import com.techtron.onebook.module.app.service.yikoujia.YikoujiaService;
import com.techtron.onebook.module.app.service.ykjorder.YkjOrderService;
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

@Tag(name = "管理后台 - 一口价买单")
@RestController
@RequestMapping("/app/ykj-order")
@Validated
public class YkjOrderController {

    @Resource
    private YkjOrderService ykjOrderService;

    @Resource
    private YikoujiaService yikoujiaService;

    @PostMapping("/create")
    @Operation(summary = "创建一口价买单")
    public CommonResult<Long> createYkjOrder(@Valid @RequestBody YkjOrderSaveReqVO createReqVO) {
        createReqVO.setUserId(getLoginUserId());
        return success(ykjOrderService.createYkjOrder(createReqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得当前用户的一口价订单")
    public CommonResult<PageResult<YkjOrderRespVO>> getYkjOrderPage(@Valid YkjOrderPageReqVO pageReqVO) {
        pageReqVO.setUserId(getLoginUserId());
        PageResult<YkjOrderDO> pageResult = ykjOrderService.getYkjOrderPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, YkjOrderRespVO.class, order -> {
            YikoujiaDO product = yikoujiaService.getYikoujia(order.getYkjId());
            if (product != null) {
                order.setName(product.getName());
                order.setPicUrl(product.getPicUrl());
            }
        }));
    }

    @PostMapping("/update-paid")
    @Operation(summary = "更新订单为已支付") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    @PermitAll
    public CommonResult<Boolean> updateOrderPaid(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        ykjOrderService.updateOrderPaid(Long.valueOf(notifyReqDTO.getMerchantOrderId()),
                notifyReqDTO.getPayOrderId());
        return success(true);
    }


}
