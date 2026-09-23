package com.techtron.onebook.module.app.dal.mysql.category;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategoryPageReqVO;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 藏品分类 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface CollectionCategoryMapper extends BaseMapperX<CollectionCategoryDO> {

    default PageResult<CollectionCategoryDO> selectPage(CollectionCategoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CollectionCategoryDO>()
                .eqIfPresent(CollectionCategoryDO::getUserId, 0)
                .orderByDesc(CollectionCategoryDO::getId));
    }

}