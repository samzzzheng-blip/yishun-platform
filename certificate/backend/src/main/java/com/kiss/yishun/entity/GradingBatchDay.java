package com.kiss.yishun.entity;
import lombok.Data;
import javax.persistence.*;
@Data @Entity
public class GradingBatchDay {
    @Id @Column(length=120) private String dayActor;
    private long lastNumber;
}
