package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.PokemonCardDao;
import com.kiss.yishun.entity.PokemonCard;
import com.kiss.yishun.service.SmartCardService;
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
public class SmartCardServiceImpl implements SmartCardService {

    @Autowired
    private PokemonCardDao cardDao;

    @Override
    public Page<PokemonCard> findAllCard(String alias, String code, PageRequest page) {
//        return cardDao.findAllCardByAliasLikeAndCodeLikeOrderByUpdatedateDesc('%'+alias+'%','%'+code+'%', page);
        return cardDao.findAll((Specification<PokemonCard>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (!StrUtils.isEmpty(alias)) {
                list.add(criteriaBuilder.like(root.get("alias"),'%'+alias+'%'));
            }
            if (!StrUtils.isEmpty(code)) {
                list.add(criteriaBuilder.like(root.get("code"), '%'+code+'%'));
            }
            return criteriaBuilder.and(list.toArray(new Predicate[0]));
        }, PageRequest.of(page.getPageNumber(),page.getPageSize(), Sort.by(Sort.Direction.DESC, "updatedate")));
    }

    @Override
    public PokemonCard findCardByAliasAndCode(String alias, String code) {
        return cardDao.findByAliasEqualsAndCodeEquals(alias, code);
    }

    @Override
    public long addCard(PokemonCard pokemonCard) {
        return cardDao.saveAndFlush(pokemonCard).getId();
    }

    @Override
    public void updateCard(PokemonCard pokemonCard) {
        cardDao.saveAndFlush(pokemonCard);
    }

    @Override
    public PokemonCard findCardById(Long id) {
        return cardDao.findById(id).get();
    }
}
