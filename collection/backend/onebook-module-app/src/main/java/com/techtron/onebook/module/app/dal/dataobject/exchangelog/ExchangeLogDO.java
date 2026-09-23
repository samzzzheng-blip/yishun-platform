package com.techtron.onebook.module.app.dal.dataobject.exchangelog;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 兑换记录 DO
 *
 * @author 超级管理员
 */
@TableName("app_exchange_log")
@KeySequence("app_exchange_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeLogDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 兑换品id
     */
    private Long exchangeId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 兑换品名称
     */
    private String exchangeName;
    /**
     * 兑换数量
     */
    private Integer amount;
    /**
     * 状态
     *
     * 枚举 {@link TODO exchange_status 对应的类}
     */
    private Integer status;
    /**
     * 手机号
     */
    private String mobile;

    private String answer;

    private String deliverCode;


}
