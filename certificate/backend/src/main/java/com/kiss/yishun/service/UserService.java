package com.kiss.yishun.service;

import com.kiss.yishun.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface UserService {
    User findByUsername(String username);
    void updateUser(User user);
    Page<User> findUserPageByKeywords(PageRequest pageRequest, String keywords, int level);
    User findUserById(long id);
    void deleteUser(long id);
    void addUser(User user);

}
