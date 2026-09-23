package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.RateDao;
import com.kiss.yishun.entity.Rate;
import com.kiss.yishun.service.RateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateServiceImpl implements RateService {

    @Autowired
    private RateDao rateDao;

    @Override
    public Rate findRateByCertNumber(String certno) {
        return rateDao.findRateByCertNumber(certno);
    }

    @Override
    public Rate findRateById(long id) {
        return rateDao.findRateById(id);
    }

    @Override
    public void delRate(long id) {
        rateDao.deleteRateById(id);
    }

    @Override
    public void updateRate(Rate Rate) {
        rateDao.saveAndFlush(Rate);
    }

    @Override
    public void addRate(Rate Rate) {
        rateDao.save(Rate);
    }

    @Override
    public Page<Rate> findRatePageByKeywords(PageRequest pageRequest, String keywords) {
        return rateDao.findAllByCertNumberLikeOrderByUpdatedateDesc('%'+keywords+'%',pageRequest);
    }

    @Override
    public Page<Rate> findRatePageByFilters(PageRequest pageRequest, String keywords, Long startNumber, Long endNumber) {
        keywords = (keywords == null ? "" : keywords.trim()).replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return rateDao.findPageByFilters(keywords, startNumber, endNumber, pageRequest);
    }

    @Override
    public List<Rate> findAllByCertNumberRange(Long startNumber, Long endNumber) {
        return rateDao.findAllByCertNumberRange(startNumber, endNumber);
    }

    @Override
    public List<Rate> findAllByUpdatedateBetween(long startTime, long endTime) {
        return rateDao.findAllByUpdatedateBetween(startTime, endTime);
    }

    @Override
    public Integer existSameCertNumber(String certno) {
        return rateDao.existsCertNumber(certno);
    }

    @Override
    public Integer existsCertNumberUpdate(String certno, Long id) {
        return rateDao.existsCertNumberUpdate(certno, id);
    }
}
