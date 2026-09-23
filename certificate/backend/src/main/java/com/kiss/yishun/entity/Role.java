package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(length = 32, unique = true, nullable = false)
    private String code;

    @Column(length = 32, unique = true, nullable = false)
    private String name;

    @Column
    private int level;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_permission", joinColumns = {@JoinColumn(name = "role_id")},
            inverseJoinColumns = {@JoinColumn(name = "permission_id")})
    private List<Permission> permissionList = new ArrayList<>();
}
