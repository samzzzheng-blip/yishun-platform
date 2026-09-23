package com.kiss.yishun.dao;

import com.kiss.yishun.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface PermissionDao extends JpaRepository<Permission, Long> {

    Permission findPermissionByCode(String code);

    Page<Permission> findAllByCode(String keywords, Pageable pageable);

    Permission findPermissionById(long id);

    @Transactional
    void deletePermissionById(long id);

}
