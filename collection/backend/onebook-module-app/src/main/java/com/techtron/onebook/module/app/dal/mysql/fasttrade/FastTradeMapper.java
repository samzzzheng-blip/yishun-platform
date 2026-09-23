package com.techtron.onebook.module.app.dal.mysql.fasttrade;

import java.util.*;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.module.app.dal.dataobject.fasttrade.FastTradeDO;
import org.apache.ibatis.annotations.Mapper;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.*;

/**
 * 快速变现 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface FastTradeMapper extends BaseMapperX<FastTradeDO> {

    default PageResult<FastTradeDO> selectPage(FastTradePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FastTradeDO>()
                .eqIfPresent(FastTradeDO::getStatus, reqVO.getStatus())
                .orderByDesc(FastTradeDO::getId));
    }

}