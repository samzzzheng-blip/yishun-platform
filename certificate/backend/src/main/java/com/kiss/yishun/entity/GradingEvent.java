package com.kiss.yishun.entity;
import lombok.Data;
import javax.persistence.*;
@Data @Entity @Table(name="grading_event")
public class GradingEvent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long jobId;
    @Column(length=100) private String actor;
    @Column(length=40) private String action;
    @Column(length=32) private String stage;
    private long createdAt;
}

