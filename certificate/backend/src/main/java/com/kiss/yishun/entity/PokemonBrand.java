package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "pokemon_brand")
public class PokemonBrand {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column()
    private String title;

    @Column()
    private String brand;

    @Column(nullable = false)
    private String alias;

    @Column()
    private String operator;

    @Column()
    private long createdate;

    @Column()
    private long updatedate;
}
