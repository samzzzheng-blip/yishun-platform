package com.techtron.onebook.module.app.service.fasttrade;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradePageReqVO;
import com.techtron.onebook.module.app.dal.mysql.fasttrade.FastTradeMapper;
import com.techtron.onebook.module.app.dal.mysql.fasttrade.FastTradeItemMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class FastTradeEmptyPageTest {
 @Test void emptyPageDoesNotQueryItemsWithEmptyInClause() {
  var service=new FastTradeServiceImpl();
  var mapper=mock(FastTradeMapper.class);var items=mock(FastTradeItemMapper.class);
  ReflectionTestUtils.setField(service,"fastTradeMapper",mapper);
  ReflectionTestUtils.setField(service,"fastTradeItemMapper",items);
  var req=new FastTradePageReqVO();
  when(mapper.selectPage(req)).thenReturn(new PageResult<>(Collections.emptyList(),0L));
  var result=service.getFastTradePage(req);
  assertEquals(0L,result.getTotal());assertTrue(result.getList().isEmpty());verifyNoInteractions(items);
 }
}
