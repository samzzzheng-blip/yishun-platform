package com.techtron.onebook.module.infra.service.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 启动时将数据库中的定时任务配置同步到 Quartz。
 *
 * <p>默认关闭，部署环境可通过 {@code onebook.job.auto-sync-on-startup=true} 开启，
 * 用于保证数据库迁移新增或更新的任务能够立即生效。</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "onebook.job", name = "auto-sync-on-startup", havingValue = "true")
public class JobAutoSyncRunner implements ApplicationRunner {

    private final JobService jobService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        jobService.syncJob();
        log.info("[run][数据库定时任务已同步到 Quartz]");
    }
}
