package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.SmartScoreDao;
import com.kiss.yishun.entity.SmartScore;
import com.kiss.yishun.service.SmartScoreService;
import com.kiss.yishun.utils.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Service
public class SmartScoreServiceImpl implements SmartScoreService {

    @Autowired
    private SmartScoreDao scoreDao;

    @Override
    public Page<SmartScore> getScoreRecord(Long userId, String certNumber, long startTime, long endTime, PageRequest pageRequest) {
        return scoreDao.findAllByUserIdEqualsAndCertNumberLikeAndUpdatedateBetweenOrderByUpdatedateDesc(userId, '%'+certNumber+'%', startTime, endTime, pageRequest);
    }

    @Override
    public SmartScore getScoreDetail(Long id) {
        return scoreDao.findById(id).get();
    }

    @Override
    public long addScore(SmartScore score) {
        SmartScore smartScore = scoreDao.saveAndFlush(score);
        return smartScore.getId();
    }

    @Override
    public int getTodayCount(long startTime, long endTime) {
        return scoreDao.countToday(startTime, endTime);
    }

    @Override
    public void updateScore(SmartScore score) {
        scoreDao.saveAndFlush(score);
    }

    @Override
    public Page<SmartScore> getAllScoreList(String phone, String certNumber, long startTime, long endTime, PageRequest pageRequest) {
        return scoreDao.findAll((Specification<SmartScore>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!StrUtils.isEmpty(phone)) {
                predicates.add(criteriaBuilder.like(root.get("phone"), '%'+phone+'%'));
            }
            if (!StrUtils.isEmpty(certNumber)) {
                predicates.add(criteriaBuilder.like(root.get("certNumber"), '%'+certNumber+'%'));
            }
            if (startTime > 0 && endTime > 0) {
                predicates.add(criteriaBuilder.between(root.get("updatedate"), startTime, endTime));
            } else if (startTime > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("updatedate"), startTime));
            } else if (endTime > 0){
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedate"), endTime));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, PageRequest.of(pageRequest.getPageNumber(),pageRequest.getPageSize(), Sort.Direction.DESC,"updatedate"));

//        return scoreDao.findAllByUserPhoneLikeAndCertNumberLikeAndUpdatedateBetweenOrderByUpdatedateDesc(phone, certNumber, startTime, endTime, pageRequest);
    }
}
