package com.techtron.onebook.module.app.service.notify;

import com.techtron.onebook.module.system.api.social.SocialClientApi;
import com.techtron.onebook.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.techtron.onebook.framework.common.enums.UserTypeEnum.MEMBER;

/**
 * 小程序业务订阅消息统一出口。
 *
 * 消息只在业务事务提交后发送，并且发送失败绝不回滚订单、库存或资金数据。
 */
@Service
@Slf4j
public class AppSubscribeMessageService {

    public static final String AUCTION_SUCCESS = "商品竞价成功通知";
    public static final String WITHDRAW_STATUS = "申请提现状态通知";
    public static final String PRODUCT_LISTED = "商品上架通知";
    public static final String PRODUCT_SOLD = "商品售出提醒";
    public static final String REVIEW_RESULT = "审核结果通知";
    public static final String ORDER_STATUS = "订单状态通知";
    public static final String PAYMENT_SUCCESS = "支付成功通知";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Resource
    private SocialClientApi socialClientApi;

    @Resource
    private AppInboxMessageService appInboxMessageService;

    public void notifyAuctionPublished(Long userId, String productName, Integer price, LocalDateTime listedAt) {
        sendAfterCommit(userId, PRODUCT_LISTED, "pages/auction/my", Map.of(
                "thing11", thing(productName),
                "time2", time(listedAt),
                "amount3", money(price)));
    }

    public void notifyProductListed(Long userId, String productName, Integer price, LocalDateTime listedAt) {
        sendAfterCommit(userId, PRODUCT_LISTED, "pages/index/trade", Map.of(
                "thing11", thing(productName),
                "time2", time(listedAt),
                "amount3", money(price)));
    }

    public void notifyGetbackAccepted(Long userId, String productName, LocalDateTime acceptedAt) {
        sendAfterCommit(userId, REVIEW_RESULT, "pages/collection/getbacklog", Map.of(
                "phrase1", phrase("已受理"),
                "thing2", thing("藏品取回"),
                "thing3", thing("请等待仓库发货"),
                "time4", time(acceptedAt),
                "thing5", thing(productName)));
    }

    public void notifyProductSold(Long userId, String productName, Integer price, LocalDateTime soldAt) {
        sendAfterCommit(userId, PRODUCT_SOLD, "pages/index/user", Map.of(
                "thing1", thing(productName),
                "amount2", money(price),
                "time3", time(soldAt),
                "thing4", thing("款项已进入钱包")));
    }

    public void notifyAuctionSuccess(Long userId, String productName, Integer price, LocalDateTime endTime) {
        sendAfterCommit(userId, AUCTION_SUCCESS, "pages/auction/my", Map.of(
                "thing1", thing(productName),
                "amount2", money(price),
                "time3", time(endTime),
                "thing4", thing("请在钱包中核对到账金额")));
    }

    public void notifyReviewResult(Long userId, String status, String applicationType,
                                   String tip, LocalDateTime reviewedAt, String activityName) {
        sendAfterCommit(userId, REVIEW_RESULT, "pages/auction/my", Map.of(
                "phrase1", phrase(status),
                "thing2", thing(applicationType),
                "thing3", thing(tip),
                "time4", time(reviewedAt),
                "thing5", thing(activityName)));
    }

    public void notifyOrderStatus(Long userId, String trackingNumber, String status, String tip) {
        sendAfterCommit(userId, ORDER_STATUS, "pages/collection/getbacklog", Map.of(
                "character_string1", chars(trackingNumber),
                "phrase2", phrase(status),
                "thing3", thing(tip)));
    }

    public void notifyPaymentSuccess(Long userId, Integer price, LocalDateTime paidAt,
                                     String productName, Integer quantity) {
        sendAfterCommit(userId, PAYMENT_SUCCESS, "pages/order/list", Map.of(
                "amount1", money(price),
                "time2", time(paidAt),
                "thing3", thing(productName),
                "thing4", thing("支付成功"),
                "number5", String.valueOf(quantity == null ? 1 : quantity)));
    }

    public void notifyWithdrawStatus(Long userId, LocalDateTime appliedAt, Integer price,
                                     String status, String remark) {
        sendAfterCommit(userId, WITHDRAW_STATUS, "pages/commission/withdraw", Map.of(
                "time1", time(appliedAt),
                "amount2", money(price),
                "phrase3", phrase(status),
                "thing4", thing(remark)));
    }

    public void sendAfterCommit(Long userId, String templateTitle, String page, Map<String, String> messages) {
        if (userId == null) {
            return;
        }
        Runnable action = () -> sendSafely(userId, templateTitle, page, messages);
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private void sendSafely(Long userId, String templateTitle, String page, Map<String, String> messages) {
        try {
            appInboxMessageService.send(userId, templateTitle, messages);
        } catch (Exception ex) {
            log.error("[sendSafely][站内消息保存失败 userId={} template={}]", userId, templateTitle, ex);
        }
        try {
            SocialWxaSubscribeMessageSendReqDTO request = new SocialWxaSubscribeMessageSendReqDTO();
            request.setUserId(userId);
            request.setUserType(MEMBER.getValue());
            request.setTemplateTitle(templateTitle);
            request.setPage(page);
            request.setMessages(messages);
            socialClientApi.sendWxaSubscribeMessage(request);
        } catch (Exception ex) {
            log.warn("[sendSafely][订阅消息发送失败 userId={} template={}]", userId, templateTitle, ex);
        }
    }

    public static String time(LocalDateTime value) {
        return value == null ? "-" : TIME_FORMATTER.format(value);
    }

    public static String money(Integer cents) {
        return "¥" + BigDecimal.valueOf(cents == null ? 0 : cents, 2).toPlainString();
    }

    public static String thing(String value) {
        return limit(value, 20);
    }

    public static String phrase(String value) {
        return limit(value, 5);
    }

    public static String chars(String value) {
        return limit(value, 32);
    }

    private static String limit(String value, int length) {
        String text = value == null || value.isBlank() ? "-" : value.trim();
        return text.length() <= length ? text : text.substring(0, length);
    }
}
