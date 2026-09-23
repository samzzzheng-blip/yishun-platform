package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "operation")
public class Operation {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(length = 32, unique = true, nullable = false)
    private String name;

    @Column(length = 32, unique = true, nullable = false)
    private String code;

}
