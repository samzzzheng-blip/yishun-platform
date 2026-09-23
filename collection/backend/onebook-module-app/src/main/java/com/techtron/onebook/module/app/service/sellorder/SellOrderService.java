package com.techtron.onebook.module.app.service.sellorder;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderCancelVO;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderRespVO;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.SellOrderSaveReqVO;
import com.techtron.onebook.module.app.controller.app.sellorder.vo.UserTradeInfoRespVO;
import com.techtron.onebook.module.app.dal.dataobject.sellorder.SellOrderDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 批量交易卖单 Service 接口
 *
 * @author 超级管理员
 */
public interface SellOrderService {

    /**
     * 创建批量交易卖单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSellOrder(@Valid SellOrderSaveReqVO createReqVO);

    /**
     * 更新批量交易卖单
     *
     * @param updateReqVO 更新信息
     */
    void updateSellOrder(@Valid SellOrderSaveReqVO updateReqVO);

    /**
     * 删除批量交易卖单
     *
     * @param id 编号
     */
    void deleteSellOrder(Long id);

    /**
    * 批量删除批量交易卖单
    *
    * @param ids 编号
    */
    void deleteSellOrderListByIds(List<Long> ids);

    /**
     * 获得批量交易卖单
     *
     * @param id 编号
     * @return 批量交易卖单
     */
    SellOrderDO getSellOrder(Long id);

    UserTradeInfoRespVO getUserTradeInfo(Long userId);

    PageResult<SellOrderRespVO> getSellOrderPage(SellOrderPageReqVO pageReqVO);

    void cancel(SellOrderCancelVO reqVO);

}