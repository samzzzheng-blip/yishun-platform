package com.techtron.onebook.module.app.dal.mysql.auction;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.module.app.controller.app.auction.vo.AppAuctionPageReqVO;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.time.LocalDateTime;

@Mapper
public interface AuctionMapper extends BaseMapperX<AuctionDO> {
    @Select("SELECT * FROM app_auction WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    AuctionDO selectByIdForUpdate(Long id);

    @Update("UPDATE app_auction SET delist_status = 1, delist_apply_time = NOW(3), " +
            "delist_audit_time = NULL, delist_audit_user_id = NULL, delist_reject_reason = NULL, " +
            "update_time = NOW() WHERE id = #{id} AND deleted = 0")
    void markDelistRequested(Long id);

    default long countActiveBySeller(Long sellerId) {
        return selectCount(new LambdaQueryWrapper<AuctionDO>()
                .eq(AuctionDO::getSellerId, sellerId).in(AuctionDO::getStatus, 0, 1, 6));
    }

    @Select("SELECT id FROM app_auction WHERE deleted = 0 AND " +
            "status = 1 AND end_time <= NOW() " +
            "ORDER BY end_time ASC LIMIT #{limit}")
    List<AuctionDO> selectSyncIds(int limit);

    // 成功查询至少间隔10秒，失败退避60秒；优先处理最久未检查的记录。
    @Select("SELECT id FROM app_auction WHERE deleted = 0 AND status = 1 " +
            "AND goofish_managed_product_id IS NOT NULL AND goofish_managed_product_id <> '' " +
            "AND (last_sync_time IS NULL OR last_sync_time <= DATE_SUB(#{now}, INTERVAL " +
            "IF(sync_error LIKE '%查询失败%', 60, 10) SECOND)) " +
            "ORDER BY last_sync_time ASC, id ASC LIMIT #{limit}")
    List<AuctionDO> selectManagedSyncIds(@Param("limit") int limit, @Param("now") LocalDateTime now);

    default PageResult<AuctionDO> selectPage(AppAuctionPageReqVO req) {
        LambdaQueryWrapperX<AuctionDO> query = new LambdaQueryWrapperX<AuctionDO>()
                .eqIfPresent(AuctionDO::getStatus, req.getStatus())
                .eqIfPresent(AuctionDO::getDelistStatus, req.getDelistStatus())
                .likeIfPresent(AuctionDO::getCollectionName, req.getKeyword());
        boolean asc = Boolean.TRUE.equals(req.getSortAsc());
        if ("currentPrice".equals(req.getSortField())) query.orderBy(true, asc, AuctionDO::getCurrentPrice);
        else if ("endTime".equals(req.getSortField())) query.orderBy(true, asc, AuctionDO::getEndTime);
        else query.orderByDesc(AuctionDO::getCreateTime);
        return selectPage(req, query);
    }
}
