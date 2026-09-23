package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.MenuDao;
import com.kiss.yishun.entity.Menu;
import com.kiss.yishun.service.MenuService;
import com.kiss.yishun.utils.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuDao menuDao;

    @Override
    public Menu findMenuByName(String name) {
        return menuDao.findMenuByName(name);
    }

    @Override
    public void updateMenu(Menu menu) {
        menuDao.saveAndFlush(menu);
    }

    @Override
    public List<Menu> findMenuList(String cid) {
        if (StrUtils.isEmpty(cid)) {
            return menuDao.findAll(Sort.by(Sort.Direction.ASC, "id"));
        } else {
            return menuDao.findAllByIdIsNotAndParentIdIsNotOrderByIdAsc(Long.parseLong(cid),Long.parseLong(cid));
        }
    }

    @Override
    public boolean existsMenus(Long[] menus) {
        return menuDao.existsByIdIn(menus);
    }

    @Override
    public List<Menu> findRoleMenuList(Long roleId, int operationId) {
        return menuDao.findRoleMenuList(roleId, operationId);
    }

    @Override
    public List<Menu> findMenuChildList(long cid) {
        return menuDao.findAllByParentIdIs(cid);
    }

    @Override
    public Menu findMenuByParentId(long pid) {
        return menuDao.findMenuById(pid);
    }

    @Override
    public Menu findMenuById(long id) {
        return menuDao.findMenuById(id);
    }

    @Override
    public void deleteMenu(long id) {
        List<Menu> childMenus = menuDao.findAllByParentIdIs(id);
        for (Menu menu: childMenus) {
            if (menu.getChildren() != null && menu.getChildren().size()>0) {
                deleteMenu(menu.getId());
            } else {
                menuDao.deleteMenuById(menu.getId());
            }
        }
        menuDao.deleteMenuById(id);

    }

    @Override
    public void addMenu(Menu menu) {
        menuDao.save(menu);
    }
}
