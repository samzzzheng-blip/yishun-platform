package com.kiss.yishun.dao;

import com.kiss.yishun.entity.SmartUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmartUserDao extends JpaRepository<SmartUser, Long> {
    SmartUser findByPhone(String phone);
    Page<SmartUser> findAllByPhoneLikeOrderByUpdatedateDesc(String phone, Pageable page);
}
