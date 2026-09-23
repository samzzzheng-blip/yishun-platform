package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "smart_audit")
public class SmartAudit {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(nullable = false)
    private String frontUrl;

    @Column(nullable = false)
    private String backUrl;

    @Column()
    private String edge;

    @Column()
    private String corner;

    @Column()
    private String center;

    @Column()
    private String surface;

    @Column()
    private String score;

    @Column()
    private String serial;

    @Column()
    private String subserial;

    @Column()
    private String name;

    @Column()
    private String alias;

    @Column()
    private String code;

    @Column()
    private String attribute;

    @Column()
    private String rarity;

    @Column()
    private int status;

    @Column()
    private Long scoreId;

    @Column(length = 20)
    private long createdate;

    @Column(length = 20)
    private long updatedate;
}
