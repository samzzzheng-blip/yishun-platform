package com.techtron.onebook.module.app.controller.app.exchangelog;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.exchangelog.vo.ExchangeLogPageReqVO;
import com.techtron.onebook.module.app.controller.admin.exchangelog.vo.ExchangeLogRespVO;
import com.techtron.onebook.module.app.controller.admin.exchangelog.vo.ExchangeLogSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.exchangelog.ExchangeLogDO;
import com.techtron.onebook.module.app.service.exchangelog.ExchangeLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 兑换记录")
@RestController
@RequestMapping("/app/exchange-log")
@Validated
public class AppExchangeLogController {

    @Resource
    private ExchangeLogService exchangeLogService;

    @PostMapping("/create")
    @Operation(summary = "创建兑换记录")
    public CommonResult<Long> createExchangeLog(@Valid @RequestBody ExchangeLogSaveReqVO createReqVO) {
        createReqVO.setUserId(getLoginUserId());
        return success(exchangeLogService.createExchangeLog(createReqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得兑换记录分页")
    public CommonResult<PageResult<ExchangeLogRespVO>> getExchangeLogPage(@Valid ExchangeLogPageReqVO pageReqVO) {
        pageReqVO.setUserId(getLoginUserId());
        PageResult<ExchangeLogDO> pageResult = exchangeLogService.getExchangeLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ExchangeLogRespVO.class));
    }

    @PutMapping("/update")
    @Operation(summary = "更新兑换记录")
    public CommonResult<Boolean> updateExchangeLog(@Valid @RequestBody ExchangeLogSaveReqVO updateReqVO) {
        exchangeLogService.updateExchangeLog(updateReqVO);
        return success(true);
    }



}