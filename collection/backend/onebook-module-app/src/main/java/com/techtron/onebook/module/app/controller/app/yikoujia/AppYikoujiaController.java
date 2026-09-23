package com.techtron.onebook.module.app.controller.app.yikoujia;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaPageReqVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaRespVO;
import com.techtron.onebook.module.app.controller.app.yikoujia.vo.AppYikoujiaSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.service.yikoujia.YikoujiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "一口价")
@RestController
@RequestMapping("/app/yikoujia")
@Validated
public class AppYikoujiaController {

    @Resource
    private YikoujiaService yikoujiaService;

    @PostMapping("/create")
    @Operation(summary = "创建一口价")
    public CommonResult<Boolean> createYikoujia(@Valid @RequestBody AppYikoujiaSaveReqVO createReqVO) {
        createReqVO.setUserId(getLoginUserId());
        yikoujiaService.createYikoujia(createReqVO);
        return success(true);
    }

    @PostMapping("/delist")
    @Operation(summary = "用户下架自己的一口价商品")
    public CommonResult<Boolean> delistYikoujia(
            @RequestParam("id") @Parameter(description = "一口价商品编号", required = true) Long id) {
        yikoujiaService.delistYikoujia(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得一口价分页")
    @PermitAll
    public CommonResult<PageResult<YikoujiaRespVO>> getYikoujiaPage(@Valid YikoujiaPageReqVO pageReqVO) {
        PageResult<YikoujiaDO> pageResult = yikoujiaService.getYikoujiaPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, YikoujiaRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得一口价")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PermitAll
    public CommonResult<YikoujiaRespVO> getYikoujia(@RequestParam("id") Long id) {
        YikoujiaDO yikoujia = yikoujiaService.getYikoujia(id);
        return success(BeanUtils.toBean(yikoujia, YikoujiaRespVO.class));
    }

    @GetMapping("/my-list")
    @Operation(summary = "在售列表")
    public CommonResult<List<YikoujiaRespVO>> getMyList(@Valid YikoujiaPageReqVO pageReqVO) {
        pageReqVO.setUserId(getLoginUserId());
        List<YikoujiaRespVO> pageResult = yikoujiaService.getMyYikoujiaList(pageReqVO);
        return success(pageResult);
    }

}
