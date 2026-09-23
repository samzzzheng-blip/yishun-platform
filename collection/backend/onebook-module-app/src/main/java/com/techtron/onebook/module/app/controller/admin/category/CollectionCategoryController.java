package com.techtron.onebook.module.app.controller.admin.category;

import com.techtron.onebook.framework.apilog.core.annotation.ApiAccessLog;
import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageParam;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.framework.excel.core.util.ExcelUtils;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategoryPageReqVO;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategoryRespVO;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategorySaveReqVO;
import com.techtron.onebook.module.app.controller.app.category.vo.AppCollectionCategoryRespVO;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.app.service.category.CollectionCategoryService;
import com.techtron.onebook.module.member.api.user.MemberUserApi;
import com.techtron.onebook.module.member.api.user.dto.MemberUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.techtron.onebook.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 藏品分类")
@RestController
@RequestMapping("/app/collection-category")
@Validated
public class CollectionCategoryController {

    @Resource
    private CollectionCategoryService collectionCategoryService;

    @Resource
    private MemberUserApi memberUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建藏品分类")
    @PreAuthorize("@ss.hasPermission('app:collection-category:create')")
    public CommonResult<Long> createCollectionCategory(@Valid @RequestBody CollectionCategorySaveReqVO createReqVO) {
        return success(collectionCategoryService.createCollectionCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新藏品分类")
    @PreAuthorize("@ss.hasPermission('app:collection-category:update')")
    public CommonResult<Boolean> updateCollectionCategory(@Valid @RequestBody CollectionCategorySaveReqVO updateReqVO) {
        collectionCategoryService.updateCollectionCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除藏品分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('app:collection-category:delete')")
    public CommonResult<Boolean> deleteCollectionCategory(@RequestParam("id") Long id) {
        collectionCategoryService.deleteCollectionCategory(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除藏品分类")
                @PreAuthorize("@ss.hasPermission('app:collection-category:delete')")
    public CommonResult<Boolean> deleteCollectionCategoryList(@RequestParam("ids") List<Long> ids) {
        collectionCategoryService.deleteCollectionCategoryListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得藏品分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:collection-category:query')")
    public CommonResult<CollectionCategoryRespVO> getCollectionCategory(@RequestParam("id") Long id) {
        CollectionCategoryDO collectionCategory = collectionCategoryService.getCollectionCategory(id);
        return success(BeanUtils.toBean(collectionCategory, CollectionCategoryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得藏品分类分页")
    @PreAuthorize("@ss.hasPermission('app:collection-category:query')")
    public CommonResult<PageResult<CollectionCategoryRespVO>> getCollectionCategoryPage(@Valid CollectionCategoryPageReqVO pageReqVO) {
        PageResult<CollectionCategoryDO> pageResult = collectionCategoryService.getCollectionCategoryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CollectionCategoryRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出藏品分类 Excel")
    @PreAuthorize("@ss.hasPermission('app:collection-category:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCollectionCategoryExcel(@Valid CollectionCategoryPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CollectionCategoryDO> list = collectionCategoryService.getCollectionCategoryPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "藏品分类.xls", "数据", CollectionCategoryRespVO.class,
                        BeanUtils.toBean(list, CollectionCategoryRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得藏品分类列表")
    public CommonResult<List<AppCollectionCategoryRespVO>> getProductCategoryList(@RequestParam("id") Long id) {
        List<CollectionCategoryDO> list = collectionCategoryService.getCollectionCategoryList(id);
        return success(BeanUtils.toBean(list, AppCollectionCategoryRespVO.class));
    }

    @GetMapping("/list-by-user")
    @Operation(summary = "获得藏品分类列表")
    public CommonResult<List<AppCollectionCategoryRespVO>> getProductCategoryList(@RequestParam("id") String id) {
        MemberUserRespDTO user = memberUserApi.getUserByMobile(id);
        List<CollectionCategoryDO> list = collectionCategoryService.getCollectionCategoryList(user.getId());
        return success(BeanUtils.toBean(list, AppCollectionCategoryRespVO.class));
    }

}