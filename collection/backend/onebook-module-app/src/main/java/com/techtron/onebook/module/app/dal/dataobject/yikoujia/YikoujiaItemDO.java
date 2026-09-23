package com.techtron.onebook.module.app.dal.dataobject.yikoujia;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 一口价 DO
 *
 * @author 超级管理员
 */
@TableName("app_yikoujia_item")
@KeySequence("app_yikoujia_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YikoujiaItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 订单id
     */
    private Long ykjId;
    /**
     * 藏品id
     */
    private Long collectionId;
    /**
     * 数量
     */
    private Integer amount;

    private String picUrl;


}
