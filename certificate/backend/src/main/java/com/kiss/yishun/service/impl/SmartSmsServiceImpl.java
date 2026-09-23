package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.SmartSmsDao;
import com.kiss.yishun.entity.SmartSms;
import com.kiss.yishun.service.SmartSmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmartSmsServiceImpl implements SmartSmsService {

    @Autowired
    private SmartSmsDao smsDao;

    @Override
    public long addRecord(SmartSms record) {
        return smsDao.saveAndFlush(record).getId();
    }

    @Override
    public long updateRecord(SmartSms record) {
        return smsDao.saveAndFlush(record).getId();
    }
}
