package com.techtron.onebook.module.app.dal.mysql.exchangelog;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.module.app.controller.admin.exchangelog.vo.ExchangeLogPageReqVO;
import com.techtron.onebook.module.app.dal.dataobject.exchangelog.ExchangeLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 兑换记录 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface ExchangeLogMapper extends BaseMapperX<ExchangeLogDO> {

    default PageResult<ExchangeLogDO> selectPage(ExchangeLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ExchangeLogDO>()
                .eqIfPresent(ExchangeLogDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ExchangeLogDO::getMobile, reqVO.getMobile())
                .eqIfPresent(ExchangeLogDO::getUserId, reqVO.getUserId())
                .orderByDesc(ExchangeLogDO::getId));
    }

}