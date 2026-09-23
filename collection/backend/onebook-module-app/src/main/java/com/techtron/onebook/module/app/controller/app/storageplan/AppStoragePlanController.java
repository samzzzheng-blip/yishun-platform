package com.techtron.onebook.module.app.controller.app.storageplan;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.app.storageplan.vo.AppStoragePlanPurchaseReqVO;
import com.techtron.onebook.module.app.controller.app.storageplan.vo.AppStoragePlanRespVO;
import com.techtron.onebook.module.app.dal.dataobject.storageplan.StoragePlanDO;
import com.techtron.onebook.module.app.service.storageplan.StoragePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.OFF_LINE;

@Tag(name = "客户端 - 寄存容量套餐")
@RestController
@RequestMapping("/app/storage-plan")
@Validated
public class AppStoragePlanController {

    @Resource
    private StoragePlanService storagePlanService;

    @GetMapping("/list")
    @Operation(summary = "查询可用寄存容量套餐列表")
    @PermitAll
    public CommonResult<List<AppStoragePlanRespVO>> getStoragePlanList() {
        List<StoragePlanDO> list = storagePlanService.getEnabledStoragePlanList();
        return success(BeanUtils.toBean(list, AppStoragePlanRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取寄存容量套餐信息")
    @Parameter(name = "id", description = "套餐ID", required = true, example = "1")
    public CommonResult<AppStoragePlanRespVO> getStoragePlan(@RequestParam("id") Long id) {
        StoragePlanDO plan = storagePlanService.getStoragePlan(id);
        return success(BeanUtils.toBean(plan, AppStoragePlanRespVO.class));
    }

    @PostMapping("/purchase")
    @Operation(summary = "购买寄存容量套餐")
    public CommonResult<Boolean> purchaseStoragePlan(@Valid @RequestBody AppStoragePlanPurchaseReqVO reqVO) {
        throw exception(OFF_LINE);
//        return success(storagePlanService.purchaseStoragePlan(getLoginUserId(), reqVO.getPlanId(), reqVO.getType()));
    }

}