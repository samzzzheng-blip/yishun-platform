package com.kiss.yishun.service.impl;

import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.dao.OperLogDao;
import com.kiss.yishun.dao.UserDao;
import com.kiss.yishun.entity.OperLog;
import com.kiss.yishun.entity.User;
import com.kiss.yishun.service.OperLogService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperLogServiceImpl implements OperLogService {
    @Autowired
    private OperLogDao operLogDao;
    @Autowired
    private UserDao userDao;

    @Override
    public void addOperLog(String message, String ip) {
        String token = (String) SecurityUtils.getSubject().getPrincipal();
        String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
        User user = userDao.findByUsername(userName);
        OperLog operLog = new OperLog();
        if (user.getRemark() != null && !user.getRemark().isEmpty()) {
            operLog.setOperator(userName + "-" + user.getRemark());
        } else {
            operLog.setOperator(userName);
        }
        operLog.setMessage(message);
        operLog.setIp(ip);
        operLogDao.saveAndFlush(operLog);
    }
}
