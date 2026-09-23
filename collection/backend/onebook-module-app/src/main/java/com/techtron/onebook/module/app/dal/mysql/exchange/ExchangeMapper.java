package com.techtron.onebook.module.app.dal.mysql.exchange;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.module.app.controller.admin.exchange.vo.ExchangePageReqVO;
import com.techtron.onebook.module.app.dal.dataobject.exchange.ExchangeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 兑换品 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface ExchangeMapper extends BaseMapperX<ExchangeDO> {

    default PageResult<ExchangeDO> selectPage(ExchangePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ExchangeDO>()
                .likeIfPresent(ExchangeDO::getName, reqVO.getName())
                .orderByAsc(ExchangeDO::getOrdinalPosition)
                .orderByDesc(ExchangeDO::getId));
    }

}