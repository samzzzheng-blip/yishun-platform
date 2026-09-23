package com.techtron.onebook.module.app.controller.app.getback;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackRespVO;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackSaveReqVO;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackUpdateReqVO;
import com.techtron.onebook.module.app.dal.dataobject.getback.GetbackDO;
import com.techtron.onebook.module.app.service.getback.GetbackService;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 取回")
@RestController
@RequestMapping("/app/getback")
@Validated
public class AppGetbackController {

    @Resource
    private GetbackService getbackService;

    @Resource
    private com.techtron.onebook.module.app.service.logistics.GetbackLogisticsService logistics;

    @GetMapping("/logistics")
    @Operation(summary = "查询本人取回包裹物流")
    public CommonResult<com.techtron.onebook.module.app.service.logistics.LogisticsResult> logistics(@RequestParam("id") Long id) {
        return success(logistics.get(id, getLoginUserId()));
    }

    @PostMapping("/create")
    @Operation(summary = "创建取回")
    public CommonResult<Long> createGetback(@Valid @RequestBody GetbackSaveReqVO createReqVO) {
        createReqVO.setUserId(getLoginUserId());
        return success(getbackService.createGetback(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新取回")
    public CommonResult<Boolean> updateGetback(@Valid @RequestBody GetbackUpdateReqVO updateReqVO) {
        if (!java.util.Objects.equals(updateReqVO.getStatus(), 2)) {
            throw com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception(
                com.techtron.onebook.module.app.enums.ErrorCodeConstants.GETBACK_STATUS_INVALID);
        }
        updateReqVO.setUserId(getLoginUserId());
        getbackService.updateGetback(updateReqVO);
        return success(true);
    }

    @GetMapping("/get-deliver")
    @Operation(summary = "获得快递单号")
    public CommonResult<GetbackDO> getCollection(@RequestParam("collectionId") Long collectionId) {
        return success(getbackService.getDeliver(collectionId, getLoginUserId())
        );
    }

    @GetMapping("/list")
    @Operation(summary = "查询用户取回记录")
    public CommonResult<List<GetbackRespVO>> getGetbackList() {
        return success(getbackService.getGetbackListByUserId(getLoginUserId()));
    }


}