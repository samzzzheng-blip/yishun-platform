package com.kiss.yishun.service;

import com.kiss.yishun.entity.PokemonCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface SmartCardService {

    Page<PokemonCard> findAllCard(String alias,String code,PageRequest page);

    PokemonCard findCardByAliasAndCode(String alias, String code);

    PokemonCard findCardById(Long id);

    long addCard(PokemonCard pokemonCard);

    void updateCard(PokemonCard pokemonCard);
}
