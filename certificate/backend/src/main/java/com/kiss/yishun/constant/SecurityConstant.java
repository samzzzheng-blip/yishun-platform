package com.kiss.yishun.constant;

public class SecurityConstant {

    public static String ACCOUNT = "username";

    public static String SIGN_TIME = "current";

    public static String REDIS_USR_EXPIRE = ":expireTime";

    public static String REDIS_LOGIN_UUID = ":uuid";

    // token过期时间,1天
    public static int TOKEN_EXPIRE_TIME = 24 * 60 * 60 * 1000;

    // redis距离过期还有多少时间续期，1小时
    public static int REDIS_EXPIRE_REFRESH_TIME = 1 * 60 * 60 * 1000;

}
