package com.techtron.onebook.module.app.job;

import com.techtron.onebook.framework.quartz.core.handler.JobHandler;
import com.techtron.onebook.module.app.service.storageplan.StoragePlanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StorageExpireJob implements JobHandler {

    @Resource
    private StoragePlanService storagePlanService;

    @Override
    public String execute(String param) throws Exception {
        int successCount = storagePlanService.autoRenewExpireStorage();
        log.info("[execute][定时执行存储套餐自动续费完成，成功处理 {} 个用户]", successCount);
        return String.format("定时执行存储套餐自动续费完成，成功处理 %s 个用户", successCount);
    }
}