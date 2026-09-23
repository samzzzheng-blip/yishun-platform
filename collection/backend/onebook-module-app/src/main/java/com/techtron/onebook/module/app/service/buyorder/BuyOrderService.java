package com.techtron.onebook.module.app.service.buyorder;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderCancelVO;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderRespVO;
import com.techtron.onebook.module.app.controller.app.buyorder.vo.BuyOrderSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.buyorder.BuyOrderDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 批量交易买单 Service 接口
 *
 * @author 超级管理员
 */
public interface BuyOrderService {

    /**
     * 创建批量交易买单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBuyOrder(@Valid BuyOrderSaveReqVO createReqVO);

    /**
     * 更新批量交易买单
     *
     * @param updateReqVO 更新信息
     */
    void updateBuyOrder(@Valid BuyOrderSaveReqVO updateReqVO);

    /**
     * 删除批量交易买单
     *
     * @param id 编号
     */
    void deleteBuyOrder(Long id);

    /**
    * 批量删除批量交易买单
    *
    * @param ids 编号
    */
    void deleteBuyOrderListByIds(List<Long> ids);

    /**
     * 获得批量交易买单
     *
     * @param id 编号
     * @return 批量交易买单
     */
    BuyOrderDO getBuyOrder(Long id);

    void updateOrderPaid(Long merchantOrderId, Long buyOrderId);

    PageResult<BuyOrderRespVO> getBuyOrderPage(BuyOrderPageReqVO pageReqVO);

    void cancel(BuyOrderCancelVO reqVO);

}