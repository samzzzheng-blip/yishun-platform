package com.kiss.yishun.dao;

import com.kiss.yishun.entity.Precious;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface PreciousDao extends JpaRepository<Precious,Long> {
    @Query("select p from Precious p where " +
            "(lower(p.certNumber) like lower(:pattern) escape '!' or lower(p.signer) like lower(:pattern) escape '!') " +
            "and (:status = -1 or p.status = :status) order by p.updatedate desc, p.id desc")
    Page<Precious> searchByNumberOrSigner(@Param("pattern") String pattern,
                                         @Param("status") int status, Pageable pageable);

    Precious findPreciousByCertNumber(String certNo);

    Precious findPreciousByCertNumberAndStatusEquals(String certNo, int status);

    Precious findPreciousById(long id);

    @Transactional
    void deletePreciousById(long id);

    Page<Precious> findAllByCertNumberLikeOrderByUpdatedateDesc(String keywords,Pageable pageable);

    Page<Precious> findAllByCertNumberLikeAndStatusEqualsOrderByUpdatedateDesc(String keywords,int status, Pageable pageable);

    List<Precious> findAllByUpdatedateBetween(long startTime, long endTime);

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
                            "WHEN EXISTS(SELECT 1 FROM precious WHERE cert_number = :certNumber AND id <> :id) " +
                            "   OR EXISTS(SELECT 1 FROM rate WHERE cert_number = :certNumber) " +
                            "   OR EXISTS(SELECT 1 FROM cartoon WHERE cert_number = :certNumber) " +
                            "THEN 1 ELSE 0 END",
            nativeQuery = true
    )
    Integer existsCertNumberUpdate(@Param("certNumber") String certNumber, @Param("id") Long id);

}
