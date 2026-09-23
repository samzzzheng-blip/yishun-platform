package com.kiss.yishun.auth;

import com.kiss.yishun.cache.JedisClient;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.User;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** One Redis entry per issued device token. Password changes invalidate all devices. */
@Service
public class AdminSessions {
    @Autowired private JedisClient redis;
    private static String digest(String value) {
        try {
            byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out=new StringBuilder(); for(byte b:bytes) out.append(String.format("%02x",b & 255));
            return out.toString();
        } catch(java.security.NoSuchAlgorithmException e) {throw new IllegalStateException(e);}
    }
    private String key(String token) {return "admin:device:"+digest(token);}
    private String credential(User user) {return digest(user.getUsername()+":"+user.getPassword());}
    public void issue(String token,User user) {
        redis.setex(key(token),credential(user),SecurityConstant.TOKEN_EXPIRE_TIME/1000,0);
    }
    public void validate(String token,User user) {
        if(user==null || user.getDisabled()!=0 || token==null) throw new AuthenticationException("账号已停用或不存在");
        String entry=redis.get(key(token),0);
        if(entry!=null && entry.equals(credential(user))) {
            Long ttl=redis.ttl(key(token),0);
            if(ttl==null || ttl<=0) throw new AuthenticationException("登录已过期");
            if(ttl<=SecurityConstant.REDIS_EXPIRE_REFRESH_TIME/1000)
                redis.expire(key(token),SecurityConstant.TOKEN_EXPIRE_TIME/1000,0);
            return;
        }
        // Previously issued sessions remain valid only if the exact stored token matches.
        String legacy=redis.get(user.getUsername(),0);
        String deadline=redis.get(user.getUsername()+SecurityConstant.REDIS_USR_EXPIRE,0);
        if(token.equals(legacy) && deadline!=null) {
            try {if(Long.parseLong(deadline)>System.currentTimeMillis()) return;} catch(NumberFormatException ignored) {}
        }
        throw new AuthenticationException("登录已过期，请重新登录");
    }
    public void revoke(String token,String username) {
        redis.del(key(token));
        if(token.equals(redis.get(username,0))) {
            redis.del(username,username+SecurityConstant.REDIS_USR_EXPIRE,username+SecurityConstant.REDIS_LOGIN_UUID);
        }
    }
}
