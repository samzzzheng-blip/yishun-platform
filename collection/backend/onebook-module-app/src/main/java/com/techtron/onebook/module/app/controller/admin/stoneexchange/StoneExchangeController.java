package com.techtron.onebook.module.app.controller.admin.stoneexchange;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.stoneexchange.vo.StoneAddReqVO;
import com.techtron.onebook.module.app.controller.admin.stoneexchange.vo.StoneExchangePageReqVO;
import com.techtron.onebook.module.app.controller.admin.stoneexchange.vo.StoneExchangeRespVO;
import com.techtron.onebook.module.app.controller.admin.stoneexchange.vo.StoneExchangeSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.stoneexchange.StoneExchangeDO;
import com.techtron.onebook.module.app.service.stoneexchange.StoneExchangeService;
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

@Tag(name = "管理后台 - 能量石兑换")
@RestController
@RequestMapping("/app/stone-exchange")
@Validated
public class StoneExchangeController {

    @Resource
    private StoneExchangeService stoneExchangeService;

    @PostMapping("/create")
    @Operation(summary = "创建能量石兑换")
    @PreAuthorize("@ss.hasPermission('app:stone-exchange:create')")
    public CommonResult<Long> createStoneExchange(@Valid @RequestBody StoneExchangeSaveReqVO createReqVO) {
        return success(stoneExchangeService.createStoneExchange(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新能量石兑换")
    @PreAuthorize("@ss.hasPermission('app:stone-exchange:update')")
    public CommonResult<Boolean> updateStoneExchange(@Valid @RequestBody StoneExchangeSaveReqVO updateReqVO) {
        stoneExchangeService.updateStoneExchange(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除能量石兑换")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('app:stone-exchange:delete')")
    public CommonResult<Boolean> deleteStoneExchange(@RequestParam("id") Long id) {
        stoneExchangeService.deleteStoneExchange(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除能量石兑换")
                @PreAuthorize("@ss.hasPermission('app:stone-exchange:delete')")
    public CommonResult<Boolean> deleteStoneExchangeList(@RequestParam("ids") List<Long> ids) {
        stoneExchangeService.deleteStoneExchangeListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得能量石兑换")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:stone-exchange:query')")
    public CommonResult<StoneExchangeRespVO> getStoneExchange(@RequestParam("id") Long id) {
        StoneExchangeDO stoneExchange = stoneExchangeService.getStoneExchange(id);
        return success(BeanUtils.toBean(stoneExchange, StoneExchangeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得能量石兑换分页")
    @PreAuthorize("@ss.hasPermission('app:stone-exchange:query')")
    public CommonResult<PageResult<StoneExchangeRespVO>> getStoneExchangePage(@Valid StoneExchangePageReqVO pageReqVO) {
        return success(stoneExchangeService.getStoneExchangePage(pageReqVO));
    }

    @PostMapping("/add")
    public CommonResult<Boolean> addStoneExchange(@Valid @RequestBody StoneAddReqVO reqVO) {
        return success(stoneExchangeService.addStone(reqVO));
    }

}