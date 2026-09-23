package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(length = 32, unique = true, nullable = false)
    private String username;

    @Column(length = 32, nullable = false)
    private String password;

    @Column(name = "disabled")
    private int disabled;

    @ManyToOne
    @JoinColumn(name="role_id")
    private Role role;

    @Column(length = 100)
    private String remark;

}
