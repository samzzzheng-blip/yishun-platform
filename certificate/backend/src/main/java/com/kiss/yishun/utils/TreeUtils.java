package com.kiss.yishun.utils;

import com.kiss.yishun.entity.Menu;

import java.util.ArrayList;
import java.util.List;

public class TreeUtils {

    public static List<Menu> getMenuChild(long pid,List<Menu> rootMenu){
        List<Menu> childList = new ArrayList<>();
        for (Menu menu: rootMenu) {
            if (menu.getParentId() == pid) {
                childList.add(menu);
            }
        }
        for (Menu menu: childList) {
            menu.setChildren(getMenuChild(menu.getId(),rootMenu));
        }

        if (childList.size() == 0) {
            return null;
        }

        return childList;
    }

}
