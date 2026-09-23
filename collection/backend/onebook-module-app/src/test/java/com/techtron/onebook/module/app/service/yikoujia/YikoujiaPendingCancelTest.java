package com.techtron.onebook.module.app.service.yikoujia;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import com.techtron.onebook.module.app.dal.mysql.ykjorder.YkjOrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class YikoujiaPendingCancelTest {
    @Test void pendingCancellationRestoresAvailableStockWithoutDeletingHoldings() {
        var listings = mock(YikoujiaMapper.class);
        var collections = mock(CollectionMapper.class);
        var orders = mock(YkjOrderMapper.class);
        var service = new YikoujiaServiceImpl();
        ReflectionTestUtils.setField(service, "yikoujiaMapper", listings);
        ReflectionTestUtils.setField(service, "collectionMapper", collections);
        ReflectionTestUtils.setField(service, "ykjOrderMapper", orders);
        when(listings.selectOne(any(Wrapper.class))).thenReturn(new YikoujiaDO().setId(2L).setUserId(674L).setCollectionId(3L).setStatus(0));
        when(orders.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(collections.selectForUpdate0(List.of(3L), 674L)).thenReturn(List.of(new CollectionDO().setId(3L).setRealStock(1).setStock(0).setGetbackStatus(0)));
        service.delistYikoujia(2L, 674L);
        verify(listings).updateById(argThat((YikoujiaDO row) -> row.getId() == 2L && row.getStatus() == 2));
        verify(collections).updateById(argThat((CollectionDO row) -> row.getId() == 3L && row.getStock() == 1 && row.getTradeStatus() == 0 && row.getRealStock() == null));
    }

    @Test void otherUserAndTerminalListingsCannotBeCancelled() {
        var listings = mock(YikoujiaMapper.class);
        var collections = mock(CollectionMapper.class);
        var service = new YikoujiaServiceImpl();
        ReflectionTestUtils.setField(service, "yikoujiaMapper", listings);
        ReflectionTestUtils.setField(service, "collectionMapper", collections);
        when(listings.selectOne(any(Wrapper.class))).thenReturn(new YikoujiaDO().setId(2L).setUserId(674L).setStatus(0));
        assertThrows(RuntimeException.class, () -> service.delistYikoujia(2L, 305L));
        for (Integer status : new Integer[]{null, 1, 2, 4, 5}) {
            when(listings.selectOne(any(Wrapper.class))).thenReturn(new YikoujiaDO().setId(2L).setUserId(674L).setStatus(status));
            assertThrows(RuntimeException.class, () -> service.delistYikoujia(2L, 674L));
        }
        verifyNoInteractions(collections);
    }
}
