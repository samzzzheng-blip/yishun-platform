package com.kiss.yishun.service;

import com.kiss.yishun.entity.Permission;

import java.util.List;

public interface PermissionService {

    Permission findPermissionByCode(String code);
    void updatePermission(Permission permission);
    Permission findPermissionById(long id);
    void deletePermission(long id);
    void addPermission(Permission role);
    List<Permission> findPermissionList();

}
