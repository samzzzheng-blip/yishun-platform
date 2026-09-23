package com.techtron.onebook.module.app.job;

import com.techtron.onebook.framework.quartz.core.handler.JobHandler;
import com.techtron.onebook.module.app.service.auction.AuctionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuctionCloseJob implements JobHandler {
    @Resource private AuctionService auctionService;

    @Override
    public String execute(String param) {
        int refreshed = auctionService.syncManagedAuctions(50);
        int expired = auctionService.closeExpiredAuctions(100);
        if (refreshed + expired > 0) log.info("[execute][闲管家详情检查 {} 条，到期检查 {} 条]", refreshed, expired);
        return "闲管家详情检查 " + refreshed + " 条，到期检查 " + expired + " 条";
    }
}
