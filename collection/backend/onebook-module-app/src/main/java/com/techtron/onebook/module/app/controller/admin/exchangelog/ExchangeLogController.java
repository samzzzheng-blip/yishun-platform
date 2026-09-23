package com.techtron.onebook.module.app.controller.admin.exchangelog;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import com.techtron.onebook.framework.common.pojo.PageParam;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

import com.techtron.onebook.framework.excel.core.util.ExcelUtils;

import com.techtron.onebook.framework.apilog.core.annotation.ApiAccessLog;
import static com.techtron.onebook.framework.apilog.core.enums.OperateTypeEnum.*;

import com.techtron.onebook.module.app.controller.admin.exchangelog.vo.*;
import com.techtron.onebook.module.app.dal.dataobject.exchangelog.ExchangeLogDO;
import com.techtron.onebook.module.app.service.exchangelog.ExchangeLogService;

@Tag(name = "管理后台 - 兑换记录")
@RestController
@RequestMapping("/app/exchange-log")
@Validated
public class ExchangeLogController {

    @Resource
    private ExchangeLogService exchangeLogService;

    @PostMapping("/create")
    @Operation(summary = "创建兑换记录")
    @PreAuthorize("@ss.hasPermission('app:exchange-log:create')")
    public CommonResult<Long> createExchangeLog(@Valid @RequestBody ExchangeLogSaveReqVO createReqVO) {
        return success(exchangeLogService.createExchangeLog(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新兑换记录")
    @PreAuthorize("@ss.hasPermission('app:exchange-log:update')")
    public CommonResult<Boolean> updateExchangeLog(@Valid @RequestBody ExchangeLogSaveReqVO updateReqVO) {
        exchangeLogService.updateExchangeLog(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除兑换记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('app:exchange-log:delete')")
    public CommonResult<Boolean> deleteExchangeLog(@RequestParam("id") Long id) {
        exchangeLogService.deleteExchangeLog(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除兑换记录")
                @PreAuthorize("@ss.hasPermission('app:exchange-log:delete')")
    public CommonResult<Boolean> deleteExchangeLogList(@RequestParam("ids") List<Long> ids) {
        exchangeLogService.deleteExchangeLogListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得兑换记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:exchange-log:query')")
    public CommonResult<ExchangeLogRespVO> getExchangeLog(@RequestParam("id") Long id) {
        ExchangeLogDO exchangeLog = exchangeLogService.getExchangeLog(id);
        return success(BeanUtils.toBean(exchangeLog, ExchangeLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得兑换记录分页")
    @PreAuthorize("@ss.hasPermission('app:exchange-log:query')")
    public CommonResult<PageResult<ExchangeLogRespVO>> getExchangeLogPage(@Valid ExchangeLogPageReqVO pageReqVO) {
        PageResult<ExchangeLogDO> pageResult = exchangeLogService.getExchangeLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ExchangeLogRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出兑换记录 Excel")
    @PreAuthorize("@ss.hasPermission('app:exchange-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExchangeLogExcel(@Valid ExchangeLogPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ExchangeLogDO> list = exchangeLogService.getExchangeLogPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "兑换记录.xls", "数据", ExchangeLogRespVO.class,
                        BeanUtils.toBean(list, ExchangeLogRespVO.class));
    }

}