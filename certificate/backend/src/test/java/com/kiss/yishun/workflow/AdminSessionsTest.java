package com.kiss.yishun.workflow;

import com.kiss.yishun.auth.*;
import com.kiss.yishun.cache.JedisClient;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.User;
import org.apache.shiro.authc.AuthenticationException;
import org.junit.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AdminSessionsTest {
    private AdminSessions sessions;
    private JedisClient redis;
    private User user;
    private Map<String,String> values;
    private Map<String,Long> ttl;
    @Before public void setup() {
        sessions=new AdminSessions();redis=mock(JedisClient.class);values=new HashMap<>();ttl=new HashMap<>();
        ReflectionTestUtils.setField(sessions,"redis",redis);
        when(redis.get(anyString(),eq(0))).thenAnswer(a->values.get(a.getArgument(0)));
        when(redis.ttl(anyString(),eq(0))).thenAnswer(a->ttl.getOrDefault(a.getArgument(0),-2L));
        when(redis.setex(anyString(),anyString(),anyInt(),eq(0))).thenAnswer(a->{values.put(a.getArgument(0),a.getArgument(1));ttl.put(a.getArgument(0),((Integer)a.getArgument(2)).longValue());return "OK";});
        when(redis.expire(anyString(),anyInt(),eq(0))).thenAnswer(a->{ttl.put(a.getArgument(0),((Integer)a.getArgument(1)).longValue());return 1L;});
        when(redis.del(anyString())).thenAnswer(a->{values.remove(a.getArgument(0));ttl.remove(a.getArgument(0));return 1L;});
        user=new User();user.setUsername("staff_one");user.setPassword("password-fingerprint");user.setDisabled(0);
    }
    private String token() {return JwtUtil.sign(user.getUsername(),System.currentTimeMillis(),UUID.randomUUID().toString());}
    private void rejected(Runnable operation) {try {operation.run();fail("Expected rejected session");}catch(AuthenticationException expected){}}
    @Test public void twoDevicesStayOnlineAndLogoutRevokesOnlyOne() {
        String phone=token(),desktop=token();sessions.issue(phone,user);sessions.issue(desktop,user);
        sessions.validate(phone,user);sessions.validate(desktop,user);
        sessions.revoke(phone,user.getUsername());rejected(()->sessions.validate(phone,user));sessions.validate(desktop,user);
    }
    @Test public void passwordChangeInvalidatesEveryDevice() {
        String a=token(),b=token();sessions.issue(a,user);sessions.issue(b,user);user.setPassword("changed");
        rejected(()->sessions.validate(a,user));rejected(()->sessions.validate(b,user));
    }
    @Test public void disabledOrDeletedUserCannotUseIssuedToken() {
        String a=token();sessions.issue(a,user);user.setDisabled(1);rejected(()->sessions.validate(a,user));rejected(()->sessions.validate(a,null));
    }
    @Test public void neverIssuedAndExpiredTokensAreRejected() {
        rejected(()->sessions.validate(token(),user));String a=token();sessions.issue(a,user);ttl.replaceAll((k,v)->0L);rejected(()->sessions.validate(a,user));
    }
    @Test public void renewalDoesNotExtendAnotherDevice() {
        String a=token(),b=token();sessions.issue(a,user);sessions.issue(b,user);ttl.replaceAll((k,v)->60L);
        sessions.validate(a,user);assertEquals(1,ttl.values().stream().filter(v->v==86400L).count());assertEquals(1,ttl.values().stream().filter(v->v==60L).count());
    }
    @Test public void tokenAccountParsingPreservesUnderscoresAndRejectsMalformedClaim() {
        assertEquals("staff_one",JwtUtil.getAccount(token(),SecurityConstant.ACCOUNT));
        assertNull(JwtUtil.getAccount("not-a-token",SecurityConstant.ACCOUNT));
    }
}
