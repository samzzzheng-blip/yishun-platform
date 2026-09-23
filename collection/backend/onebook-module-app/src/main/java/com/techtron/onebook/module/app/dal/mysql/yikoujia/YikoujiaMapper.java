package com.techtron.onebook.module.app.dal.mysql.yikoujia;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaPageReqVO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 一口价 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface YikoujiaMapper extends BaseMapperX<YikoujiaDO> {

    default PageResult<YikoujiaDO> selectPage(YikoujiaPageReqVO reqVO) {
        LambdaQueryWrapperX<YikoujiaDO> query = new LambdaQueryWrapperX<YikoujiaDO>()
                .eqIfPresent(YikoujiaDO::getStatus, reqVO.getStatus())
                .likeIfPresent(YikoujiaDO::getName, reqVO.getKeyword());

        if (Boolean.TRUE.equals(reqVO.getPendingSettlement())) {
            query.eq(YikoujiaDO::getStatus, 4).isNotNull(YikoujiaDO::getCollectionId);
        }

        if (reqVO.getStatusList() != null) {
            if (reqVO.getStatusList() == 0) {
                query.in(YikoujiaDO::getStatus, Arrays.asList(1, 3, 4));
            } else if (reqVO.getStatusList() == 1) {
                query.in(YikoujiaDO::getStatus, List.of(3));
            } else {
                query.in(YikoujiaDO::getStatus, Arrays.asList(1,4));
            }
        }

        if (Objects.equals(reqVO.getSortField(), YikoujiaPageReqVO.SORT_FIELD_PRICE)) {
            query.orderBy(true, reqVO.getSortAsc(), YikoujiaDO::getPrice)
                    .orderByDesc(YikoujiaDO::getId);
        } else if (Objects.equals(reqVO.getSortField(), YikoujiaPageReqVO.SORT_FIELD_CREATE_TIME)) {
            query.orderBy(true, reqVO.getSortAsc(), YikoujiaDO::getCreateTime)
                    .orderByDesc(YikoujiaDO::getId);
        } else {
            query.orderByDesc(YikoujiaDO::getId);
        }
        return selectPage(reqVO, query);
    }

}