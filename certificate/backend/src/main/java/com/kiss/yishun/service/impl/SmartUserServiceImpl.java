package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.SmartUserDao;
import com.kiss.yishun.entity.SmartUser;
import com.kiss.yishun.service.SmartUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SmartUserServiceImpl implements SmartUserService {

    @Autowired
    private SmartUserDao userDao;


    @Override
    public SmartUser findByPhone(String phone) {
        return userDao.findByPhone(phone);
    }

    @Override
    public SmartUser findById(Long userId) {
        return userDao.findById(userId).get();
    }

    @Override
    public void updateUser(SmartUser user) {
        userDao.saveAndFlush(user);
    }


    @Override
    public SmartUser findUserById(Long id) {
        return userDao.findById(id).get();
    }

    @Override
    public void deleteUser(long id) {
        userDao.deleteById(id);
    }


    @Override
    public Long addUser(SmartUser user) {
        SmartUser sUser = userDao.save(user);
        return sUser.getId();
    }

    @Override
    public Page<SmartUser> findAll(String phone, PageRequest page) {
        return userDao.findAllByPhoneLikeOrderByUpdatedateDesc('%'+phone+'%', page);
    }
}
