package com.techtron.onebook.module.app.controller.admin.yikoujia;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaPageReqVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaRespVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaSaveReqVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaImportPreviewReqVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaImportReqVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaImportRespVO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.service.yikoujia.YikoujiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 一口价")
@RestController
@RequestMapping("/app/yikoujia")
@Validated
public class YikoujiaController {

    @Resource
    private YikoujiaService yikoujiaService;

    @PostMapping("/create")
    @Operation(summary = "创建一口价")
    public CommonResult<Boolean> createYikoujia(@Valid @RequestBody YikoujiaSaveReqVO createReqVO) {
        yikoujiaService.createYikoujiaByAdmin(createReqVO);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得一口价分页")
    @PreAuthorize("@ss.hasPermission('app:yikoujia:query')")
    public CommonResult<PageResult<YikoujiaRespVO>> getYikoujiaPage(@Valid YikoujiaPageReqVO pageReqVO) {
        return success(yikoujiaService.getYikoujiaPage1(pageReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新一口价")
    public CommonResult<Boolean> updateYikoujia(@Valid @RequestBody YikoujiaSaveReqVO updateReqVO) {
        yikoujiaService.updateYikoujia(updateReqVO);
        return success(true);
    }

    @PutMapping("/image-order")
    @Operation(summary = "调整一口价图片顺序")
    @PreAuthorize("@ss.hasPermission('app:yikoujia:update')")
    public CommonResult<Boolean> updateImageOrder(@Valid @RequestBody
            com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaImageOrderReqVO req) {
        yikoujiaService.updateImageOrder(req.getId(), req.getOriginalPicUrl(), req.getPicUrl());
        return success(true);
    }

    @PostMapping("/import-preview")
    @Operation(summary = "根据闲鱼链接预览商品")
    @PreAuthorize("@ss.hasPermission('app:yikoujia:query')")
    public CommonResult<YikoujiaImportRespVO> previewGoofishProduct(
            @Valid @RequestBody YikoujiaImportPreviewReqVO importReqVO) {
        return success(yikoujiaService.previewGoofishProduct(importReqVO.getSource()));
    }

    @GetMapping("/goofish-products")
    @Operation(summary = "查询已授权闲鱼店铺的商品")
    @PreAuthorize("@ss.hasPermission('app:yikoujia:query')")
    public CommonResult<PageResult<YikoujiaImportRespVO>> getGoofishProducts(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "productStatus", required = false) Integer productStatus,
            @RequestParam(value = "source", required = false) String source) {
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 50);
        return success(yikoujiaService.getGoofishProductPage(pageNo, pageSize, productStatus, source));
    }

    @PostMapping("/import-confirm")
    @Operation(summary = "确认导入闲鱼商品")
    @PreAuthorize("@ss.hasPermission('app:yikoujia:query')")
    public CommonResult<Long> importGoofishProduct(@Valid @RequestBody YikoujiaImportReqVO importReqVO) {
        return success(yikoujiaService.importGoofishProduct(importReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得一口价分页")
    @PreAuthorize("@ss.hasPermission('app:yikoujia:query')")
    public CommonResult<YikoujiaRespVO> getYikoujia(@RequestParam("id") Long id) {
        YikoujiaDO yikoujia = yikoujiaService.getYikoujia(id);
        return success(BeanUtils.toBean(yikoujia, YikoujiaRespVO.class));
    }

    @GetMapping("/test")
    public CommonResult<Boolean> test() {
        yikoujiaService.test();
        return success(true);
    }


}
