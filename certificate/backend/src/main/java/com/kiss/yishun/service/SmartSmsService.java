package com.kiss.yishun.service;

import com.kiss.yishun.entity.SmartSms;

public interface SmartSmsService {
    long addRecord(SmartSms record);
    long updateRecord(SmartSms record);
}
