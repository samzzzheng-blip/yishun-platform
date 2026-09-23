package com.kiss.yishun.dao;

import com.kiss.yishun.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface RoleDao extends JpaRepository<Role, Long> {

    Role findRoleByCode(String code);

    Page<Role> findAllByCode(String keywords, Pageable pageable);

    Role findRoleById(long id);

    @Transactional
    void deleteRoleById(long id);

    List<Role> findByCodeIsNot(String code);

    List<Role> findAllByLevelIsGreaterThanEqualOrderByLevelAsc(int level);
}
