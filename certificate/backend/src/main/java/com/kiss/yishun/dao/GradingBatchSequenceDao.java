package com.kiss.yishun.dao;
import com.kiss.yishun.entity.GradingBatchSequence;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import javax.persistence.LockModeType;
import java.util.Optional;
public interface GradingBatchSequenceDao extends JpaRepository<GradingBatchSequence,String> {
    Optional<GradingBatchSequence> findByRequestId(String requestId);
    java.util.List<GradingBatchSequence> findByCreatedByOrderByCreatedAtDesc(String actor);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from GradingBatchSequence s where s.batch=:batch")
    Optional<GradingBatchSequence> lockBatch(@Param("batch") String batch);
}
