package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.RoleDao;
import com.kiss.yishun.entity.Role;
import com.kiss.yishun.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleDao roleDao;

    @Override
    public Role findRoleByCode(String code) {
        return roleDao.findRoleByCode(code);
    }

    @Override
    public void updateRole(Role role) {
        roleDao.saveAndFlush(role);
    }

    @Override
    public List<Role> findRoleList(int level) {
        return roleDao.findAllByLevelIsGreaterThanEqualOrderByLevelAsc(level);
    }

    @Override
    public Role findRoleById(long id) {
        return roleDao.findRoleById(id);
    }

    @Override
    public void deleteRole(long id) {
        roleDao.deleteRoleById(id);
    }


    @Override
    public void addRole(Role role) {
        roleDao.save(role);
    }
}
