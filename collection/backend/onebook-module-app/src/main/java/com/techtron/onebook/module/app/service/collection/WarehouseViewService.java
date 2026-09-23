package com.techtron.onebook.module.app.service.collection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.app.collection.vo.AppCollectionRespVO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionDO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.*;

/** Read-only ownership projection. Never restores locked stock or changes auction custody. */
@Service
public class WarehouseViewService {
    @Resource private CollectionMapper collections;
    @Resource private AuctionMapper auctions;
    @Resource private YikoujiaMapper listings;

    public List<AppCollectionRespVO> list(Long userId) {
        if (userId == null || userId <= 0) return List.of();
        List<AuctionDO> active = auctions.selectList(new LambdaQueryWrapper<AuctionDO>()
            .eq(AuctionDO::getSellerId, userId).in(AuctionDO::getStatus, 0, 1, 2, 6)
            .orderByDesc(AuctionDO::getId));
        List<Long> auctionIds = active.stream().map(AuctionDO::getCollectionId).filter(Objects::nonNull).toList();
        LambdaQueryWrapper<CollectionDO> query = new LambdaQueryWrapper<CollectionDO>()
            .eq(CollectionDO::getStatus, 1).gt(CollectionDO::getRealStock, 0)
            .and(q -> q.isNull(CollectionDO::getGetbackStatus).or().ne(CollectionDO::getGetbackStatus, 2))
            .and(q -> {
                q.eq(CollectionDO::getUserId, userId);
                if (!auctionIds.isEmpty()) q.or(x -> x.eq(CollectionDO::getUserId, 0L).in(CollectionDO::getId, auctionIds));
            }).orderByDesc(CollectionDO::getId);
        List<CollectionDO> held = collections.selectList(query);
        if (held.isEmpty()) return List.of();
        List<YikoujiaDO> direct = listings.selectList(new LambdaQueryWrapper<YikoujiaDO>()
            .eq(YikoujiaDO::getUserId, userId).in(YikoujiaDO::getStatus, 0, 3)
            .in(YikoujiaDO::getCollectionId, held.stream().map(CollectionDO::getId).toList())
            .orderByDesc(YikoujiaDO::getId));
        Map<Long,YikoujiaDO> directMap = new HashMap<>();
        direct.forEach(item -> directMap.putIfAbsent(item.getCollectionId(), item));
        Map<Long,AuctionDO> auctionMap = new HashMap<>();
        active.forEach(item -> auctionMap.putIfAbsent(item.getCollectionId(), item));
        return held.stream().map(item -> {
            AppCollectionRespVO view = BeanUtils.toBean(item, AppCollectionRespVO.class);
            view.setAvailableStock(item.getStock());
            view.setStock(item.getRealStock());
            view.setUserId(userId);
            AuctionDO auction = auctionMap.get(item.getId());
            YikoujiaDO listing = directMap.get(item.getId());
            if (auction != null && Objects.equals(item.getUserId(), 0L)) {
                view.setSaleType("auction"); view.setListingId(auction.getId()); view.setListingStatus(auction.getStatus());
            } else if (listing != null) {
                view.setSaleType("direct"); view.setListingId(listing.getId()); view.setListingStatus(listing.getStatus());
            }
            return view;
        }).toList();
    }
}
