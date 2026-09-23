package com.techtron.onebook.module.app.dal.dataobject.auction;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.techtron.onebook.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@TableName(value = "app_auction", autoResultMap = true)
@KeySequence("app_auction_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionDO extends BaseDO {
    @TableId private Long id;
    private Long sellerId;
    private Long collectionId;
    private String collectionName;
    private String categoryName;
    private String picUrl;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> picUrls;
    /** 管理员维护的拍卖展示图，与原藏品图片分离；第一张为封面。 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> displayPicUrls;
    private Integer amount;
    private Integer startPrice;
    private Integer minIncrement;
    private Integer currentPrice;
    private Long highestBidId;
    private Long highestBidderId;
    private Integer bidCount;
    private String goofishProductId;
    /** 闲管家product_id，与闲鱼item_id分别存储，禁止混用。 */
    private String goofishManagedProductId;
    private String goofishDetail;
    private String goofishUrl;
    private String shareText;
    private String syncError;
    private LocalDateTime lastSyncTime;
    /** 0-pending manual publish 1-active 2-pending sale confirmation 3-wallet credited 4-unsold 5-cancelled 6-sync exception 7-review rejected */
    private Integer status;
    private LocalDateTime reviewTime;
    private Long reviewUserId;
    private String reviewRejectReason;
    /** 0-none 1-pending review 2-approved 3-rejected */
    private Integer delistStatus;
    private LocalDateTime delistApplyTime;
    private LocalDateTime delistAuditTime;
    private Long delistAuditUserId;
    private String delistRejectReason;
    private LocalDateTime endTime;
    private LocalDateTime settledTime;
}
