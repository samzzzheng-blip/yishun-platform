package com.techtron.onebook.module.app.mq.producer;

import com.techtron.onebook.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Mail 邮件相关消息的 Producer
 *
 * @author wangjingyi
 * @since 2021/4/19 13:33
 */
@Slf4j
@Component
public class GetbackNotifyProducer {

    @Resource
    private ApplicationContext applicationContext;


    public void sendNotifySendMessage(String templateCode, Long userId, String name, String deliverCode) {
        // 1、构造消息
        Map<String, Object> msgMap = new HashMap<>(2);
        msgMap.put("collectionName", name);
        msgMap.put("deliverCode", deliverCode);
        // 2、发送站内信
        NotifySendSingleToUserReqDTO message = new NotifySendSingleToUserReqDTO()
                .setUserId(userId)
                .setTemplateCode(templateCode)
                .setTemplateParams(msgMap);
        applicationContext.publishEvent(message);
    }

}
