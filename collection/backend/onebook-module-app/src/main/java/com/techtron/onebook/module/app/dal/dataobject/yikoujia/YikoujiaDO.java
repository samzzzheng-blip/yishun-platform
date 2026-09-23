package com.techtron.onebook.module.app.dal.dataobject.yikoujia;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 一口价 DO
 *
 * @author 超级管理员
 */
@TableName(value="app_yikoujia", autoResultMap = true)
@KeySequence("app_yikoujia_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YikoujiaDO extends BaseDO {

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
     * 价格，单位：分
     */
    private Integer price;
    /**
     * 状态（0待交易 1待结算 2已结算）
     *
     * 枚举 {@link TODO collection_trade_status 对应的类}
     */
    private Integer status;

    private Long collectionId;
    /**
     * 数量
     */
    private Integer amount;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> picUrl;

    private String name;

    private String productId;

    private String introduction;

    /**
     * 实际成交渠道：MINIAPP 小程序、GOOFISH 闲鱼、CONFLICT 双端冲突。
     */
    private String saleChannel;

    /** 成交时间 */
    private LocalDateTime soldAt;

    /** 闲鱼最后一次返回的商品状态 */
    private Integer goofishStatus;

    /** 最后一次闲鱼同步时间 */
    private LocalDateTime lastSyncTime;

    /** 同步结果或冲突说明 */
    private String syncRemark;


}
