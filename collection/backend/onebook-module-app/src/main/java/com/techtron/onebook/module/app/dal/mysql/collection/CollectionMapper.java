package com.techtron.onebook.module.app.dal.mysql.collection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.collection.CollUtil;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.framework.mybatis.core.query.MPJLambdaWrapperX;
import com.techtron.onebook.module.app.controller.admin.collection.vo.CollectionPageReqVO;
import com.techtron.onebook.module.app.controller.admin.collection.vo.CollectionRespVO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.member.dal.dataobject.user.MemberUserDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Select;

/**
 * 藏品登记 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface CollectionMapper extends BaseMapperX<CollectionDO> {

    @Select("SELECT * FROM app_collection WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    CollectionDO selectByIdForUpdate(Long id);

    default PageResult<CollectionRespVO> selectPage(CollectionPageReqVO reqVO) {
        MPJLambdaWrapperX<CollectionDO> query = new MPJLambdaWrapperX<CollectionDO>()
                .selectAll(CollectionDO.class)
                .selectAs(MemberUserDO::getMobile, CollectionRespVO::getMobile)
                .leftJoin(MemberUserDO.class, MemberUserDO::getId, CollectionDO::getUserId)
                .likeIfPresent(CollectionDO::getName, reqVO.getName())
                .eqIfPresent(CollectionDO::getUserId, reqVO.getUserId())
                .eqIfPresent(CollectionDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(CollectionDO::getStatus, reqVO.getStatus())
                .eqIfPresent(MemberUserDO::getMobile, reqVO.getUserName())
                .orderByDesc(CollectionDO::getId);
        return selectJoinPage(reqVO, CollectionRespVO.class, query);
    }

    default List<CollectionDO> selectForUpdate(List<Long> collectionIds, Long userId) {
        return selectList(new LambdaQueryWrapper<CollectionDO>()
                .in(CollectionDO::getId, collectionIds)
                .eq(CollectionDO::getUserId, userId)
                .eq(CollectionDO::getStatus, 1)
                .gt(CollectionDO::getStock, 0)
                .last("FOR UPDATE"));
    }

    default List<CollectionDO> selectForUpdate0(List<Long> collectionIds, Long userId) {
        return selectList(new LambdaQueryWrapperX<CollectionDO>()
                .in(CollectionDO::getId, collectionIds)
                .eqIfPresent(CollectionDO::getUserId, userId)
                .eq(CollectionDO::getStatus, 1)
                .last("FOR UPDATE"));
    }

    default List<CollectionDO> selectForUpdate1(List<Long> collectionIds) {
        return selectList(new LambdaQueryWrapper<CollectionDO>()
                .in(CollectionDO::getId, collectionIds)
                .eq(CollectionDO::getStatus, 1)
                .gt(CollectionDO::getStock, 0)
                .last("FOR UPDATE"));
    }

    default Integer selectRealAmountByUserId(Long userId) {
        List<Map<String, Object>> result = selectMaps(new MPJLambdaWrapperX<CollectionDO>()
                .select("COALESCE(SUM(real_stock), 0) AS realAmount")
                .eq(CollectionDO::getUserId, userId));
        if (CollUtil.isEmpty(result)) {
            return 0;
        }
        Object value = result.get(0).get("realAmount");
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

}
