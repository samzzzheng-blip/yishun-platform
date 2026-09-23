package com.kiss.yishun.service;

import com.kiss.yishun.entity.PokemonBrand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface SmartBrandService {

    Page<PokemonBrand> findAllBrand(String alias, PageRequest page);

    PokemonBrand findBrandByAlias(String alias);

    PokemonBrand findBrandById(Long id);

    long addBrand(PokemonBrand pokemonBrand);

    void updateBrand(PokemonBrand pokemonBrand);
}
