package com.techtron.onebook.module.app.service.brokerage.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提现合计 BO
 *
 * @author owen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerageWithdrawSummaryRespBO {

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 提现次数
     */
    private Integer count;
    /**
     * 提现金额
     */
    private Integer price;

}
