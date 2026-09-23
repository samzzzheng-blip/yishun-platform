package com.kiss.yishun.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.kiss.yishun.utils.TimestampConverter;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "precious")
public class Precious {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(length = 100, unique = true, nullable = false)
    private String certNumber;

    @Column(length = 100, nullable = false)
    private String signer;

    @Column(length = 50, nullable = false)
    private String itemType;

    @Column(length = 200, nullable = false)
    private String imgUrl;

    @Column(length = 50)
    private String publishTime;

    @Column(length = 100)
    private String publishCity;

    @Column(length = 200)
    private String publishActivity;

    @Column(length = 100)
    private String publishSign;

    @Column(length = 200)
    private String evidenceImg;

    @Column(length = 200)
    private String evidenceVideo;

    @Column(length = 200)
    private String evidenceVideoImgUrl;

    @ExcelProperty(converter = TimestampConverter.class)
    @Column(length = 20)
    private long createdate;

    @ExcelProperty(converter = TimestampConverter.class)
    @Column(length = 20)
    private long updatedate;

    @Column(length = 50)
    private String score;

    @Column(length = 1)
    private int status; // 0-未上架 1-已上架

    @Column(length = 500)
    private String remark;

    @Column(length = 100)
    private String operator;
}
