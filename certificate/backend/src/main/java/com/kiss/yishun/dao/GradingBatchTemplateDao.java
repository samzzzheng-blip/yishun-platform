package com.kiss.yishun.dao;
import com.kiss.yishun.entity.GradingBatchTemplate;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import javax.persistence.LockModeType;
import java.util.Optional;
public interface GradingBatchTemplateDao extends JpaRepository<GradingBatchTemplate,String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from GradingBatchTemplate b where b.batch=:batch")
    Optional<GradingBatchTemplate> lockBatch(@Param("batch") String batch);
}
