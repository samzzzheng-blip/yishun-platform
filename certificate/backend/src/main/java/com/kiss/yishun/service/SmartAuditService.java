package com.kiss.yishun.service;

import com.kiss.yishun.entity.SmartAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface SmartAuditService {
    Page<SmartAudit> getAuditRecord(PageRequest pageRequest);

    void updateAudit(SmartAudit audit);

    long addAudit(SmartAudit audit);

    SmartAudit findAuditById(Long id);

    void rejectAudit(Long id);
}
