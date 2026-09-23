package com.techtron.onebook.module.app.dal.dataobject.stonerecord;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;

@TableName("app_stone_record")
@KeySequence("app_stone_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoneRecordDO extends BaseDO {

    @TableId
    private Long id;

    private Long userId;

    private Integer amount;

    @TableField("`type`")
    private Integer type;

    private Integer balance;
    /** 兑换时的商品快照，历史无关联记录保持为空。 */
    private Long exchangeLogId;
    private String exchangeName;
    private Integer exchangeQuantity;

}
