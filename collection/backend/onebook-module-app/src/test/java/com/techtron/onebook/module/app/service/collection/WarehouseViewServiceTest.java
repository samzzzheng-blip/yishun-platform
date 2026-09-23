package com.techtron.onebook.module.app.service.collection;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionDO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class WarehouseViewServiceTest {
    @Test void projectsLockedHoldingsWithoutRestoringDatabaseStockOrAuctionOwner() {
        for (Class<?> type : List.of(CollectionDO.class, AuctionDO.class, YikoujiaDO.class)) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"), type);
        }
        var collections = mock(CollectionMapper.class);
        var auctions = mock(AuctionMapper.class);
        var listings = mock(YikoujiaMapper.class);
        var service = new WarehouseViewService();
        ReflectionTestUtils.setField(service, "collections", collections);
        ReflectionTestUtils.setField(service, "auctions", auctions);
        ReflectionTestUtils.setField(service, "listings", listings);
        var pending = new CollectionDO().setId(10L).setUserId(674L).setStock(0).setRealStock(1);
        var auction = new CollectionDO().setId(11L).setUserId(0L).setStock(1).setRealStock(1);
        when(auctions.selectList(any(Wrapper.class))).thenReturn(List.of(new AuctionDO().setId(20L).setCollectionId(11L).setSellerId(674L).setStatus(1)));
        when(collections.selectList(any(Wrapper.class))).thenAnswer(invocation -> {
            String sql = ((Wrapper<?>) invocation.getArgument(0)).getSqlSegment();
            assertTrue(sql.contains("real_stock >"));
            assertFalse(sql.contains(" AND stock >"));
            assertTrue(sql.contains("getback_status <>"));
            assertTrue(sql.contains("user_id ="));
            assertTrue(sql.contains(" OR (user_id ="));
            return List.of(pending, auction);
        });
        when(listings.selectList(any(Wrapper.class))).thenReturn(List.of(new YikoujiaDO().setId(30L).setCollectionId(10L).setUserId(674L).setStatus(0)));
        var result = service.list(674L);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getStock());
        assertEquals(0, result.get(0).getAvailableStock());
        assertEquals("direct", result.get(0).getSaleType());
        assertEquals(0, result.get(0).getListingStatus());
        assertEquals("auction", result.get(1).getSaleType());
        assertEquals(674L, result.get(1).getUserId());
        assertEquals(0, pending.getStock());
        assertEquals(0L, auction.getUserId());
        verify(collections).selectList(any(Wrapper.class));
        verify(auctions).selectList(any(Wrapper.class));
        verify(listings).selectList(any(Wrapper.class));
        verifyNoMoreInteractions(collections, auctions, listings);
    }
}
