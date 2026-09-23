package com.kiss.yishun.service.impl;

import com.kiss.yishun.service.UserService;
import com.kiss.yishun.dao.UserDao;
import com.kiss.yishun.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public void updateUser(User user) {
        userDao.saveAndFlush(user);
    }

    @Override
    public Page<User> findUserPageByKeywords(PageRequest pageRequest, String keywords, int level) {
        return userDao.findAllByUsernameLikeAndRole_LevelGreaterThanOrderByDisabledAscIdAsc('%'+keywords+'%', level, pageRequest);
    }

    @Override
    public User findUserById(long id) {
        return userDao.findById(id);
    }

    @Override
    public void deleteUser(long id) {
        userDao.deleteUserById(id);
    }


    @Override
    public void addUser(User user) {
        userDao.save(user);
    }
}
