package com.techtron.onebook.module.app.controller.admin.collection;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.collection.vo.CollectionRecordPageReqVO;
import com.techtron.onebook.module.app.controller.admin.collection.vo.CollectionRecordRespVO;
import com.techtron.onebook.module.app.service.collection.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 藏品变更记录")
@RestController
@RequestMapping("/app/collection-record")
@Validated
public class CollectionRecordController {

    @Resource
    private CollectionService collectionService;

    @GetMapping("/page")
    @Operation(summary = "获得藏品变更记录分页")
    @PreAuthorize("@ss.hasPermission('app:collection-record:query')")
    public CommonResult<PageResult<CollectionRecordRespVO>> getCollectionRecordPage(@Valid CollectionRecordPageReqVO pageReqVO) {
        return success(collectionService.getCollectionRecordPage(pageReqVO));
    }

}