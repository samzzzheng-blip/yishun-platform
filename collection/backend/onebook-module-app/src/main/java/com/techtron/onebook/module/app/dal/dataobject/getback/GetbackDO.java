package com.techtron.onebook.module.app.dal.dataobject.getback;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 取回 DO
 *
 * @author 超级管理员
 */
@TableName("app_getback")
@KeySequence("app_getback_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetbackDO extends BaseDO {

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
     * 状态
     *
     * 枚举 {@link TODO collection_deliver_status 对应的类}
     */
    private Integer status;
    /**
     * 收件人名称
     */
    private String receiverName;
    /**
     * 收件人手机
     */
    private String receiverMobile;
    /**
     * 收件人地区
     */
    private String receiverAreaName;
    /**
     * 收件人详细地址
     */
    private String receiverDetailAddress;

    private String deliverCode;

    private String expressCompany;


}
