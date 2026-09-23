package com.techtron.onebook.module.app.dal.dataobject.storageplan;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;

@TableName("app_storage_plan")
@KeySequence("app_storage_plan_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoragePlanDO extends BaseDO {

    @TableId
    private Long id;

    private Integer minCount;

    private Integer maxCount;

    private Integer monthlyPrice;

    private Integer yearlyPrice;

}