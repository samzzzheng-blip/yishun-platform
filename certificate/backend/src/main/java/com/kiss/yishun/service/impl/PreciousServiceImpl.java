package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.PreciousDao;
import com.kiss.yishun.entity.Precious;
import com.kiss.yishun.service.PreciousService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreciousServiceImpl implements PreciousService {

    @Autowired
    private PreciousDao preciousDao;

    @Override
    public Precious findPreciousByCertNumber(String certNo, int status) {
        if (status == -1) {
            // 全部
            return preciousDao.findPreciousByCertNumber(certNo);
        } else {
            // 指定
            return preciousDao.findPreciousByCertNumberAndStatusEquals(certNo, status);
        }
    }

    @Override
    public Precious findPreciousById(long id) {
        return preciousDao.findPreciousById(id);
    }

    @Override
    public void delPrecious(long id) {
        preciousDao.deletePreciousById(id);
    }

    @Override
    public void updatePrecious(Precious precious) {
        preciousDao.saveAndFlush(precious);
    }

    @Override
    public void addPrecious(Precious precious) {
        preciousDao.save(precious);
    }

    @Override
    public Page<Precious> findPreciousPageByKeywords(PageRequest pageRequest, String keywords, Integer status) {
        String keyword = keywords == null ? "" : keywords.trim();
        String pattern = "%" + keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        return preciousDao.searchByNumberOrSigner(pattern, status == null ? -1 : status, pageRequest);
    }

    @Override
    public List<Precious> findAllByUpdatedateBetween(long startTime, long endTime) {
        return preciousDao.findAllByUpdatedateBetween(startTime, endTime);
    }

    @Override
    public Integer existSameCertNumber(String certno) {
        return preciousDao.existsCertNumber(certno);
    }

    @Override
    public Integer existsCertNumberUpdate(String certno, Long id) {
        return preciousDao.existsCertNumberUpdate(certno, id);
    }
}
