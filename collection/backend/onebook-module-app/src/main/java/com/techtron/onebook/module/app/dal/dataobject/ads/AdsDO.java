package com.techtron.onebook.module.app.dal.dataobject.ads;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;

/**
 * 广告 DO
 *
 * @author 超级管理员
 */
@TableName("app_ads")
@KeySequence("app_ads_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdsDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 图片地址
     */
    private String picUrl;
    /**
     * 状态
     */
    private Integer status;

    /** 广告跳转的一口价商品编号，空表示不跳转 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long targetProductId;

    @TableField(exist = false)
    private String targetProductName;


}