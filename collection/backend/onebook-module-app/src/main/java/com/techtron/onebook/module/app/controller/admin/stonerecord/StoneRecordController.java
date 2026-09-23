package com.techtron.onebook.module.app.controller.admin.stonerecord;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.stonerecord.vo.StoneRecordPageReqVO;
import com.techtron.onebook.module.app.controller.admin.stonerecord.vo.StoneRecordRespVO;
import com.techtron.onebook.module.app.service.stonerecord.StoneRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 能量石记录")
@RestController
@RequestMapping("/app/stone-record")
@Validated
public class StoneRecordController {

    @Resource
    private StoneRecordService stoneRecordService;

    @GetMapping("/list")
    @Operation(summary = "查询能量石记录列表")
    public CommonResult<PageResult<StoneRecordRespVO>> getStoneRecordList(@Valid StoneRecordPageReqVO pageReqVO) {
        return success(stoneRecordService.getStoneRecordPage(pageReqVO));
    }

}
