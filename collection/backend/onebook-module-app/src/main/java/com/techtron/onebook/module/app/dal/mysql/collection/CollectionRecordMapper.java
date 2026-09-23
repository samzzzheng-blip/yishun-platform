package com.techtron.onebook.module.app.dal.mysql.collection;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.MPJLambdaWrapperX;
import com.techtron.onebook.module.app.controller.admin.collection.vo.CollectionRecordPageReqVO;
import com.techtron.onebook.module.app.controller.admin.collection.vo.CollectionRecordRespVO;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionRecordDO;
import com.techtron.onebook.module.member.dal.dataobject.user.MemberUserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 藏品变更记录 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface CollectionRecordMapper extends BaseMapperX<CollectionRecordDO> {

    default PageResult<CollectionRecordRespVO> selectPage(CollectionRecordPageReqVO reqVO) {
        MPJLambdaWrapperX<CollectionRecordDO> query = new MPJLambdaWrapperX<CollectionRecordDO>()
                .selectAll(CollectionRecordDO.class)
                .selectAs(MemberUserDO::getMobile, CollectionRecordRespVO::getMobile)
                .selectAs(CollectionCategoryDO::getName, CollectionRecordRespVO::getCategoryName)
                .leftJoin(MemberUserDO.class, MemberUserDO::getId, CollectionRecordDO::getUserId)
                .leftJoin(CollectionCategoryDO.class, CollectionCategoryDO::getId, CollectionRecordDO::getCategoryId)
                .likeIfPresent(MemberUserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(CollectionRecordDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(CollectionRecordDO::getType, reqVO.getType())
                .orderByDesc(CollectionRecordDO::getId);
        return selectJoinPage(reqVO, CollectionRecordRespVO.class, query);
    }

}