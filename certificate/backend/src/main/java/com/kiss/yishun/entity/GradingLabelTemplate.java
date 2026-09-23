package com.kiss.yishun.entity;
import lombok.Data;
import javax.persistence.*;
@Data @Entity @Table(name="grading_label_template")
public class GradingLabelTemplate {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(nullable=false,length=80) private String name;
    @Lob private String fieldsJson;
    private boolean archived;
    private String updatedBy;
    private long updatedAt;
}
