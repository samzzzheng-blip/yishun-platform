package com.kiss.yishun.service;

import com.kiss.yishun.entity.Rate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RateService {
    Rate findRateByCertNumber(String certno);
    Rate findRateById(long id);
    void delRate(long id);

    void updateRate(Rate Rate);
    void addRate(Rate Rate);
    Page<Rate> findRatePageByKeywords(PageRequest pageRequest, String keywords);
    Page<Rate> findRatePageByFilters(PageRequest pageRequest, String keywords, Long startNumber, Long endNumber);
    List<Rate> findAllByCertNumberRange(Long startNumber, Long endNumber);
    List<Rate> findAllByUpdatedateBetween(long startTime, long endTime);
    Integer existSameCertNumber(String certno);
    Integer existsCertNumberUpdate(String certno, Long id);
}
