package com.kiss.yishun.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUtils {

    public static boolean isPhone(String phone) {
        String reg = "^1[3-9]\\d{9}$";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(phone);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }
}
