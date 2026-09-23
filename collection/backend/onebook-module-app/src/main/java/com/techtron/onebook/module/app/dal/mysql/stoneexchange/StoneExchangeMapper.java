package com.techtron.onebook.module.app.dal.mysql.stoneexchange;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.module.app.controller.admin.stoneexchange.vo.StoneExchangePageReqVO;
import com.techtron.onebook.module.app.dal.dataobject.stoneexchange.StoneExchangeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 能量石兑换 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface StoneExchangeMapper extends BaseMapperX<StoneExchangeDO> {

    default PageResult<StoneExchangeDO> selectPage(StoneExchangePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StoneExchangeDO>()
                .orderByDesc(StoneExchangeDO::getId));
    }

}