package com.techtron.onebook.module.app.service.yikoujia;

import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.framework.common.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YikoujiaImageOrderTest {
    @Test void preservesDuplicateCountsAndRejectsAdditions() {
        assertTrue(YikoujiaServiceImpl.isImagePermutation(List.of("a","a","b"), List.of("b","a","a")));
        assertFalse(YikoujiaServiceImpl.isImagePermutation(List.of("a","a","b"), List.of("b","b","a")));
        assertFalse(YikoujiaServiceImpl.isImagePermutation(List.of("a"), List.of("other")));
        assertFalse(YikoujiaServiceImpl.isImagePermutation(List.of("a","b"), List.of("a")));
    }
    @Test void reordersWithoutUpdatingStatusOrPrice() {
        var mapper = mock(YikoujiaMapper.class);
        var service = new YikoujiaServiceImpl();
        ReflectionTestUtils.setField(service, "yikoujiaMapper", mapper);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(new YikoujiaDO().setId(957L).setStatus(3).setPicUrl(List.of("a","b")));
        service.updateImageOrder(957L, List.of("a","b"), List.of("b","a"));
        var update = ArgumentCaptor.forClass(YikoujiaDO.class);
        verify(mapper).updateById(update.capture());
        assertEquals(List.of("b","a"), update.getValue().getPicUrl());
        assertNull(update.getValue().getStatus());
        assertNull(update.getValue().getPrice());
        assertNull(update.getValue().getProductId());
    }
    @Test void rejectsStaleOrderWithoutWriting() {
        var mapper = mock(YikoujiaMapper.class);
        var service = new YikoujiaServiceImpl();
        ReflectionTestUtils.setField(service, "yikoujiaMapper", mapper);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(new YikoujiaDO().setId(957L).setPicUrl(List.of("b","a")));
        assertThrows(ServiceException.class, () -> service.updateImageOrder(957L,List.of("a","b"),List.of("b","a")));
        verify(mapper, never()).updateById(any(YikoujiaDO.class));
    }
}
