package com.techtron.onebook.module.app.dal.dataobject.ykjorder;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 一口价买单 DO
 *
 * @author 超级管理员
 */
@TableName("app_ykj_order")
@KeySequence("app_ykj_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YkjOrderDO extends BaseDO {

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
     * 一口价商品id
     */
    private Long ykjId;
    /**
     * 支付状态（0未支付，1已支付）
     */
    private Integer status;
    /**
     * 支付订单编号
     */
    private Long payOrderId;

    private Integer price;

    /** WAREHOUSE 入库；SHIP 直接寄出。旧订单为空时按入库处理。 */
    private String fulfillmentType;
    private String receiverName;
    private String receiverMobile;
    private String receiverAreaName;
    private String receiverDetailAddress;
    private Long getbackId;


}
