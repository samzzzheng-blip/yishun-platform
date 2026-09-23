package com.techtron.onebook.module.app.controller.admin.storageplan;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.storageplan.vo.StoragePlanPageReqVO;
import com.techtron.onebook.module.app.controller.admin.storageplan.vo.StoragePlanRespVO;
import com.techtron.onebook.module.app.controller.admin.storageplan.vo.StoragePlanSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.storageplan.StoragePlanDO;
import com.techtron.onebook.module.app.service.storageplan.StoragePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 寄存容量套餐")
@RestController
@RequestMapping("/app/storage-plan")
@Validated
public class StoragePlanController {

    @Resource
    private StoragePlanService storagePlanService;

    @PostMapping("/add")
    @Operation(summary = "新增寄存容量套餐")
    @PreAuthorize("@ss.hasPermission('app:storage-plan:add')")
    public CommonResult<Long> addStoragePlan(@Valid @RequestBody StoragePlanSaveReqVO createReqVO) {
        return success(storagePlanService.createStoragePlan(createReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "修改寄存容量套餐")
    public CommonResult<Boolean> updateStoragePlan(@Valid @RequestBody StoragePlanSaveReqVO updateReqVO) {
        storagePlanService.updateStoragePlan(updateReqVO);
        return success(true);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除寄存容量套餐")
    @Parameter(name = "id", description = "套餐ID", required = true)
    @PreAuthorize("@ss.hasPermission('app:storage-plan:delete')")
    public CommonResult<Boolean> deleteStoragePlan(@RequestBody Long id) {
        storagePlanService.deleteStoragePlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "查询寄存容量套餐详情")
    @Parameter(name = "id", description = "套餐ID", required = true, example = "1")
    public CommonResult<StoragePlanRespVO> getStoragePlan(@RequestParam("id") Long id) {
        StoragePlanDO storagePlan = storagePlanService.getStoragePlan(id);
        return success(BeanUtils.toBean(storagePlan, StoragePlanRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "查询寄存容量套餐列表")
    public CommonResult<PageResult<StoragePlanRespVO>> getStoragePlanList(@Valid StoragePlanPageReqVO pageReqVO) {
        PageResult<StoragePlanDO> pageResult = storagePlanService.getStoragePlanPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, StoragePlanRespVO.class));
    }

}