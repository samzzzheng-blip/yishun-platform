package com.techtron.onebook.module.app.dal.dataobject.exchange;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

@TableName("app_exchange_question")
@KeySequence("app_exchange_question_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeQuestionDO extends BaseDO {
    @TableId
    private Long id;

    private Long exchangeId;

    private String question;
}
