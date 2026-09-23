package com.techtron.onebook.module.app.controller.app.category;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategoryPageReqVO;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategorySaveReqVO;
import com.techtron.onebook.module.app.controller.app.category.vo.AppCollectionCategoryReqVO;
import com.techtron.onebook.module.app.controller.app.category.vo.AppCollectionCategoryRespVO;
import com.techtron.onebook.module.app.controller.app.category.vo.AppTradeInfoRespVO;
import com.techtron.onebook.module.app.controller.app.category.vo.ChangeCategoryReqVO;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.app.service.category.CollectionCategoryService;
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

@Tag(name = "用户 APP - 藏品分类")
@RestController
@RequestMapping("/app/collection-category")
@Validated
public class AppCollectionCategoryController {

    @Resource
    private CollectionCategoryService collectionCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建藏品分类")
    public CommonResult<Long> createCollectionCategory(@Valid @RequestBody CollectionCategorySaveReqVO createReqVO) {
        createReqVO.setUserId(getLoginUserId());
        return success(collectionCategoryService.createCollectionCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新藏品分类")
    public CommonResult<Boolean> updateCollectionCategory(@Valid @RequestBody CollectionCategorySaveReqVO updateReqVO) {
        collectionCategoryService.updateCollectionCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除藏品分类")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteCollectionCategory(@RequestParam("id") Long id) {
        collectionCategoryService.deleteCollectionCategory(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除藏品分类")
    public CommonResult<Boolean> deleteCollectionCategoryList(@RequestParam("ids") List<Long> ids) {
        collectionCategoryService.deleteCollectionCategoryListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得藏品分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppCollectionCategoryRespVO> getCollectionCategory(@RequestParam("id") Long id) {
        CollectionCategoryDO collectionCategory = collectionCategoryService.getCollectionCategory(id);
        return success(BeanUtils.toBean(collectionCategory, AppCollectionCategoryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得藏品分类分页")
    public CommonResult<PageResult<AppCollectionCategoryRespVO>> getCollectionCategoryPage(@Valid CollectionCategoryPageReqVO pageReqVO) {
        PageResult<CollectionCategoryDO> pageResult = collectionCategoryService.getCollectionCategoryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AppCollectionCategoryRespVO.class));
    }

    @GetMapping("/my-list")
    @Operation(summary = "获得藏品分类列表")
    public CommonResult<List<AppCollectionCategoryRespVO>> getProductCategoryList(AppCollectionCategoryReqVO reqVO) {
        reqVO.setUserId(getLoginUserId());
        return success(collectionCategoryService.getCollectionCategoryList(reqVO));
    }

    @GetMapping("/list")
    @Operation(summary = "获得藏品分类列表")
    @PermitAll
    public CommonResult<List<AppCollectionCategoryRespVO>> getProductCategoryList() {
        List<CollectionCategoryDO> list = collectionCategoryService.getCollectionCategoryList(getLoginUserId());
        return success(BeanUtils.toBean(list, AppCollectionCategoryRespVO.class));
    }

    @GetMapping("/get-latest-price")
    @PermitAll
    public CommonResult<Integer> getLatestPrice(@RequestParam("id") Long id) {
        int price = collectionCategoryService.getLatestPrice(id);
        return success(price);
    }

    @GetMapping("/get-trade-info")
    @PermitAll
    public CommonResult<AppTradeInfoRespVO> getTradeInfo(@RequestParam("id") Long id) {
        return success(collectionCategoryService.getTradeInfo(id));
    }

    @PutMapping("/change-category")
    @Operation(summary = "批量修改藏品分类")
    public CommonResult<Boolean> changeCategory(@Valid @RequestBody ChangeCategoryReqVO reqVO) {
        collectionCategoryService.changeCategory(reqVO.getCategoryId(), reqVO.getCollectionIds());
        return success(true);
    }

}