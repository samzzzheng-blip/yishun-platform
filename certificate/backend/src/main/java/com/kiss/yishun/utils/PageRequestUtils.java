package com.kiss.yishun.utils;

import com.kiss.yishun.constant.PageConstant;
import org.springframework.data.domain.PageRequest;

public class PageRequestUtils {

    public static PageRequest getPageRequest(String page) {
        return getPageRequest(page,null);
    }

    public static PageRequest getPageRequest(String page, String size) {
        page = StrUtils.isEmpty(page)? "1":page;
        size = StrUtils.isEmpty(size)? String.valueOf(PageConstant.PAGESIZE) :size;
        return PageRequest.of(Integer.parseInt(page)-1, Integer.parseInt(size));
    }
}
