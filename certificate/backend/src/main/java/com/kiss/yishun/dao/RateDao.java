package com.kiss.yishun.dao;

import com.kiss.yishun.entity.Rate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface RateDao extends JpaRepository<Rate,Long> {
    Rate findRateByCertNumber(String certNo);
    Rate findRateById(long id);

    @Transactional
    void deleteRateById(long id);

    Page<Rate> findAllByCertNumberLikeOrderByUpdatedateDesc(String keywords,Pageable pageable);

    @Query(
            value = "SELECT * FROM rate WHERE " +
                    "(:keywords = '' OR LOWER(cert_number) LIKE LOWER(CONCAT('%', :keywords, '%')) ESCAPE '!' OR LOWER(rate_name) LIKE LOWER(CONCAT('%', :keywords, '%')) ESCAPE '!') " +
                    "AND (:startNumber IS NULL OR (cert_number REGEXP '^[0-9]+$' AND CAST(cert_number AS UNSIGNED) >= :startNumber)) " +
                    "AND (:endNumber IS NULL OR (cert_number REGEXP '^[0-9]+$' AND CAST(cert_number AS UNSIGNED) <= :endNumber)) " +
                    "ORDER BY updatedate DESC, id DESC",
            countQuery = "SELECT COUNT(*) FROM rate WHERE " +
                    "(:keywords = '' OR LOWER(cert_number) LIKE LOWER(CONCAT('%', :keywords, '%')) ESCAPE '!' OR LOWER(rate_name) LIKE LOWER(CONCAT('%', :keywords, '%')) ESCAPE '!') " +
                    "AND (:startNumber IS NULL OR (cert_number REGEXP '^[0-9]+$' AND CAST(cert_number AS UNSIGNED) >= :startNumber)) " +
                    "AND (:endNumber IS NULL OR (cert_number REGEXP '^[0-9]+$' AND CAST(cert_number AS UNSIGNED) <= :endNumber))",
            nativeQuery = true
    )
    Page<Rate> findPageByFilters(@Param("keywords") String keywords,
                                 @Param("startNumber") Long startNumber,
                                 @Param("endNumber") Long endNumber,
                                 Pageable pageable);

    @Query(
            value = "SELECT * FROM rate WHERE cert_number REGEXP '^[0-9]+$' " +
                    "AND (:startNumber IS NULL OR CAST(cert_number AS UNSIGNED) >= :startNumber) " +
                    "AND (:endNumber IS NULL OR CAST(cert_number AS UNSIGNED) <= :endNumber) " +
                    "ORDER BY CAST(cert_number AS UNSIGNED) ASC",
            nativeQuery = true
    )
    List<Rate> findAllByCertNumberRange(@Param("startNumber") Long startNumber,
                                        @Param("endNumber") Long endNumber);

    List<Rate> findAllByUpdatedateBetween(long startTime, long endTime);

    @Query(
            value =
                    "SELECT CASE " +
                            "WHEN EXISTS(SELECT 1 FROM precious WHERE cert_number = :certNumber) " +
                            "   OR EXISTS(SELECT 1 FROM rate WHERE cert_number = :certNumber) " +
                            "   OR EXISTS(SELECT 1 FROM cartoon WHERE cert_number = :certNumber) " +
                            "THEN 1 ELSE 0 END",
            nativeQuery = true
    )
    Integer existsCertNumber(@Param("certNumber") String certNumber);

    @Query(
            value =
                    "SELECT CASE " +
                            "WHEN EXISTS(SELECT 1 FROM precious WHERE cert_number = :certNumber) " +
                            "   OR EXISTS(SELECT 1 FROM rate WHERE cert_number = :certNumber AND id <> :id) " +
                            "   OR EXISTS(SELECT 1 FROM cartoon WHERE cert_number = :certNumber) " +
                            "THEN 1 ELSE 0 END",
            nativeQuery = true
    )
    Integer existsCertNumberUpdate(@Param("certNumber") String certNumber, @Param("id") Long id);

}
