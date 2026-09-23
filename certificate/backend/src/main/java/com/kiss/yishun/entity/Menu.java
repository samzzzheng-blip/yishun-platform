package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "menu")
public class Menu {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(length = 32, unique = true, nullable = false)
    private String name;

    @Column(length = 80, unique = true, nullable = false)
    private String path;

    @Column(length = 11)
    private Long parentId;

    @Transient
    private List<Menu> children;

}
