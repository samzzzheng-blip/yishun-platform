package com.techtron.onebook.module.app.dal.dataobject.exchange;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 兑换品 DO
 *
 * @author 超级管理员
 */
@TableName("app_exchange")
@KeySequence("app_exchange_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 图片
     */
    private String picUrl;
    /**
     * 兑换品名称
     */
    private String name;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 库存
     */
    private Integer stock;

    private Integer amount;

    private Integer ordinalPosition;



}
