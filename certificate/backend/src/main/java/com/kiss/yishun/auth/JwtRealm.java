package com.kiss.yishun.auth;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.constant.SmartConstant;
import com.kiss.yishun.entity.Permission;
import com.kiss.yishun.entity.SmartUser;
import com.kiss.yishun.entity.User;
import com.kiss.yishun.service.SmartUserService;
import com.kiss.yishun.service.UserService;
import com.kiss.yishun.utils.StrUtils;
import com.kiss.yishun.cache.JedisClient;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JwtRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    @Autowired
    private SmartUserService smartUserService;

    @Autowired
    JedisClient redisClient;

    @Autowired private AdminSessions sessions;

    /**
     * 必须重写此方法，不然Shiro会报错
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission之类的
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        String token = principals.toString();
        String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
        if (userName != null && userName.startsWith(SmartConstant.SMART_USER)) {
            String userId = userName.split(SmartConstant.SMART_USER)[1];
            SmartUser smartUser = smartUserService.findById(Long.parseLong(userId));
            if (smartUser != null) {
                simpleAuthorizationInfo.addRole("smartUser");
                List<String> permissions = new ArrayList<>();
                permissions.add("smart:view");
                simpleAuthorizationInfo.addStringPermissions(permissions);
            }
        } else {
            User user = userService.findByUsername(userName);
            if (null != user) {
                simpleAuthorizationInfo.addRole(user.getRole().getCode());
                List<String> permissions = new ArrayList<>();
                for (Permission p:user.getRole().getPermissionList()) {
                    permissions.add(p.getCode());
                }
                simpleAuthorizationInfo.addStringPermissions(permissions);
            }
        }
        return simpleAuthorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getCredentials();
        String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
        String uuid = JwtUtil.getUUID(token, SecurityConstant.ACCOUNT);
        if(!StrUtils.isEmpty(userName) && !userName.startsWith(SmartConstant.SMART_USER)) {
            sessions.validate(token,userService.findByUsername(userName));
            return new SimpleAuthenticationInfo(token,token,"my_realm");
        }
        // 不在redis中默认认证失败
        if (StrUtils.isEmpty(userName) || !redisClient.exists(userName, 0)) {
            throw new TokenExpiredException("token无效");
        }
        // 是否还有该用户或已注销
//        User userBean = userService.findByUsername(userName);
//        if (userBean == null || userBean.getDisabled()) {
//            redisClient.del(userName);
//            throw new AuthenticationException("用户不存在!");
//        }
        // 做token校验
    //    JwtUtil.verify(token, userName);
        // 校验其他设备登录
        String redisUUID = redisClient.get( userName + SecurityConstant.REDIS_LOGIN_UUID, 0);
        if (redisUUID != null && !redisUUID.equals(uuid)) {
            throw new AuthenticationException("已在其他设备登录!");
        }
        // 校验过期时间
        String redisExpireKey = userName+SecurityConstant.REDIS_USR_EXPIRE;
        String expiretime = redisClient.get(redisExpireKey,0);
        long now = System.currentTimeMillis();
        // 已过期redis没有及时清理
        if (Long.parseLong(expiretime) - now <= 0) {
            redisClient.del(userName);
            redisClient.del(redisExpireKey);
            throw new AuthenticationException("token已过期!");
        }
        // 即将过期，redis续期
        if (Long.parseLong(expiretime) - now <= SecurityConstant.REDIS_EXPIRE_REFRESH_TIME) {
            redisClient.expire(userName,(int)(Long.parseLong(expiretime) + SecurityConstant.TOKEN_EXPIRE_TIME - now)/1000 ,0);
            redisClient.set(redisExpireKey,String.valueOf(Long.parseLong(expiretime)+SecurityConstant.TOKEN_EXPIRE_TIME),0);
        }
        return new SimpleAuthenticationInfo(token, token, "my_realm");
    }
}
