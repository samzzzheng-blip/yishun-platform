package com.kiss.yishun.dao;

import com.kiss.yishun.entity.PokemonBrand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PokemonBrandDao extends JpaRepository<PokemonBrand, Long>, JpaSpecificationExecutor<PokemonBrand> {
    PokemonBrand findByAliasEquals(String alias);
    Page<PokemonBrand> findAllByAliasLikeOrderByUpdatedateDesc(String alias, Pageable page);
}
