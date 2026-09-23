package com.techtron.onebook.module.app.service.storageplan;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.storageplan.vo.StoragePlanPageReqVO;
import com.techtron.onebook.module.app.controller.admin.storageplan.vo.StoragePlanSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.storageplan.StoragePlanDO;
import jakarta.validation.Valid;

import java.util.List;

public interface StoragePlanService {

    Long createStoragePlan(@Valid StoragePlanSaveReqVO createReqVO);

    void updateStoragePlan(@Valid StoragePlanSaveReqVO updateReqVO);

    void deleteStoragePlan(Long id);

    StoragePlanDO getStoragePlan(Long id);

    PageResult<StoragePlanDO> getStoragePlanPage(StoragePlanPageReqVO pageReqVO);

    List<StoragePlanDO> getEnabledStoragePlanList();

    Long getStoragePlanIdByRealAmount(Integer realAmount);

    Boolean purchaseStoragePlan(Long userId, Long planId, Integer type);

    Integer autoRenewExpireStorage();

}