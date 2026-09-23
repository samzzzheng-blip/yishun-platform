package com.kiss.yishun.dao;

import com.kiss.yishun.entity.SmartScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SmartScoreDao extends JpaRepository<SmartScore, Long>, JpaSpecificationExecutor<SmartScore> {
    Page<SmartScore> findAllByUserIdEqualsAndCertNumberLikeAndUpdatedateBetweenOrderByUpdatedateDesc(Long userId, String certNumber, long startTime, long endTime, Pageable pageable);

    @Query(value = "select count(1) from smart_score where updatedate between ?1 and ?2",nativeQuery = true)
    int countToday(long startTime, long endTime);

    Page<SmartScore> findAllByUserPhoneLikeAndCertNumberLikeAndUpdatedateBetweenOrderByUpdatedateDesc(String phone, String certNumber, long startTime, long endTime, Pageable pageable);
}
