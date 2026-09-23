package com.techtron.onebook.module.app.dal.dataobject.auction;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

@TableName("app_auction_bid")
@KeySequence("app_auction_bid_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionBidDO extends BaseDO {
    @TableId private Long id;
    private Long auctionId;
    private Long bidderId;
    private Integer amount;
    /** 1-leading 2-outbid 3-winner */
    private Integer status;
    private String requestId;
}
