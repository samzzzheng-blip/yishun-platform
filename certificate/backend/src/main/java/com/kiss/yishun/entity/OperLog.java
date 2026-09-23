package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "oper_log")
public class OperLog {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(length = 100, nullable = false)
    private String operator;

    @Column(length = 200, nullable = false)
    private String message;

    @Column(length = 32)
    private String ip;
}
