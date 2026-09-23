package com.kiss.yishun.entity;
import lombok.Data;
import javax.persistence.*;
@Data @Entity
@Table(name="grading_batch_sequence")
public class GradingBatchSequence {
    @Id @Column(length=100) private String batch;
    private long lastNumber;
    @Column(length=100) private String createdBy;
    private long createdAt;
    @Column(unique=true,length=100) private String requestId;
    @Column(length=18) private String startNumber;
}
