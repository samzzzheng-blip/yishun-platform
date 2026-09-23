package com.techtron.onebook.module.app.dal.dataobject.category;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 藏品分类 DO
 *
 * @author 超级管理员
 */
@TableName("app_category")
@KeySequence("app_category_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionCategoryDO extends BaseDO {

    /**
     * 分类编号
     */
    @TableId
    private Long id;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 用户id
     */
    private Long userId;

    private Long copyId;

    private String picUrl;

    private Integer exchangeRate;


}
