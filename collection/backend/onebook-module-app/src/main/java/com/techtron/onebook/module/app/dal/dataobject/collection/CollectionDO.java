package com.techtron.onebook.module.app.dal.dataobject.collection;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.util.List;

/**
 * 藏品登记 DO
 *
 * @author 超级管理员
 */
@TableName(value="app_collection", autoResultMap = true)
@KeySequence("app_collection_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDO extends BaseDO {

    /**
     * 藏品编号
     */
    @TableId
    private Long id;
    /**
     * 品名
     */
    private String name;
    /**
     * 分类名
     */
    private String categoryName;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 分类id
     */
    private Long categoryId;
    /**
     * 图片地址
     */
    private String picUrl;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 数量
     */
    private Integer stock;

    private Integer realStock;

    private Integer tradeStatus;

    private Integer getbackStatus;

    private String creatorUserName;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> picUrls;


}
