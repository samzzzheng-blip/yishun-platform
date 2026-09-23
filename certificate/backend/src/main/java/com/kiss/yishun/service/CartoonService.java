package com.kiss.yishun.service;

import com.kiss.yishun.entity.Cartoon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartoonService {
    Cartoon findCartoonByCertNumber(String certno);
    Cartoon findCartoonById(long id);
    void delCartoon(long id);

    void updateCartoon(Cartoon cartoon);
    void addCartoon(Cartoon cartoon);
    Page<Cartoon> findCartoonPageByKeywords(PageRequest pageRequest, String keywords);

    List<Cartoon> findAllByUpdatedateBetween(long startTime, long endTime);

    Integer existSameCertNumber(String certno);

    Integer existsCertNumberUpdate(String certno, Long id);
}
