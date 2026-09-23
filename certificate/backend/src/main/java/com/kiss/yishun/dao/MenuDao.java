package com.kiss.yishun.dao;

import com.kiss.yishun.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface MenuDao extends JpaRepository<Menu, Long> {

    Menu findMenuByName(String name);

    Menu findMenuById(long id);

    List<Menu> findAllByIdIsNotAndParentIdIsNotOrderByIdAsc(long cid,long pid);

    @Transactional
    void deleteMenuById(long id);

    List<Menu> findAllByParentIdIs(long pid);

    boolean existsByIdIn(Long[] ids);

    @Query(value = "select menu.id,menu.name,menu.parent_id,menu.path from menu where id in (select menu_id from permission where operation_id=?2 and id in (select permission_id from role_permission where role_id = ?1))",nativeQuery = true)
    List<Menu> findRoleMenuList(long roleId, int operationId);

}
