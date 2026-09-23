package com.kiss.yishun.dao;

import com.kiss.yishun.entity.Cartoon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface CartoonDao extends JpaRepository<Cartoon,Long> {
    Cartoon findCartoonByCertNumber(String certNo);
    Cartoon findCartoonById(long id);

    @Transactional
    void deleteCartoonById(long id);

    Page<Cartoon> findAllByCertNumberLikeOrderByUpdatedateDesc(String keywords,Pageable pageable);

    @Query("select c from Cartoon c where lower(c.certNumber) like lower(:pattern) escape '!' " +
            "or lower(c.roleName) like lower(:pattern) escape '!' " +
            "or lower(c.cartoonName) like lower(:pattern) escape '!' order by c.updatedate desc, c.id desc")
    Page<Cartoon> searchByNumberOrName(@Param("pattern") String pattern, Pageable pageable);

    List<Cartoon> findAllByUpdatedateBetween(long startTime, long endTime);

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
                            "   OR EXISTS(SELECT 1 FROM rate WHERE cert_number = :certNumber) " +
                            "   OR EXISTS(SELECT 1 FROM cartoon WHERE cert_number = :certNumber AND id <> :id) " +
                            "THEN 1 ELSE 0 END",
            nativeQuery = true
    )
    Integer existsCertNumberUpdate(@Param("certNumber") String certNumber, @Param("id") Long id);
}
