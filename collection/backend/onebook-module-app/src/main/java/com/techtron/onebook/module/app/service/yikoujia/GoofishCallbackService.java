package com.techtron.onebook.module.app.service.yikoujia;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoofishCallbackService {

    @Resource
    private YikoujiaService yikoujiaService;

    /**
     * 回调必须在三秒内响应，真实状态查询和本地同步放到异步线程执行。
     * 即使本次异步处理失败，GoldfishJob 仍会在下一轮补偿同步。
     */
    @Async
    public void processOrder(String orderNo, String productId) {
        try {
            yikoujiaService.syncGoofishOrder(orderNo, productId);
        } catch (Exception ex) {
            log.error("[processOrder][闲管家订单回调同步失败，orderNo={}, productId={}]",
                    orderNo, productId, ex);
        }
    }

    @Async
    public void processProduct(String productId) {
        try {
            yikoujiaService.syncGoofishProduct(productId);
        } catch (Exception ex) {
            log.error("[processProduct][闲管家商品回调同步失败，productId={}]", productId, ex);
        }
    }
}
