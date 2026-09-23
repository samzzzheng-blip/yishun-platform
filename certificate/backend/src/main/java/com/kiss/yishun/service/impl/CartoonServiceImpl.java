package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.CartoonDao;
import com.kiss.yishun.entity.Cartoon;
import com.kiss.yishun.service.CartoonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartoonServiceImpl implements CartoonService {

    @Autowired
    private CartoonDao cartoonDao;

    @Override
    public Cartoon findCartoonByCertNumber(String certno) {
        return cartoonDao.findCartoonByCertNumber(certno);
    }

    @Override
    public Cartoon findCartoonById(long id) {
        return cartoonDao.findCartoonById(id);
    }

    @Override
    public void delCartoon(long id) {
        cartoonDao.deleteCartoonById(id);
    }

    @Override
    public void updateCartoon(Cartoon cartoon) {
        cartoonDao.saveAndFlush(cartoon);
    }

    @Override
    public void addCartoon(Cartoon cartoon) {
        cartoonDao.save(cartoon);
    }

    @Override
    public Page<Cartoon> findCartoonPageByKeywords(PageRequest pageRequest, String keywords) {
        String pattern = "%" + (keywords == null ? "" : keywords.trim()).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        return cartoonDao.searchByNumberOrName(pattern, pageRequest);
    }

    @Override
    public List<Cartoon> findAllByUpdatedateBetween(long startTime, long endTime) {
        return cartoonDao.findAllByUpdatedateBetween(startTime, endTime);
    }

    @Override
    public Integer existSameCertNumber(String certno) {
        return cartoonDao.existsCertNumber(certno);
    }

    @Override
    public Integer existsCertNumberUpdate(String certno, Long id) {
        return cartoonDao.existsCertNumberUpdate(certno, id);
    }
}
