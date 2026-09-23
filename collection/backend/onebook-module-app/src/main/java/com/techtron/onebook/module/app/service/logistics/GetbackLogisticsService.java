package com.techtron.onebook.module.app.service.logistics;

import com.techtron.onebook.module.app.dal.mysql.getback.GetbackMapper;
import com.techtron.onebook.module.app.dal.dataobject.getback.GetbackDO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import lombok.RequiredArgsConstructor;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.GETBACK_NOT_EXISTS;

@Service
@RequiredArgsConstructor
public class GetbackLogisticsService {
    private final GetbackMapper getbacks;
    private final Kuaidi100Client client;
    private final StringRedisTemplate redis;
    private final ObjectMapper json = new ObjectMapper();
    private static final Duration TTL = Duration.ofMinutes(30);
    public static final Set<String> CARRIERS = Set.of("shunfeng", "zhongtong", "yuantong", "shentong", "yunda", "jd", "ems", "youzhengguonei", "debangkuaidi", "jtexpress");

    public LogisticsResult get(Long id, Long userId) {
        GetbackDO order = id == null ? null : getbacks.selectById(id);
        if (userId == null || order == null || !Objects.equals(order.getUserId(), userId)) throw exception(GETBACK_NOT_EXISTS);
        if (!Set.of(1, 2).contains(order.getStatus() == null ? -1 : order.getStatus())
            || order.getDeliverCode() == null || order.getDeliverCode().isBlank())
            return LogisticsResult.unavailable("NOT_SHIPPED", "尚未发货，发货后可查看物流。");
        String number = order.getDeliverCode().trim();
        String carrier = order.getExpressCompany();
        // Only SF-prefixed historical waybills are unambiguous; never guess numeric waybills.
        if ((carrier == null || carrier.isBlank()) && number.toUpperCase(Locale.ROOT).startsWith("SF")) carrier = "shunfeng";
        if (!CARRIERS.contains(carrier == null ? "" : carrier))
            return LogisticsResult.unavailable("MISSING_CARRIER", "快递公司信息待补充，请联系客服核实。");
        if (!client.available()) return LogisticsResult.unavailable("NOT_CONFIGURED", "物流查询暂未开通，可复制单号到快递官方渠道查询。");
        String cacheKey = "app:getback:logistics:" + DigestUtils.md5DigestAsHex(
            (carrier + ":" + number + ":" + order.getReceiverMobile()).getBytes(StandardCharsets.UTF_8));
        try {
            String cached = redis.opsForValue().get(cacheKey);
            if (cached != null) return json.readValue(cached, LogisticsResult.class);
            // Reserve before sending: one external request per waybill/30 minutes, including failures.
            String pending = json.writeValueAsString(LogisticsResult.unavailable("PENDING", "物流查询中，请稍后刷新。"));
            if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(cacheKey, pending, TTL)))
                return LogisticsResult.unavailable("PENDING", "物流查询中，请稍后刷新。");
            LogisticsResult result;
            try { result = client.query(carrier, number, order.getReceiverMobile()); }
            catch (Exception ignored) { result = unavailable(); }
            redis.opsForValue().set(cacheKey, json.writeValueAsString(result), TTL);
            return result;
        } catch (Exception ignored) {
            // Redis failure must not bypass the vendor's query frequency limit.
            return unavailable();
        }
    }
    private static LogisticsResult unavailable() {
        return LogisticsResult.unavailable("UNAVAILABLE", "物流查询暂不可用，请稍后再试，或复制单号到快递官方渠道查询。");
    }
}
