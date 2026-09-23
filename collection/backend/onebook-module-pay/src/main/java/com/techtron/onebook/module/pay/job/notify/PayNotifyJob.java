package com.techtron.onebook.module.pay.job.notify;

import com.techtron.onebook.framework.quartz.core.handler.JobHandler;
import com.techtron.onebook.module.pay.service.notify.PayNotifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 支付通知 Job
 * 通过不断扫描待通知的 PayNotifyTaskDO 记录，回调业务线的回调接口
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class PayNotifyJob implements JobHandler {

    @Resource
    private PayNotifyService payNotifyService;

    @Override
    public String execute(String param) throws Exception {
        int notifyCount = payNotifyService.executeNotify();
        return String.format("执行支付通知 %s 个", notifyCount);
    }

}
