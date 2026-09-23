package com.techtron.onebook.module.app.controller.admin.exchange;

import com.techtron.onebook.framework.apilog.core.annotation.ApiAccessLog;
import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageParam;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.framework.excel.core.util.ExcelUtils;
import com.techtron.onebook.module.app.controller.admin.exchange.vo.ExchangePageReqVO;
import com.techtron.onebook.module.app.controller.admin.exchange.vo.ExchangeRespVO;
import com.techtron.onebook.module.app.controller.admin.exchange.vo.ExchangeSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.exchange.ExchangeDO;
import com.techtron.onebook.module.app.service.exchange.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.techtron.onebook.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 兑换品")
@RestController
@RequestMapping("/app/exchange")
@Validated
public class ExchangeController {

    @Resource
    private ExchangeService exchangeService;

    @PostMapping("/create")
    @Operation(summary = "创建兑换品")
    @PreAuthorize("@ss.hasPermission('app:exchange:create')")
    public CommonResult<Long> createExchange(@Valid @RequestBody ExchangeSaveReqVO createReqVO) {
        return success(exchangeService.createExchange(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新兑换品")
    @PreAuthorize("@ss.hasPermission('app:exchange:update')")
    public CommonResult<Boolean> updateExchange(@Valid @RequestBody ExchangeSaveReqVO updateReqVO) {
        exchangeService.updateExchange(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除兑换品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('app:exchange:delete')")
    public CommonResult<Boolean> deleteExchange(@RequestParam("id") Long id) {
        exchangeService.deleteExchange(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除兑换品")
                @PreAuthorize("@ss.hasPermission('app:exchange:delete')")
    public CommonResult<Boolean> deleteExchangeList(@RequestParam("ids") List<Long> ids) {
        exchangeService.deleteExchangeListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得兑换品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:exchange:query')")
    public CommonResult<ExchangeRespVO> getExchange(@RequestParam("id") Long id) {
        return success(exchangeService.getExchange(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得兑换品分页")
    @PreAuthorize("@ss.hasPermission('app:exchange:query')")
    public CommonResult<PageResult<ExchangeRespVO>> getExchangePage(@Valid ExchangePageReqVO pageReqVO) {
        PageResult<ExchangeDO> pageResult = exchangeService.getExchangePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ExchangeRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出兑换品 Excel")
    @PreAuthorize("@ss.hasPermission('app:exchange:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExchangeExcel(@Valid ExchangePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ExchangeDO> list = exchangeService.getExchangePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "兑换品.xls", "数据", ExchangeRespVO.class,
                        BeanUtils.toBean(list, ExchangeRespVO.class));
    }

}