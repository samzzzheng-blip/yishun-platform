package com.techtron.onebook.module.app.service.category;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategoryPageReqVO;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategorySaveReqVO;
import com.techtron.onebook.module.app.controller.app.category.vo.AppCollectionCategoryReqVO;
import com.techtron.onebook.module.app.controller.app.category.vo.AppCollectionCategoryRespVO;
import com.techtron.onebook.module.app.controller.app.category.vo.AppTradeInfoRespVO;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 藏品分类 Service 接口
 *
 * @author 超级管理员
 */
public interface CollectionCategoryService {
    CollectionCategoryDO getPurchaseCategory(Long sourceCategoryId);

    /**
     * 创建藏品分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCollectionCategory(@Valid CollectionCategorySaveReqVO createReqVO);

    /**
     * 更新藏品分类
     *
     * @param updateReqVO 更新信息
     */
    void updateCollectionCategory(@Valid CollectionCategorySaveReqVO updateReqVO);

    /**
     * 删除藏品分类
     *
     * @param id 编号
     */
    void deleteCollectionCategory(Long id);

    /**
    * 批量删除藏品分类
    *
    * @param ids 编号
    */
    void deleteCollectionCategoryListByIds(List<Long> ids);

    /**
     * 获得藏品分类
     *
     * @param id 编号
     * @return 藏品分类
     */
    CollectionCategoryDO getCollectionCategory(Long id);

    /**
     * 获得藏品分类分页
     *
     * @param pageReqVO 分页查询
     * @return 藏品分类分页
     */
    PageResult<CollectionCategoryDO> getCollectionCategoryPage(CollectionCategoryPageReqVO pageReqVO);

    List<CollectionCategoryDO> getCollectionCategoryList(Long userId);

    List<AppCollectionCategoryRespVO> getCollectionCategoryList(AppCollectionCategoryReqVO reqVO);

    int getLatestPrice(Long id);

    AppTradeInfoRespVO getTradeInfo(Long id);

    void changeCategory(Long categoryId, List<Long> collectionIds);

}
