package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.PermissionDao;
import com.kiss.yishun.entity.Permission;
import com.kiss.yishun.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionDao permissionDao;

    @Override
    public Permission findPermissionByCode(String code) {
        return permissionDao.findPermissionByCode(code);
    }

    @Override
    public void updatePermission(Permission permission) {
        permissionDao.saveAndFlush(permission);
    }

    @Override
    public List<Permission> findPermissionList() {
        return permissionDao.findAll();
    }

    @Override
    public Permission findPermissionById(long id) {
        return permissionDao.findPermissionById(id);
    }

    @Override
    public void deletePermission(long id) {
        permissionDao.deletePermissionById(id);
    }


    @Override
    public void addPermission(Permission permission) {
        permissionDao.save(permission);
    }
}
