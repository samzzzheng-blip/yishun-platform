package com.techtron.onebook.module.app.dal.mysql.ykjorder;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.module.app.controller.app.ykjorder.vo.YkjOrderPageReqVO;
import com.techtron.onebook.module.app.dal.dataobject.ykjorder.YkjOrderDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 一口价买单 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface YkjOrderMapper extends BaseMapperX<YkjOrderDO> {

    default PageResult<YkjOrderDO> selectPage(YkjOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<YkjOrderDO>()
                .eqIfPresent(YkjOrderDO::getUserId, reqVO.getUserId())
                .eqIfPresent(YkjOrderDO::getYkjId, reqVO.getYkjId())
                .eqIfPresent(YkjOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(YkjOrderDO::getPayOrderId, reqVO.getPayOrderId())
                .betweenIfPresent(YkjOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(YkjOrderDO::getId));
    }

}