package com.techtron.onebook.module.app.dal.mysql.ads;

import java.util.*;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.module.app.dal.dataobject.ads.AdsDO;
import org.apache.ibatis.annotations.Mapper;
import com.techtron.onebook.module.app.controller.admin.ads.vo.*;

/**
 * 广告 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface AdsMapper extends BaseMapperX<AdsDO> {

    default PageResult<AdsDO> selectPage(AdsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AdsDO>()
                .eqIfPresent(AdsDO::getPicUrl, reqVO.getPicUrl())
                .eqIfPresent(AdsDO::getStatus, reqVO.getStatus())
                .orderByDesc(AdsDO::getId));
    }

}