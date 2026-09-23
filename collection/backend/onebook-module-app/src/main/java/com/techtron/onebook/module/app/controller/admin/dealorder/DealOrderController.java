package com.techtron.onebook.module.app.controller.admin.dealorder;

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

import com.techtron.onebook.module.app.controller.admin.dealorder.vo.*;
import com.techtron.onebook.module.app.dal.dataobject.dealorder.DealOrderDO;
import com.techtron.onebook.module.app.service.dealorder.DealOrderService;

@Tag(name = "管理后台 - 批量交易成交")
@RestController
@RequestMapping("/app/deal-order")
@Validated
public class DealOrderController {

    @Resource
    private DealOrderService dealOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建批量交易成交")
    @PreAuthorize("@ss.hasPermission('app:deal-order:create')")
    public CommonResult<Long> createDealOrder(@Valid @RequestBody DealOrderSaveReqVO createReqVO) {
        return success(dealOrderService.createDealOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新批量交易成交")
    @PreAuthorize("@ss.hasPermission('app:deal-order:update')")
    public CommonResult<Boolean> updateDealOrder(@Valid @RequestBody DealOrderSaveReqVO updateReqVO) {
        dealOrderService.updateDealOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除批量交易成交")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('app:deal-order:delete')")
    public CommonResult<Boolean> deleteDealOrder(@RequestParam("id") Long id) {
        dealOrderService.deleteDealOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除批量交易成交")
                @PreAuthorize("@ss.hasPermission('app:deal-order:delete')")
    public CommonResult<Boolean> deleteDealOrderList(@RequestParam("ids") List<Long> ids) {
        dealOrderService.deleteDealOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得批量交易成交")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:deal-order:query')")
    public CommonResult<DealOrderRespVO> getDealOrder(@RequestParam("id") Long id) {
        DealOrderDO dealOrder = dealOrderService.getDealOrder(id);
        return success(BeanUtils.toBean(dealOrder, DealOrderRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得批量交易成交分页")
    @PreAuthorize("@ss.hasPermission('app:deal-order:query')")
    public CommonResult<PageResult<DealOrderRespVO>> getDealOrderPage(@Valid DealOrderPageReqVO pageReqVO) {
        PageResult<DealOrderDO> pageResult = dealOrderService.getDealOrderPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DealOrderRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出批量交易成交 Excel")
    @PreAuthorize("@ss.hasPermission('app:deal-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDealOrderExcel(@Valid DealOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DealOrderDO> list = dealOrderService.getDealOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "批量交易成交.xls", "数据", DealOrderRespVO.class,
                        BeanUtils.toBean(list, DealOrderRespVO.class));
    }

}