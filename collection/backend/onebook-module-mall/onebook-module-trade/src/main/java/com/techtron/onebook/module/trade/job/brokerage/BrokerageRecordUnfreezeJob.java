package com.techtron.onebook.module.trade.job.brokerage;

import cn.hutool.core.util.StrUtil;
import com.techtron.onebook.framework.quartz.core.handler.JobHandler;
import com.techtron.onebook.module.trade.service.brokerage.BrokerageRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 佣金解冻 Job
 *
 * @author owen
 */
@Component
public class BrokerageRecordUnfreezeJob implements JobHandler {

    @Resource
    private BrokerageRecordService brokerageRecordService;

    @Override
    public String execute(String param) {
        int count = brokerageRecordService.unfreezeRecord();
        return StrUtil.format("解冻佣金 {} 个", count);
    }

}
