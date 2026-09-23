package com.techtron.onebook.module.app.dal.mysql.auction;

import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionBidDO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface AuctionBidMapper extends BaseMapperX<AuctionBidDO> {
    default AuctionBidDO selectByRequestId(Long bidderId, String requestId) {
        return selectOne(AuctionBidDO::getBidderId, bidderId, AuctionBidDO::getRequestId, requestId);
    }
    default List<AuctionBidDO> selectByAuctionId(Long auctionId) {
        return selectList(AuctionBidDO::getAuctionId, auctionId);
    }
}
