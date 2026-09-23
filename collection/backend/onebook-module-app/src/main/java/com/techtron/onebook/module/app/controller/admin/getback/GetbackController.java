package com.techtron.onebook.module.app.controller.admin.getback;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackPageReqVO;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackRespVO;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackSaveReqVO;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackUpdateReqVO;
import com.techtron.onebook.module.app.dal.dataobject.getback.GetbackDO;
import com.techtron.onebook.module.app.service.getback.GetbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 取回")
@RestController
@RequestMapping("/app/getback")
@Validated
public class GetbackController {

    @Resource
    private GetbackService getbackService;

    @PostMapping("/create")
    @Operation(summary = "创建取回")
    @PreAuthorize("@ss.hasPermission('app:getback:create')")
    public CommonResult<Long> createGetback(@Valid @RequestBody GetbackSaveReqVO createReqVO) {
        return success(getbackService.createGetback(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新取回")
    @PreAuthorize("@ss.hasPermission('app:getback:update')")
    public CommonResult<Boolean> updateGetback(@Valid @RequestBody GetbackUpdateReqVO updateReqVO) {
        getbackService.updateGetback(updateReqVO);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消待发货的取回申请")
    @PreAuthorize("@ss.hasPermission('app:getback:update')")
    public CommonResult<Boolean> cancelGetback(@RequestParam("id") Long id) {
        getbackService.cancelGetback(id);
        return success(true);
    }



    @GetMapping("/page")
    @Operation(summary = "获得取回分页")
    @PreAuthorize("@ss.hasPermission('app:getback:query')")
    public CommonResult<PageResult<GetbackRespVO>> getGetbackPage(@Valid GetbackPageReqVO pageReqVO) {
        return success(getbackService.getGetbackPage(pageReqVO));
    }

}
