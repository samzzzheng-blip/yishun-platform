package com.techtron.onebook.module.app.controller.admin.fasttrade;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradePageReqVO;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradeRespVO;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradeSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.fasttrade.FastTradeDO;
import com.techtron.onebook.module.app.service.fasttrade.FastTradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 快速变现")
@RestController
@RequestMapping("/app/fast-trade")
@Validated
public class FastTradeController {

    @Resource
    private FastTradeService fastTradeService;

    @PutMapping("/update")
    @Operation(summary = "更新快速变现")
    @PreAuthorize("@ss.hasPermission('app:fast-trade:update')")
    public CommonResult<Boolean> updateFastTrade(@Valid @RequestBody FastTradeSaveReqVO updateReqVO) {
        fastTradeService.updateFastTrade(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除快速变现")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('app:fast-trade:delete')")
    public CommonResult<Boolean> deleteFastTrade(@RequestParam("id") Long id) {
        fastTradeService.deleteFastTrade(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除快速变现")
                @PreAuthorize("@ss.hasPermission('app:fast-trade:delete')")
    public CommonResult<Boolean> deleteFastTradeList(@RequestParam("ids") List<Long> ids) {
        fastTradeService.deleteFastTradeListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得快速变现")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:fast-trade:query')")
    public CommonResult<FastTradeRespVO> getFastTrade(@RequestParam("id") Long id) {
        FastTradeDO fastTrade = fastTradeService.getFastTrade(id);
        return success(BeanUtils.toBean(fastTrade, FastTradeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得快速变现分页")
    @PreAuthorize("@ss.hasPermission('app:fast-trade:query')")
    public CommonResult<PageResult<FastTradeRespVO>> getFastTradePage(@Valid FastTradePageReqVO pageReqVO) {
        return success(fastTradeService.getFastTradePage(pageReqVO));
    }

}