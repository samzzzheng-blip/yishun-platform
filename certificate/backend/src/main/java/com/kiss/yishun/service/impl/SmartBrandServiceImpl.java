package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.PokemonBrandDao;
import com.kiss.yishun.dao.PokemonCardDao;
import com.kiss.yishun.entity.PokemonBrand;
import com.kiss.yishun.service.SmartBrandService;
import com.kiss.yishun.utils.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SmartBrandServiceImpl implements SmartBrandService {

    @Autowired
    private PokemonBrandDao brandDao;

    @Autowired
    private PokemonCardDao cardDao;

    @Override
    public Page<PokemonBrand> findAllBrand(String alias, PageRequest page) {
//        return brandDao.findAllByAliasLikeOrderByUpdatedateDesc('%'+alias+'%',page);
        return brandDao.findAll((Specification<PokemonBrand>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (!StrUtils.isEmpty(alias)) {
                list.add(criteriaBuilder.like(root.get("alias"), "%"+alias+"%"));
            }
            return criteriaBuilder.and(list.toArray(new Predicate[0]));
        }, PageRequest.of(page.getPageNumber(),page.getPageSize(), Sort.by(Sort.Direction.DESC, "updatedate")));
    }

    @Override
    public PokemonBrand findBrandByAlias(String alias) {
        return brandDao.findByAliasEquals(alias);
    }

    @Override
    public PokemonBrand findBrandById(Long id) {
        return brandDao.findById(id).get();
    }

    @Override
    public long addBrand(PokemonBrand pokemonBrand) {
        return brandDao.saveAndFlush(pokemonBrand).getId();
    }

    @Override
    public void updateBrand(PokemonBrand pokemonBrand) {
        brandDao.saveAndFlush(pokemonBrand);
    }
}
