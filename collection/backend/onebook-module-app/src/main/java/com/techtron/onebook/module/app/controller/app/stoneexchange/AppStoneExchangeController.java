package com.techtron.onebook.module.app.controller.app.stoneexchange;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.module.app.controller.admin.stoneexchange.vo.StoneExchangeSaveReqVO;
import com.techtron.onebook.module.app.service.stoneexchange.StoneExchangeService;
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

@Tag(name = "管理后台 - 能量石兑换")
@RestController
@RequestMapping("/app/stone-exchange")
@Validated
public class AppStoneExchangeController {

    @Resource
    private StoneExchangeService stoneExchangeService;

    @PostMapping("/create")
    @Operation(summary = "创建能量石兑换")
    public CommonResult<Long> createStoneExchange(@Valid @RequestBody StoneExchangeSaveReqVO createReqVO) {
        createReqVO.setUserId(getLoginUserId());
        return success(stoneExchangeService.createStoneExchange(createReqVO));
    }



}