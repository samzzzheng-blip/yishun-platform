package com.techtron.onebook.module.app.dal.mysql.storageplan;

import java.util.*;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.module.app.dal.dataobject.storageplan.StoragePlanDO;
import org.apache.ibatis.annotations.Mapper;
import com.techtron.onebook.module.app.controller.admin.storageplan.vo.*;

@Mapper
public interface StoragePlanMapper extends BaseMapperX<StoragePlanDO> {

    default PageResult<StoragePlanDO> selectPage(StoragePlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StoragePlanDO>());
    }

    default List<StoragePlanDO> selectListAll() {
        return selectList(new LambdaQueryWrapperX<StoragePlanDO>());
    }

}