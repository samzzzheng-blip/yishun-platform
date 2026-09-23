package com.techtron.onebook.module.app.controller.app.fasttrade;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradeSaveReqVO;
import com.techtron.onebook.module.app.service.fasttrade.FastTradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "快速变现")
@RestController
@RequestMapping("/app/fast-trade")
@Validated
public class AppFastTradeController {
    @Resource
    private FastTradeService fastTradeService;

    @PostMapping("/create")
    @Operation(summary = "创建快速变现")
    public CommonResult<Boolean> createFastTrade(@Valid @RequestBody FastTradeSaveReqVO createReqVO) {
        createReqVO.setUserId(getLoginUserId());
        fastTradeService.createFastTrade(createReqVO);
        return success(true);
    }
}
