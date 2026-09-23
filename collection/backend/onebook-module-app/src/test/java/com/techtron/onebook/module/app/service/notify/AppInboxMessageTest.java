package com.techtron.onebook.module.app.service.notify;

import com.techtron.onebook.module.system.api.notify.NotifyMessageSendApi;
import com.techtron.onebook.module.system.api.social.SocialClientApi;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;
import java.util.Map;
import static org.mockito.Mockito.*;
import static com.techtron.onebook.module.app.service.notify.AppSubscribeMessageService.*;

class AppInboxMessageTest {
    private final NotifyMessageSendApi api = mock(NotifyMessageSendApi.class);
    private final SocialClientApi social = mock(SocialClientApi.class);
    private final AppInboxMessageService inbox = new AppInboxMessageService();
    private final AppSubscribeMessageService service = new AppSubscribeMessageService();

    AppInboxMessageTest() {
        ReflectionTestUtils.setField(inbox, "notifyMessageSendApi", api);
        ReflectionTestUtils.setField(service, "appInboxMessageService", inbox);
        ReflectionTestUtils.setField(service, "socialClientApi", social);
    }

    @Test void paymentPersistsEvenWhenWechatFails() {
        doThrow(new RuntimeException("no subscription")).when(social).sendWxaSubscribeMessage(any());
        pay();
        verify(api).sendSingleMessageToMember(argThat(req -> req.getUserId().equals(8L)
                && req.getTemplateCode().equals("app_business_trade")
                && req.getTemplateParams().get("content").toString().contains("¥12.30")));
    }

    @Test void transactionWaitsForCommitAndRollbackSendsNothing() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            pay();
            verifyNoInteractions(api, social);
            // 回滚只调用 afterCompletion，不调用 afterCommit。
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(1));
            verifyNoInteractions(api, social);
        } finally {
            TransactionSynchronizationManager.clear();
        }
    }

    @Test void committedPaymentSendsBothChannels() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            pay();
            verifyNoInteractions(api, social);
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
            verify(api).sendSingleMessageToMember(any());
            verify(social).sendWxaSubscribeMessage(any());
        } finally {
            TransactionSynchronizationManager.clear();
        }
    }

    @Test void existingSoldAndShippedMessagesAreNotDuplicated() {
        inbox.send(8L, PRODUCT_SOLD, Map.of());
        inbox.send(8L, ORDER_STATUS, Map.of("phrase2", "已发货"));
        verifyNoInteractions(api);
    }

    @Test void reviewGoesToSystemCategory() {
        service.notifyReviewResult(8L, "已通过", "藏品审核", "已入库", LocalDateTime.now(), "测试藏品");
        verify(api).sendSingleMessageToMember(argThat(req -> req.getTemplateCode().equals("app_business_system")));
    }

    private void pay() {
        service.notifyPaymentSuccess(8L, 1230, LocalDateTime.now(), "测试藏品", 1);
    }
}
