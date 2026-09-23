package com.kiss.yishun.entity;
import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name="grading_job", uniqueConstraints=@UniqueConstraint(name="uk_grading_batch_number",columnNames={"batch","batchNumber"}), indexes={@Index(name="idx_grading_status", columnList="stage"), @Index(name="idx_grading_batch", columnList="batch")})
public class GradingJob {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(nullable=false,unique=true,length=48) private String jobNumber;
    @Column(nullable=false,length=32) private String stage;
    @Column(length=100) private String batch;
    private Long batchNumber;
    private Long deletedAt;
    @Column(length=100) private String cardName;
    @Column(length=100) private String series;
    @Column(length=20) private String cardYear;
    @Column(length=40) private String language;
    @Column(length=100) private String cardNumber;
    @Column(length=100) private String certNumber;
    @Column(unique=true) private Long rateId;
    @Column(length=200) private String frontPhoto;
    @Column(length=200) private String backPhoto;
    @Column(length=200) private String finishedPhoto;
    @Column(length=10) private String surface;
    @Column(length=10) private String center;
    @Column(length=10) private String edge;
    @Column(length=10) private String corner;
    @Column(length=10) private String score;
    @Column(length=1000) private String notes;
    @Column(length=100) private String createdBy;
    @Column(length=100) private String gradedBy;
    @Column(length=100) private String confirmedBy;
    @Lob private String templateSchema;
    @Lob private String templateValues;
    @Lob private String labelText;
    private long createdAt;
    private long updatedAt;
}
