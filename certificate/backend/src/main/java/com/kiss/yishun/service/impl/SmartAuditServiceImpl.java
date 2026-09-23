package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.SmartAuditDao;
import com.kiss.yishun.entity.SmartAudit;
import com.kiss.yishun.entity.enums.SmartAuditEnum;
import com.kiss.yishun.service.SmartAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SmartAuditServiceImpl implements SmartAuditService {

    @Autowired
    private SmartAuditDao auditDao;

    @Override
    public Page<SmartAudit> getAuditRecord(PageRequest pageRequest) {
        return auditDao.findAllByStatusEqualsOrderByUpdatedateDesc(SmartAuditEnum.WAIT.getCode(), pageRequest);
    }

    @Override
    public void updateAudit(SmartAudit audit) {
        auditDao.saveAndFlush(audit);
    }

    @Override
    public long addAudit(SmartAudit audit) {
        SmartAudit newAudit = auditDao.saveAndFlush(audit);
        return newAudit.getId();
    }

    @Override
    public SmartAudit findAuditById(Long id) {
        return auditDao.findById(id).get();
    }

    @Override
    public void rejectAudit(Long id) {
        SmartAudit audit = auditDao.findById(id).get();
        if (audit != null) {
            audit.setStatus(SmartAuditEnum.REJECT.getCode());
            auditDao.saveAndFlush(audit);
        }
    }
}
