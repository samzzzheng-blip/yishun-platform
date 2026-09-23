package com.kiss.yishun.service;

import com.kiss.yishun.entity.Precious;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface PreciousService {
    Precious findPreciousByCertNumber(String certNo, int status);
    Precious findPreciousById(long id);
    void delPrecious(long id);
    void updatePrecious(Precious precious);
    void addPrecious(Precious precious);
    Page<Precious> findPreciousPageByKeywords(PageRequest pageRequest, String keywords, Integer status);
    List<Precious> findAllByUpdatedateBetween(long startTime, long endTime);
    Integer existSameCertNumber(String certno);
    Integer existsCertNumberUpdate(String certno, Long id);
}
