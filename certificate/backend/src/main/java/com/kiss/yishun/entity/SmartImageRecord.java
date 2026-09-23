package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "smart_image_record")
public class SmartImageRecord {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private int type;

    @Column(nullable = false)
    private String name;

    @Column(length = 20)
    private long createdate;

    @Column(length = 20)
    private long updatedate;
}
