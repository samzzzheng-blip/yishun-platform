package com.techtron.onebook.module.app.service.collection;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.collection.vo.*;
import com.techtron.onebook.module.app.controller.app.collection.vo.AppCollectionListReqVO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 藏品登记 Service 接口
 *
 * @author 超级管理员
 */
public interface CollectionService {

    /**
     * 创建藏品登记
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCollection(@Valid CollectionSaveReqVO createReqVO);

    Long createCollection(@Valid CollectionAdminSaveReqVO createReqVO);

    /**
     * 更新藏品登记
     *
     * @param updateReqVO 更新信息
     */
    void auditCollection(@Valid CollectionUpdateReqVO updateReqVO);

    void updateCollection(@Valid CollectionSaveReqVO updateReqVO);

    /**
     * 删除藏品登记
     *
     * @param id 编号
     */
    void deleteCollection(Long id);

    /**
    * 批量删除藏品登记
    *
    * @param ids 编号
    */
    void deleteCollectionListByIds(List<Long> ids);

    /**
     * 获得藏品登记
     *
     * @param id 编号
     * @return 藏品登记
     */
    CollectionDO getCollection(Long id);

    /**
     * 获得藏品登记分页
     *
     * @param pageReqVO 分页查询
     * @return 藏品登记分页
     */
    PageResult<CollectionRespVO> getCollectionPage(CollectionPageReqVO pageReqVO);

    List<CollectionDO> getCollectionList(AppCollectionListReqVO listReqVO);

    List<CollectionDO> getRealCollectionList(AppCollectionListReqVO listReqVO);

    Integer getRealAmountByUserId(Long userId);

    /**
     * 获得藏品变更记录分页
     *
     * @param pageReqVO 分页查询
     * @return 藏品变更记录分页
     */
    PageResult<CollectionRecordRespVO> getCollectionRecordPage(CollectionRecordPageReqVO pageReqVO);

}