package com.kiss.yishun.service;

import com.kiss.yishun.entity.SmartConfig;

public interface SmartConfigService {
    String findValueByKey(String key);

    void updateConfig(SmartConfig config);
}
