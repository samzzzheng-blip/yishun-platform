package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "permission")
public class Permission {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(length = 32, unique = true, nullable = false)
    private String code;

    @Column(length = 32, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name="menu_id")
    private Menu menu;

    @ManyToOne
    @JoinColumn(name="operation_id")
    private Operation operation;

}
