package com.techtron.onebook.module.app.dal.mysql.dealorder;

import java.util.*;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.module.app.dal.dataobject.dealorder.DealOrderDO;
import org.apache.ibatis.annotations.Mapper;
import com.techtron.onebook.module.app.controller.admin.dealorder.vo.*;

/**
 * 批量交易成交 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface DealOrderMapper extends BaseMapperX<DealOrderDO> {

    default PageResult<DealOrderDO> selectPage(DealOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DealOrderDO>()
                .eqIfPresent(DealOrderDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(DealOrderDO::getAmount, reqVO.getAmount())
                .eqIfPresent(DealOrderDO::getPrice, reqVO.getPrice())
                .betweenIfPresent(DealOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DealOrderDO::getId));
    }

}