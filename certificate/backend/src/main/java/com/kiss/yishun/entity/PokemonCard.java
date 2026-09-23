package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "pokemon_card")
public class PokemonCard {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column()
    private String code;

    @Column()
    private String name;

    @Column()
    private String alias;

    @Column()
    private String attribute;

    @Column()
    private String rarity;

    @Column()
    private String operator;

    @Column()
    private long createdate;

    @Column()
    private long updatedate;
}
