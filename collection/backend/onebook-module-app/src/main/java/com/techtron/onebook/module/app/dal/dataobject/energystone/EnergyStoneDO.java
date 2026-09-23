package com.techtron.onebook.module.app.dal.dataobject.energystone;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;

/**
 * 能量石 DO
 *
 * @author 超级管理员
 */
@TableName("app_energy_stone")
@KeySequence("app_energy_stone_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyStoneDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 数量
     */
    private Integer amount;


}