package com.kiss.yishun.service;

import com.kiss.yishun.entity.Menu;

import java.util.List;

public interface MenuService {
    Menu findMenuByName(String name);
    void updateMenu(Menu role);
    Menu findMenuById(long id);
    void deleteMenu(long id);
    void addMenu(Menu menu);
    List<Menu> findMenuList(String cid);
    boolean existsMenus(Long[] menus);
    List<Menu> findRoleMenuList(Long roleId, int operationId);
    List<Menu> findMenuChildList(long cid);
    Menu findMenuByParentId(long pid);
}
