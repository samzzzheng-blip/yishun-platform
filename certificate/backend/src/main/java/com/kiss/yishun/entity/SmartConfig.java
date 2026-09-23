package com.kiss.yishun.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "smart_config")
public class SmartConfig {

    @Column(unique = true, nullable = false)
    @Id
    private String name;

    @Column()
    private String value;

}
