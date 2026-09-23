package com.kiss.yishun.service;

import com.kiss.yishun.entity.SmartImageRecord;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface SmartImageRecordService {
    List<SmartImageRecord> findByFileName(int type, String fileName, Long userId, PageRequest pageRequest);

    List<SmartImageRecord> findLastList(int type, Long userId, PageRequest pageRequest);

    void addRecord(SmartImageRecord record);

    boolean deleteImageRecord(Long userId, Long id);
}
