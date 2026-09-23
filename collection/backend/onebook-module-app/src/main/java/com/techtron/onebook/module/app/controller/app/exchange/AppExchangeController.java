package com.techtron.onebook.module.app.controller.app.exchange;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.exchange.vo.ExchangeRespVO;
import com.techtron.onebook.module.app.dal.dataobject.exchange.ExchangeDO;
import com.techtron.onebook.module.app.service.exchange.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 兑换品")
@RestController
@RequestMapping("/app/exchange")
@Validated
public class AppExchangeController {

    @Resource
    private ExchangeService exchangeService;

    @GetMapping("/list")
    @Operation(summary = "获得兑换品分页")
    @PermitAll
    public CommonResult<List<ExchangeRespVO>> getExchangeList() {
        List<ExchangeDO> pageResult = exchangeService.getExchangeList();
        return success(BeanUtils.toBean(pageResult, ExchangeRespVO.class));
    }

    @GetMapping("/get")
    @PermitAll
    public CommonResult<ExchangeRespVO> getExchangeById(@RequestParam("id") Long id) {
        return success(exchangeService.getExchange(id));
    }

}