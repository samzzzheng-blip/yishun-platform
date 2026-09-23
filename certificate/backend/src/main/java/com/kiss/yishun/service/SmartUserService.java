package com.kiss.yishun.service;

import com.kiss.yishun.entity.SmartUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface SmartUserService {
    SmartUser findByPhone(String phone);
    SmartUser findById(Long userId);
    void updateUser(SmartUser user);
    SmartUser findUserById(Long id);
    void deleteUser(long id);
    Long addUser(SmartUser user);
    Page<SmartUser> findAll(String phone, PageRequest page);
}
