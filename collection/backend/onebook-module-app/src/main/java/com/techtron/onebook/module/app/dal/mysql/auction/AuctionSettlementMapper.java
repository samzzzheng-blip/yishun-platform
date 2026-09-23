package com.techtron.onebook.module.app.dal.mysql.auction;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionSettlementDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuctionSettlementMapper extends BaseMapperX<AuctionSettlementDO> {
    default AuctionSettlementDO selectByAuctionId(Long auctionId) {
        return selectOne(new LambdaQueryWrapper<AuctionSettlementDO>()
                .eq(AuctionSettlementDO::getAuctionId, auctionId));
    }
}
