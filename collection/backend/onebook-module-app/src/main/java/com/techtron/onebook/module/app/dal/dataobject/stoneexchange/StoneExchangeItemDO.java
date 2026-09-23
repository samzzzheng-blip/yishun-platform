package com.techtron.onebook.module.app.dal.dataobject.stoneexchange;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 能量石兑换 DO
 *
 * @author 超级管理员
 */
@TableName("app_stone_exchange_item")
@KeySequence("app_stone_exchange_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoneExchangeItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 订单id
     */
    private Long stoneExchangeId;

    /**
     * 藏品id
     */
    private Long collectionId;

    private String picUrl;


}
