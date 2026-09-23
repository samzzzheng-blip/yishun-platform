package com.techtron.onebook.module.app.dal.dataobject.collectiontransfer;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;

/**
 * 转移记录 DO
 *
 * @author 超级管理员
 */
@TableName("app_collection_transfer")
@KeySequence("app_collection_transfer_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionTransferDO extends BaseDO {

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
     * 被转移用户id
     */
    private Long toUserId;


}
