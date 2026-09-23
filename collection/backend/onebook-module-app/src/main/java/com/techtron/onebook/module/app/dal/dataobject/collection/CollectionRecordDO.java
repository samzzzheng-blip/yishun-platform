package com.techtron.onebook.module.app.dal.dataobject.collection;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 藏品变更记录 DO
 *
 * @author 超级管理员
 */
@TableName("app_collection_record")
@KeySequence("app_collection_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRecordDO extends BaseDO {

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

    /**
     * 数量
     */
    private Integer amount;

    /**
     * 类型：1-用户新增 2-管理员新增 3-删除 4-兑换能量石 5-买 6-卖 7-取回
     */
    private Integer type;

}