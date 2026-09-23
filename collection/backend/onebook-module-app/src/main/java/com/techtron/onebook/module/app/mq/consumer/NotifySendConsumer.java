package com.techtron.onebook.module.app.mq.consumer;

import com.techtron.onebook.module.system.api.notify.NotifyMessageSendApi;
import com.techtron.onebook.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import com.techtron.onebook.module.system.mq.message.mail.MailSendMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 针对 {@link MailSendMessage} 的消费者
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class NotifySendConsumer {

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @EventListener
    @Async // Spring Event 默认在 Producer 发送的线程，通过 @Async 实现异步
    public void onMessage(NotifySendSingleToUserReqDTO message) {
        log.info("[onMessage][消息内容({})]", message);
        // 2、发送站内信
        notifyMessageSendApi.sendSingleMessageToMember(message);
    }

}
