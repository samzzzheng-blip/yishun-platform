package com.kiss.yishun.dao;

import com.kiss.yishun.entity.SmartImageRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmartImageRecordDao extends JpaRepository<SmartImageRecord, Long> {

    List<SmartImageRecord> findByUserIdAndTypeAndNameLikeOrderByUpdatedateDesc(Long userId, int type, String fileName, Pageable pageable);

    List<SmartImageRecord> findAllByUserIdEqualsAndTypeEqualsOrderByUpdatedateDesc(Long userId, int type, Pageable pageable);

}
