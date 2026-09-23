package com.techtron.onebook.module.app.dal.dataobject.auction;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;

@TableName("app_auction_settlement")
@KeySequence("app_auction_settlement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionSettlementDO extends BaseDO {
    @TableId private Long id;
    private Long auctionId;
    private Long sellerId;
    private Long buyerId;
    private Long bidId;
    private Integer grossAmount;
    /** 成交确认时锁定的手续费比例，单位：百分比 */
    private Integer feeRate;
    private Integer feeAmount;
    private Integer sellerIncome;
    /** 1-已入钱包 2-入账失败 */
    private Integer status;
    private Long confirmUserId;
    private String confirmRemark;
    private String failReason;
    private Integer retryCount;
    private LocalDateTime settledTime;
}
