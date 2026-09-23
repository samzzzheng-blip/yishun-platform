package com.techtron.onebook.module.statistics.dal.mysql.trade;

import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import com.techtron.onebook.module.statistics.service.trade.bo.AfterSaleSummaryRespBO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 售后订单的统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface AfterSaleStatisticsMapper extends BaseMapperX<TradeStatisticsDO> {

    AfterSaleSummaryRespBO selectSummaryByRefundTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                            @Param("endTime") LocalDateTime endTime);

    Long selectCountByStatus(@Param("status") Integer status);

}
