package com.kiss.yishun.dao;

import com.kiss.yishun.entity.SmartAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmartAuditDao extends JpaRepository<SmartAudit, Long> {
    Page<SmartAudit> findAllByStatusEqualsOrderByUpdatedateDesc(int status, Pageable pageable);
}
