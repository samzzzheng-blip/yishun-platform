package com.kiss.yishun.utils;


import java.util.ArrayList;
import java.util.List;

public class StrUtils {

    public static Boolean isEmpty(String str) {
        return str==null|| str.trim().isEmpty();
    }

    public static Long str2Long(String str) {
        return isEmpty(str)? null: Long.parseLong(str);
    }

    public static String obj2String(long l) {
        return String.valueOf(l);
    }

    public static Long[] tranStringArray2LongArray(String[] stringArray)
    {
        List<Long> list=new ArrayList<>();
        for (String str: stringArray) {
            try {
                list.add(Long.parseLong(str));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        Long[] longArray=list.toArray(new Long[list.size()]);
        return longArray;
    }
}
