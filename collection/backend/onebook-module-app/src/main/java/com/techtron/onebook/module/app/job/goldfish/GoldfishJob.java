package com.techtron.onebook.module.app.job.goldfish;

import com.techtron.onebook.framework.quartz.core.handler.JobHandler;
import com.techtron.onebook.module.app.service.yikoujia.YikoujiaService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GoldfishJob implements JobHandler {

    @Resource
    private YikoujiaService yikoujiaService;

    @Override
    public String execute(String param) throws Exception {
        int updateCount = yikoujiaService.executeUpdate();
        return String.format("执行商品状态更新 %s 个", updateCount);
    }
}
