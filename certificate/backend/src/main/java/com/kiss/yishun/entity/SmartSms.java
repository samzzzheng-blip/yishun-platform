package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "smart_sms")
public class SmartSms {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(nullable = false)
    private String scene;

    @Column(nullable = false)
    private String phone;

    @Column()
    private String content;

    @Column()
    private String response;

    @Column()
    private String err;

    @Column()
    private int status;

    @Column()
    private String remark;

    @Column(length = 20)
    private long createdate;

    @Column(length = 20)
    private long updatedate;
}
