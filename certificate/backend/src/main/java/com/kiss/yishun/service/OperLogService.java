package com.kiss.yishun.service;

import com.kiss.yishun.entity.OperLog;

public interface OperLogService {

    void addOperLog(String message, String ip);
}
