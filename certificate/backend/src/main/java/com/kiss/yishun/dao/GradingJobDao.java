package com.kiss.yishun.dao;
import com.kiss.yishun.entity.GradingJob;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import javax.persistence.LockModeType;
import java.util.Optional;
public interface GradingJobDao extends JpaRepository<GradingJob,Long>, JpaSpecificationExecutor<GradingJob> {
    Optional<GradingJob> findByJobNumber(String jobNumber);
    java.util.List<GradingJob> findByBatchAndCreatedByOrderByIdAsc(String batch,String createdBy);
    boolean existsByBatchAndBatchNumberAndIdNot(String batch,Long batchNumber,Long id);
    @Query("select max(j.batchNumber) from GradingJob j where j.batch=:batch")
    Long maxBatchNumber(@Param("batch") String batch);
    @Query("select count(j) from GradingJob j where j.rateId is not null and (j.frontPhoto=:photo or j.backPhoto=:photo or j.finishedPhoto=:photo)")
    long countPublishedPhoto(@Param("photo") String photo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from GradingJob j where j.id=:id")
    Optional<GradingJob> lockById(@Param("id") Long id);
}
