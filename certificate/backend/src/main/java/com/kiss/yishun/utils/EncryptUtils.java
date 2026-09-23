package com.kiss.yishun.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class EncryptUtils {

    /**
     * 校验输入密码是否正确
     * @param encryptPassword
     * @param password
     * @return
     */
    public static Boolean verfifyPassword(String encryptPassword, String password) {
        String encryptStr = encryptPassword(password);
        return encryptPassword.equals(encryptStr);
    }

    /**
     * 获得加密后的密码
     * @param password
     * @return
     */
    public static String encryptPassword(String password) {
        return DigestUtils.md5Hex(password);
    }





}
