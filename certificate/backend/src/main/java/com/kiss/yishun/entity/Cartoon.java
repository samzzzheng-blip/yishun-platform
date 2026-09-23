package com.kiss.yishun.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.kiss.yishun.utils.TimestampConverter;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "cartoon")
public class Cartoon {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(length = 100, unique = true, nullable = false)
    private String certNumber;

    @Column(length = 100, nullable = false)
    private String roleName;

    @Column(length = 100, nullable = false)
    private String cartoonName;

    @Column(length = 50, nullable = false)
    private String itemType;

    @Column(length = 200, nullable = false)
    private String imgUrl;

    @Column(length = 100, nullable = false)
    private String author;

    @Column(length = 200, nullable = false)
    private String company;

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

    @Column(length = 500)
    private String remark;

    @Column(length = 100)
    private String operator;
}
