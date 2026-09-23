package com.kiss.yishun.dao;
import com.kiss.yishun.entity.GradingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface GradingEventDao extends JpaRepository<GradingEvent,Long> {
    List<GradingEvent> findByJobIdOrderByCreatedAtDesc(Long jobId);
}

