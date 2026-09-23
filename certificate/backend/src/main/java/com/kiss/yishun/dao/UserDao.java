package com.kiss.yishun.dao;

import com.kiss.yishun.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UserDao extends JpaRepository<User, Long> {

    User findByUsername(String username);

    Page<User> findAllByUsernameLikeAndRole_LevelGreaterThanOrderByDisabledAscIdAsc(String keywords, int level, Pageable pageable);

    User findById(long id);

    @Transactional
    void deleteUserById(long id);
}
