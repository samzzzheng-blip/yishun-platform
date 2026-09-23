package com.techtron.onebook.module.app.dal.mysql.buyorder;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.framework.mybatis.core.query.MPJLambdaWrapperX;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderRespVO;
import com.techtron.onebook.module.app.dal.dataobject.buyorder.BuyOrderDO;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.member.dal.dataobject.user.MemberUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 批量交易买单 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface BuyOrderMapper extends BaseMapperX<BuyOrderDO> {

    @Select("SELECT * FROM app_buy_order WHERE category_id = #{categoryId} " +
            "AND user_id != #{userId} " +
            "AND price >= #{price} " +
            "AND amount > 0 AND status = 1 " +
            "AND deleted = false " +
            "ORDER BY price DESC, create_time ASC " +
            "FOR UPDATE")
    List<BuyOrderDO> selectListForUpdate(@Param("categoryId") Long categoryId,
                                          @Param("userId") Long userId,
                                          @Param("price") Integer price
                                          );

    default PageResult<BuyOrderRespVO> selectPage(BuyOrderPageReqVO reqVO) {
        MPJLambdaWrapperX<BuyOrderDO> query = new MPJLambdaWrapperX<BuyOrderDO>()
                .selectAll(BuyOrderDO.class)
                .selectAs(MemberUserDO::getMobile, BuyOrderRespVO::getMobile)
                .selectAs(CollectionCategoryDO::getName, BuyOrderRespVO::getCategoryName)
                .leftJoin(MemberUserDO.class, MemberUserDO::getId, BuyOrderDO::getUserId)
                .leftJoin(CollectionCategoryDO.class, CollectionCategoryDO::getId, BuyOrderDO::getCategoryId)
                .eqIfPresent(BuyOrderDO::getUserId, reqVO.getUserId())
                .likeIfPresent(MemberUserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(BuyOrderDO::getCategoryId, reqVO.getCategoryId())
                .eq(BuyOrderDO::getStatus, 1)
                .orderByDesc(BuyOrderDO::getId);
        return selectJoinPage(reqVO, BuyOrderRespVO.class, query);
    }


}