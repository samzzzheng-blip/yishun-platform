package com.techtron.onebook.module.app.dal.dataobject.sellorder;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 批量交易卖单 DO
 *
 * @author 超级管理员
 */
@TableName("app_sell_order")
@KeySequence("app_sell_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellOrderDO extends BaseDO {

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
     * 藏品分类id
     */
    private Long categoryId;

    private Long collectionId;
    /**
     * 剩余未成交数量
     */
    private Integer amount;
    /**
     * 交易数量
     */
    private Integer dealAmount;
    /**
     * 价格，单位：分
     */
    private Integer price;


}
