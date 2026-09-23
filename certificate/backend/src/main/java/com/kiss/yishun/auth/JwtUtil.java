package com.kiss.yishun.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.kiss.yishun.config.RedisConfig;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.constant.SmartConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.Date;

public class JwtUtil {

    @Autowired
    RedisConfig redisConfig;

    public static String SECRET = System.getenv("YISHUN_JWT_SECRET");

    public static String ACCOUNT_SPEROTOR = "_";

    /**
     * 校验token是否正确
     *
     * @param token
     *
     * @return 是否正确
     */
    // public static boolean verify(String token, String username) {
    //     //根据密码生成JWT效验器
    //     Algorithm algorithm = Algorithm.HMAC256(SECRET);
    //     JWTVerifier verifier = JWT.require(algorithm)
    //             .withClaim(SecurityConstant.ACCOUNT, username)
    //             .build();
    //     //效验TOKEN
    //     verifier.verify(token);
    //     return true;
    // }

    /**
     * 获取claim ACCOUNT
     *
     * @return
     */
    public static String getClaimAsString(String token, String claim) {
        if (!StringUtils.isEmpty(token)) {
            try {
                DecodedJWT jwt = JWT.decode(token);
                return jwt.getClaim(claim).asString();
            } catch (JWTDecodeException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取claim SIGN_TIME
     * @param token
     * @param claim
     * @return
     */
    public static Long getClaimAsLong(String token, String claim) {
        if (!StringUtils.isEmpty(token)) {
            try {
                DecodedJWT jwt = JWT.decode(token);
                return jwt.getClaim(claim).asLong();
            } catch (JWTDecodeException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 生成签名
     *
     * @param username 用户名
     *
     * @return 加密的token
     */
    public static String sign(String username, Long now, String ip) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        Date date = new Date(now + SecurityConstant.TOKEN_EXPIRE_TIME);
        // 附带username信息
        return JWT.create()
                .withClaim(SecurityConstant.ACCOUNT, ip + ACCOUNT_SPEROTOR + username)
                .withClaim(SecurityConstant.SIGN_TIME, now)
                .withExpiresAt(date)
                .sign(algorithm);
    }

    public static String getAccount(String token, String claim) {
        String account = getClaimAsString(token, claim);
        if (account != null && account.indexOf(ACCOUNT_SPEROTOR)>0) {
            return account.substring(account.indexOf(ACCOUNT_SPEROTOR)+1);
        }
        return null;
    }

    public static String getUUID(String token, String claim) {
        String account = getClaimAsString(token, claim);
        if (account != null && account.indexOf(ACCOUNT_SPEROTOR)>0) {
            return account.substring(0,account.indexOf(ACCOUNT_SPEROTOR));
        }
        return null;
    }

    public static Long getSmartUserId(String token, String claim) {
        String account = getClaimAsString(token, claim);
        if (account != null) {
            String accountName = account.split(ACCOUNT_SPEROTOR)[1];
            String userId = accountName.split(SmartConstant.SMART_USER)[1];
            return Long.parseLong(userId);
        }
        return null;
    }

}
