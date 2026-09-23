package com.kiss.yishun.utils;

import javax.servlet.http.HttpServletRequest;

public class IpUtils {
    public static String getIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0];
            }
        }
        String ip = request.getRemoteAddr();
        // ✅ 处理 IPv6 本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}