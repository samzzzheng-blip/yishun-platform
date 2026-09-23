package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "smart_user")
public class SmartUser {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column()
    private String nick;

    @Column()
    private String head;

    @Column(nullable = false)
    private String pwd;


    @Column(nullable = false)
    private String phone;

    @Column(name = "disabled")
    private Integer disabled;

    @Column(length = 20)
    private long createdate;

    @Column(length = 20)
    private long updatedate;
}
