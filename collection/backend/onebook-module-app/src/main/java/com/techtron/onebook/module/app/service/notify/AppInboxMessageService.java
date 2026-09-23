package com.techtron.onebook.module.app.service.notify;

import com.techtron.onebook.module.system.api.notify.NotifyMessageSendApi;
import com.techtron.onebook.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.techtron.onebook.module.app.service.notify.AppSubscribeMessageService.*;

/** 业务提交后的站内反馈，不依赖微信订阅授权。 */
@Service
public class AppInboxMessageService {
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(Long userId, String title, Map<String, String> fields) {
        // 这两个场景已经由原有站内信生产者发送，避免重复。
        if (PRODUCT_SOLD.equals(title)
                || (ORDER_STATUS.equals(title) && "已发货".equals(fields.get("phrase2")))) {
            return;
        }
        String content = switch (title) {
            case PAYMENT_SUCCESS -> String.format("%s，数量 %s，支付金额 %s，支付时间 %s。可在订单中查看。",
                    fields.get("thing3"), fields.get("number5"), fields.get("amount1"), fields.get("time2"));
            case PRODUCT_LISTED -> String.format("%s 已上架，金额 %s，上架时间 %s。",
                    fields.get("thing11"), fields.get("amount3"), fields.get("time2"));
            case AUCTION_SUCCESS -> String.format("%s，成交金额 %s，%s。",
                    fields.get("thing1"), fields.get("amount2"), fields.get("thing4"));
            case REVIEW_RESULT -> String.format("%s：%s；%s。%s（%s）",
                    fields.get("thing2"), fields.get("phrase1"), fields.get("thing5"), fields.get("thing3"), fields.get("time4"));
            case ORDER_STATUS -> String.format("状态：%s；运单号：%s。%s",
                    fields.get("phrase2"), fields.get("character_string1"), fields.get("thing3"));
            case WITHDRAW_STATUS -> String.format("提现金额 %s，状态：%s。%s（申请时间 %s）",
                    fields.get("amount2"), fields.get("phrase3"), fields.get("thing4"), fields.get("time1"));
            default -> null;
        };
        if (content == null) {
            return;
        }
        boolean system = REVIEW_RESULT.equals(title) && !"藏品取回".equals(fields.get("thing2"));
        notifyMessageSendApi.sendSingleMessageToMember(new NotifySendSingleToUserReqDTO()
                .setUserId(userId)
                .setTemplateCode(system ? "app_business_system" : "app_business_trade")
                .setTemplateParams(Map.of("title", title, "content", content)));
    }
}
