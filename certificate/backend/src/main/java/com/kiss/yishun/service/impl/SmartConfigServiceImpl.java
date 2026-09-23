package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.SmartConfigDao;
import com.kiss.yishun.entity.SmartConfig;
import com.kiss.yishun.service.SmartConfigService;
import com.kiss.yishun.utils.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmartConfigServiceImpl implements SmartConfigService {

    @Autowired
    private SmartConfigDao configDao;

    @Override
    public String findValueByKey(String key) {
        if (StrUtils.isEmpty(key)) {
            return null;
        }
        SmartConfig config = configDao.findValueByNameEquals(key);
        if (config == null) {
            return null;
        }
        return config.getValue();
    }

    @Override
    public void updateConfig(SmartConfig config) {
        configDao.saveAndFlush(config);
    }
}
