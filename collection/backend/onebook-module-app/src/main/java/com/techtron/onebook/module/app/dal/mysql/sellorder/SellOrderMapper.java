package com.techtron.onebook.module.app.dal.mysql.sellorder;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.framework.mybatis.core.query.MPJLambdaWrapperX;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderRespVO;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.app.dal.dataobject.sellorder.SellOrderDO;
import com.techtron.onebook.module.member.dal.dataobject.user.MemberUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 批量交易卖单 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface SellOrderMapper extends BaseMapperX<SellOrderDO> {

    @Select("SELECT * FROM app_sell_order WHERE category_id = #{categoryId} " +
            "AND user_id != #{userId} " +
            "AND price <= #{price} " +
            "AND amount > 0 " +
            "AND deleted = false " +
            "ORDER BY price ASC, create_time ASC " +
            "FOR UPDATE")
    List<SellOrderDO> selectListForUpdate(@Param("categoryId") Long categoryId,
                                          @Param("userId") Long userId,
                                          @Param("price") Integer price
    );

    default PageResult<SellOrderRespVO> selectPage(SellOrderPageReqVO reqVO) {
        MPJLambdaWrapperX<SellOrderDO> query = new MPJLambdaWrapperX<SellOrderDO>()
                .selectAll(SellOrderDO.class)
                .selectAs(MemberUserDO::getMobile, SellOrderRespVO::getMobile)
                .selectAs(CollectionCategoryDO::getName, SellOrderRespVO::getCategoryName)
                .leftJoin(MemberUserDO.class, MemberUserDO::getId, SellOrderDO::getUserId)
                .leftJoin(CollectionCategoryDO.class, CollectionCategoryDO::getId, SellOrderDO::getCategoryId)
                .eqIfPresent(SellOrderDO::getUserId, reqVO.getUserId())
                .likeIfPresent(MemberUserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(SellOrderDO::getCategoryId, reqVO.getCategoryId())
                .orderByDesc(SellOrderDO::getId);
        return selectJoinPage(reqVO, SellOrderRespVO.class, query);
    }

}