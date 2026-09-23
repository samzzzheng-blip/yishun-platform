package com.kiss.yishun.service;

import com.kiss.yishun.entity.SmartScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface SmartScoreService {
    Page<SmartScore> getScoreRecord(Long userId, String certNumber, long startTime, long endTime, PageRequest pageRequest);

    SmartScore getScoreDetail(Long id);

    long addScore(SmartScore score);

    int getTodayCount(long startTime, long endTime);

    void updateScore(SmartScore score);

    Page<SmartScore> getAllScoreList(String phone, String certNumber, long startTime, long endTime, PageRequest pageRequest);
}
