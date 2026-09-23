package com.techtron.onebook.module.app.controller.admin.workbench;

import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/** Durable, per-administrator read receipts; business records are never modified. */
@Component
public class WorkbenchReadStore {
    @Resource private JdbcTemplate jdbc;
    public List<String> unread(String key, List<Map<String, Object>> records) {
        Set<String> read = new HashSet<>(jdbc.queryForList(
                "SELECT item_token FROM app_workbench_read WHERE user_id=? AND queue_key=?", String.class, userId(), key));
        return records.stream().map(WorkbenchReadStore::token).filter(token -> !read.contains(token)).toList();
    }
    @Transactional(rollbackFor = Exception.class)
    public void markRead(String key, List<String> tokens) {
        Long user = userId();
        if (tokens.isEmpty()) return;
        jdbc.batchUpdate("INSERT IGNORE INTO app_workbench_read(user_id,queue_key,item_token) VALUES(?,?,?)",
                tokens.stream().map(token -> new Object[]{user, key, token}).toList());
    }
    private Long userId() { return Objects.requireNonNull(getLoginUserId(), "Administrator login required"); }
    static String token(Map<String, Object> row) {
        try {
            String version = Objects.requireNonNull(row.get("id")) + ":" + Objects.toString(row.get("update_time"), "");
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(version.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
