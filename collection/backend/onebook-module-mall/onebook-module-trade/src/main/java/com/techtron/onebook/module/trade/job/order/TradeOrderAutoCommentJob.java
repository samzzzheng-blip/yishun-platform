package com.techtron.onebook.module.trade.job.order;

import com.techtron.onebook.framework.quartz.core.handler.JobHandler;
import com.techtron.onebook.module.trade.service.order.TradeOrderUpdateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 交易订单的自动评论 Job
 *
 * @author 芋道源码
 */
@Component
public class TradeOrderAutoCommentJob implements JobHandler {

    @Resource
    private TradeOrderUpdateService tradeOrderUpdateService;

    @Override
    public String execute(String param) {
        int count = tradeOrderUpdateService.createOrderItemCommentBySystem();
        return String.format("评论订单 %s 个", count);
    }

}
