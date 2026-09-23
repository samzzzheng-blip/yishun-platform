package com.techtron.onebook.module.app.dal.dataobject.fasttrade;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 快速变现 DO
 *
 * @author 超级管理员
 */
@TableName("app_fast_trade")
@KeySequence("app_fast_trade_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastTradeDO extends BaseDO {

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
     * 状态
     *
     * 枚举 {@link TODO collection_trade_status 对应的类}
     */
    private Integer status;

    private Integer price;


}
