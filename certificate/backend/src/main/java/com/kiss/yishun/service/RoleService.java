package com.kiss.yishun.service;

import com.kiss.yishun.entity.Role;

import java.util.List;

public interface RoleService {
    Role findRoleByCode(String code);
    void updateRole(Role role);
    Role findRoleById(long id);
    void deleteRole(long id);
    void addRole(Role role);
    List<Role> findRoleList(int level);

}
