package com.techtron.onebook.module.app.dal.mysql.getback;

import java.util.*;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.module.app.dal.dataobject.getback.GetbackDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.techtron.onebook.module.app.controller.admin.getback.vo.*;

/**
 * 取回 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface GetbackMapper extends BaseMapperX<GetbackDO> {

    @Select("SELECT * FROM app_getback WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    GetbackDO selectByIdForUpdate(Long id);

    default PageResult<GetbackDO> selectPage(GetbackPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GetbackDO>()
                .eqIfPresent(GetbackDO::getStatus, reqVO.getStatus())
                .orderByDesc(GetbackDO::getId));
    }

    default List<GetbackDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<GetbackDO>()
                .eq(GetbackDO::getUserId, userId)
                .orderByDesc(GetbackDO::getId));
    }

}
