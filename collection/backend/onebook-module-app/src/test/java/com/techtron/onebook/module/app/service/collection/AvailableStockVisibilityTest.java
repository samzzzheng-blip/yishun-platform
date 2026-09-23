package com.techtron.onebook.module.app.service.collection;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.techtron.onebook.module.app.controller.app.collection.vo.AppCollectionListReqVO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AvailableStockVisibilityTest {
    @Test void appListRequiresPositiveAvailableAndPhysicalStock() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "visibility"), CollectionDO.class);
        var mapper = mock(CollectionMapper.class);
        var service = new CollectionServiceImpl();
        ReflectionTestUtils.setField(service, "collectionMapper", mapper);
        when(mapper.selectList(any(Wrapper.class))).thenAnswer(invocation -> {
            Wrapper<?> query = invocation.getArgument(0);
            String sql = query.getSqlSegment();
            assertTrue(sql.matches("(?s).*\\bstock > .*"), sql);
            assertTrue(sql.contains("real_stock >"), sql);
            assertTrue(sql.contains("user_id ="), sql);
            return List.of();
        });
        var req = new AppCollectionListReqVO();
        req.setUserId(305L);
        req.setStatus(1);
        assertTrue(service.getRealCollectionList(req).isEmpty());
        verify(mapper).selectList(any(Wrapper.class));
    }

    @Test void storageCapacityStillUsesPhysicalInventory() {
        var mapper = mock(CollectionMapper.class);
        var service = new CollectionServiceImpl();
        ReflectionTestUtils.setField(service, "collectionMapper", mapper);
        when(mapper.selectRealAmountByUserId(305L)).thenReturn(10);
        assertEquals(10, service.getRealAmountByUserId(305L));
    }
}
