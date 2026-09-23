package com.kiss.yishun.dao;

import com.kiss.yishun.entity.PokemonCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PokemonCardDao extends JpaRepository<PokemonCard, Long>, JpaSpecificationExecutor<PokemonCard> {
    PokemonCard findByAliasEqualsAndCodeEquals(String alias, String code);
    Page<PokemonCard> findAllCardByAliasLikeAndCodeLikeOrderByUpdatedateDesc(String alias, String code, Pageable page);
}
