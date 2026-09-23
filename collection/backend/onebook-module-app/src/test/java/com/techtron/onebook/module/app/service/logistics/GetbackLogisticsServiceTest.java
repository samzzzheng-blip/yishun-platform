package com.techtron.onebook.module.app.service.logistics;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.techtron.onebook.module.app.dal.mysql.getback.GetbackMapper;
import com.techtron.onebook.module.app.dal.dataobject.getback.GetbackDO;
import java.time.Duration;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GetbackLogisticsServiceTest {
    private final GetbackMapper mapper = mock(GetbackMapper.class);
    private final Kuaidi100Client client = mock(Kuaidi100Client.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> cache = mock(ValueOperations.class);
    private final GetbackLogisticsService service = new GetbackLogisticsService(mapper, client, redis);
    private GetbackDO order() {
        var order = new GetbackDO().setId(9L).setUserId(7L).setStatus(1).setDeliverCode("SF123").setReceiverMobile("13800138000");
        when(mapper.selectById(9L)).thenReturn(order);
        when(client.available()).thenReturn(true);
        when(redis.opsForValue()).thenReturn(cache);
        when(cache.setIfAbsent(anyString(), anyString(), eq(Duration.ofMinutes(30)))).thenReturn(true);
        return order;
    }
    @Test void deniesOtherUsersBeforeCacheAndExternalCalls() throws Exception {
        order();
        assertThrows(Exception.class, () -> service.get(9L, 8L));
        assertThrows(Exception.class, () -> service.get(9L, null));
        verify(client, never()).query(anyString(), anyString(), anyString());
        verifyNoInteractions(redis);
    }
    @Test void cachesQueriesAndHandlesHistoricalSf() throws Exception {
        order();
        when(client.query("shunfeng", "SF123", "13800138000")).thenReturn(LogisticsResult.unavailable("EMPTY", "暂无轨迹"));
        assertEquals("EMPTY", service.get(9L, 7L).getAvailability());
        verify(cache).set(anyString(), anyString(), eq(Duration.ofMinutes(30)));
        when(cache.get(anyString())).thenReturn("{\"availability\":\"OK\",\"status\":\"已签收\",\"nodes\":[{\"time\":\"2026-09-16 12:00:00\",\"description\":\"已签收\"}]}");
        assertEquals("已签收", service.get(9L, 7L).getStatus());
        verify(client, times(1)).query(anyString(), anyString(), anyString());
    }
    @Test void noQueryWhenUnshippedUnconfiguredOrAmbiguousCarrier() throws Exception {
        var order = order().setStatus(0);
        assertEquals("NOT_SHIPPED", service.get(9L, 7L).getAvailability());
        order.setStatus(1).setDeliverCode("1234567890");
        assertEquals("MISSING_CARRIER", service.get(9L, 7L).getAvailability());
        order.setExpressCompany("zhongtong"); when(client.available()).thenReturn(false);
        assertEquals("NOT_CONFIGURED", service.get(9L, 7L).getAvailability());
        verify(client, never()).query(anyString(), anyString(), anyString());
    }
    @Test void concurrentReservationAndRedisFailureDoNotBypassRateLimit() throws Exception {
        order(); when(cache.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);
        assertEquals("PENDING", service.get(9L, 7L).getAvailability());
        when(cache.get(anyString())).thenThrow(new RuntimeException());
        assertEquals("UNAVAILABLE", service.get(9L, 7L).getAvailability());
        verify(client, never()).query(anyString(), anyString(), anyString());
    }
    @Test void externalFailureIsCachedWithoutLeakingDetails() throws Exception {
        order(); when(client.query(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("secret"));
        var result = service.get(9L, 7L);
        assertEquals("UNAVAILABLE", result.getAvailability());
        assertFalse(result.getMessage().contains("secret"));
        verify(cache).set(anyString(), anyString(), eq(Duration.ofMinutes(30)));
    }
}
