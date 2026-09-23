package com.techtron.onebook.module.app.dal.dataobject.stoneexchange;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;

/**
 * 能量石兑换 DO
 *
 * @author 超级管理员
 */
@TableName("app_stone_exchange")
@KeySequence("app_stone_exchange_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoneExchangeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户id
     */
    private Long userId;


}