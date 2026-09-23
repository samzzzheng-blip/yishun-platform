package com.kiss.yishun.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "smart_score")
public class SmartScore {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(length = 11)
    private long id;

    @Column(nullable = false)
    private String frontUrl;

    @Column(nullable = false)
    private String backUrl;

//    @Column()
//    private Long userId;

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
    private String certNumber;

    @Column()
    private String alias;

    @Column()
    private long cardId;

    @Column()
    private Long auditId;

    @Column()
    private int status;

    @Column(length = 20)
    private long createdate;

    @Column(length = 20)
    private long updatedate;

    @ManyToOne
    @JoinColumn(name="user_id")
    @JsonIgnoreProperties({"pwd", "nick", "head", "disabled","createdate","updatedate"})
    private SmartUser user;
}
