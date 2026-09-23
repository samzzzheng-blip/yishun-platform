package com.techtron.onebook.module.app.controller.admin.collection;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.collection.vo.*;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.service.collection.CollectionService;
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
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 藏品登记")
@RestController
@RequestMapping("/app/collection")
@Validated
public class CollectionController {

    @Resource
    private CollectionService collectionService;

    @PostMapping("/create")
    @Operation(summary = "创建藏品登记")
    public CommonResult<Long> createCollection(@Valid @RequestBody CollectionAdminSaveReqVO createReqVO) {
        createReqVO.setCreatorUserName(getLoginUserNickname());
        return success(collectionService.createCollection(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新藏品登记")
    @PreAuthorize("@ss.hasPermission('app:collection:update')")
    public CommonResult<Boolean> updateCollection(@Valid @RequestBody CollectionSaveReqVO updateReqVO) {
        collectionService.updateCollection(updateReqVO);
        return success(true);
    }

    @PutMapping("/audit")
    @Operation(summary = "审核藏品登记")
    @PreAuthorize("@ss.hasPermission('app:collection:update')")
    public CommonResult<Boolean> updateCollection(@Valid @RequestBody CollectionUpdateReqVO updateReqVO) {
        collectionService.auditCollection(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除藏品登记")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteCollection(@RequestParam("id") Long id) {
        collectionService.deleteCollection(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除藏品登记")
                @PreAuthorize("@ss.hasPermission('app:collection:delete')")
    public CommonResult<Boolean> deleteCollectionList(@RequestParam("ids") List<Long> ids) {
        collectionService.deleteCollectionListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得藏品登记")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:collection:query')")
    public CommonResult<CollectionRespVO> getCollection(@RequestParam("id") Long id) {
        CollectionDO collection = collectionService.getCollection(id);
        return success(BeanUtils.toBean(collection, CollectionRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得藏品登记分页")
    @PreAuthorize("@ss.hasPermission('app:collection:query')")
    public CommonResult<PageResult<CollectionRespVO>> getCollectionPage(@Valid CollectionPageReqVO pageReqVO) {
        return success(collectionService.getCollectionPage(pageReqVO));
    }
    

}