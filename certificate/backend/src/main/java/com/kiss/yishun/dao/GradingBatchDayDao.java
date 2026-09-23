package com.kiss.yishun.dao;
import com.kiss.yishun.entity.GradingBatchDay;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import javax.persistence.LockModeType;
import java.util.Optional;
public interface GradingBatchDayDao extends JpaRepository<GradingBatchDay,String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from GradingBatchDay d where d.dayActor=:key")
    Optional<GradingBatchDay> lockDay(@Param("key") String key);
}
