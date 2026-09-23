package com.techtron.onebook.module.app.controller.admin.workbench;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.security.core.service.SecurityFrameworkService;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.getback.GetbackMapper;
import com.techtron.onebook.module.app.dal.mysql.exchangelog.ExchangeLogMapper;
import com.techtron.onebook.module.app.dal.mysql.fasttrade.FastTradeMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionMapper;
import com.techtron.onebook.module.app.dal.mysql.brokerage.BrokerageWithdrawMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.function.Supplier;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

/** Operational reminders. Reading a reminder never changes its business status. */
@Slf4j
@RestController
@RequestMapping("/app/workbench")
public class WorkbenchController {
    @Resource private SecurityFrameworkService security;
    @Resource private CollectionMapper collectionMapper;
    @Resource private GetbackMapper getbackMapper;
    @Resource private ExchangeLogMapper exchangeLogMapper;
    @Resource private FastTradeMapper fastTradeMapper;
    @Resource private YikoujiaMapper yikoujiaMapper;
    @Resource private AuctionMapper auctionMapper;
    @Resource private BrokerageWithdrawMapper withdrawMapper;
    @Resource private JdbcTemplate jdbc;
    @Resource private WorkbenchReadStore readStore;
    @Value("${onebook.inbound.enabled:false}") private boolean inboundEnabled;

    public record QueueCount(String key, Long count, boolean available, List<String> snapshot) {}
    public record ReadRequest(@NotBlank String key, @NotNull @Size(max=10000) List<@NotBlank @Size(max=64) String> snapshot) {}

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<QueueCount>> pending() {
        List<QueueCount> rows = new ArrayList<>();
        add(rows, "collection-review", "app:collection:query", () -> count(collectionMapper, 0));
        if (inboundEnabled) {
            add(rows, "parcel-receive", "app:collection:query", () -> jdbc.queryForList(
                    "SELECT id, update_time FROM app_inbound_parcel WHERE status=0"));
            add(rows, "parcel-inspect", "app:collection:query", () -> jdbc.queryForList(
                    "SELECT id, update_time FROM app_inbound_parcel WHERE status=1"));
        }
        add(rows, "getback-ship", "app:getback:query", () -> count(getbackMapper, 0));
        add(rows, "exchange-ship", "app:exchange-log:query", () -> count(exchangeLogMapper, 0));
        add(rows, "fasttrade-review", "app:fast-trade:query", () -> count(fastTradeMapper, 0));
        add(rows, "yikoujia-publish", "app:yikoujia:query", () -> count(yikoujiaMapper, 0));
        add(rows, "yikoujia-settle", "app:yikoujia:query", () -> yikoujiaMapper.selectMaps(
                new QueryWrapper<com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO>()
                        .select("id", "update_time").eq("status", 4).isNotNull("collection_id")));
        add(rows, "yikoujia-conflict", "app:yikoujia:query", () -> count(yikoujiaMapper, 6));
        add(rows, "auction-review", "app:auction:query", () -> count(auctionMapper, 0));
        add(rows, "auction-failed", "app:auction:query", () -> count(auctionMapper, 6));
        add(rows, "auction-confirm", "app:auction:query", () -> count(auctionMapper, 2));
        add(rows, "auction-delist", "app:auction:query", () -> auctionMapper.selectMaps(
                new QueryWrapper<com.techtron.onebook.module.app.dal.dataobject.auction.AuctionDO>()
                        .select("id", "update_time").eq("status", 1).eq("delist_status", 1)));
        add(rows, "withdraw-review", "trade:brokerage-withdraw:query", () -> count(withdrawMapper, 0));
        add(rows, "withdraw-failed", "trade:brokerage-withdraw:query", () -> count(withdrawMapper, 21));
        return success(rows);
    }
    @PostMapping("/read")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> markRead(@Valid @RequestBody ReadRequest request) {
        // Recheck permissions and intersect with the displayed snapshot: later arrivals stay unread.
        QueueCount current = pending().getData().stream().filter(row -> row.key().equals(request.key()))
                .findFirst().orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("无此待办权限"));
        if (!current.available()) throw new IllegalStateException("待办查询失败，请稍后重试");
        Set<String> shown = new HashSet<>(request.snapshot());
        readStore.markRead(request.key(), current.snapshot().stream().filter(shown::contains).toList());
        return success(true);
    }
    private <T> List<Map<String, Object>> count(BaseMapper<T> mapper, int status) {
        return mapper.selectMaps(new QueryWrapper<T>().select("id", "update_time").eq("status", status));
    }
    private void add(List<QueueCount> rows, String key, String permission, Supplier<List<Map<String, Object>>> query) {
        if (!security.hasPermission(permission)) return;
        try {
            List<String> unread = readStore.unread(key, query.get());
            rows.add(new QueueCount(key, (long) unread.size(), true, unread));
        } catch (Exception ex) {
            log.warn("Unable to load workbench queue {}", key, ex);
            rows.add(new QueueCount(key, null, false, List.of()));
        }
    }
}
