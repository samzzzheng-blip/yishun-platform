package com.techtron.onebook.module.app.dal.mysql.stonerecord;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.MPJLambdaWrapperX;
import com.techtron.onebook.module.app.controller.admin.stonerecord.vo.StoneRecordPageReqVO;
import com.techtron.onebook.module.app.controller.admin.stonerecord.vo.StoneRecordRespVO;
import com.techtron.onebook.module.app.dal.dataobject.stonerecord.StoneRecordDO;
import com.techtron.onebook.module.member.dal.dataobject.user.MemberUserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoneRecordMapper extends BaseMapperX<StoneRecordDO> {

    default PageResult<StoneRecordDO> selectUserPage(Long userId,
            com.techtron.onebook.module.app.controller.app.stonerecord.vo.AppStoneRecordPageReqVO req) {
        java.util.Objects.requireNonNull(userId, "登录后才能查看能量石明细");
        return selectPage(req, new LambdaQueryWrapper<StoneRecordDO>()
                .eq(StoneRecordDO::getUserId, userId)
                .gt("income".equals(req.getDirection()), StoneRecordDO::getAmount, 0)
                .lt("expense".equals(req.getDirection()), StoneRecordDO::getAmount, 0)
                .orderByDesc(StoneRecordDO::getCreateTime)
                .orderByDesc(StoneRecordDO::getId));
    }

    default PageResult<StoneRecordRespVO> selectPage(StoneRecordPageReqVO reqVO) {
        MPJLambdaWrapperX<StoneRecordDO> query = new MPJLambdaWrapperX<StoneRecordDO>()
                .selectAll(StoneRecordDO.class)
                .selectAs(MemberUserDO::getMobile, StoneRecordRespVO::getMobile)
                .leftJoin(MemberUserDO.class, MemberUserDO::getId, StoneRecordDO::getUserId)
                .eqIfPresent(StoneRecordDO::getUserId, reqVO.getUserId())
                .likeIfPresent(MemberUserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(StoneRecordDO::getType, reqVO.getType())
                .orderByDesc(StoneRecordDO::getCreateTime);
        return selectJoinPage(reqVO, StoneRecordRespVO.class, query);
    }

    default StoneRecordDO selectLatestByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapper<StoneRecordDO>()
                .eq(StoneRecordDO::getUserId, userId)
                .orderByDesc(StoneRecordDO::getId)
                .last("LIMIT 1"));
    }

}
