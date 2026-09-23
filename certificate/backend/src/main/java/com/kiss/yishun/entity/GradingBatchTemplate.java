package com.kiss.yishun.entity;
import lombok.Data;
import javax.persistence.*;
@Data @Entity @Table(name="grading_batch_template")
public class GradingBatchTemplate {
    @Id @Column(length=100) private String batch;
    @Version private Long version;
    @Lob private String schemaJson;
    private String updatedBy;
    private long updatedAt;
}
