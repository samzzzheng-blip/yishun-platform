package com.techtron.onebook.module.app.service.dealorder;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.dealorder.vo.DealOrderPageReqVO;
import com.techtron.onebook.module.app.controller.admin.dealorder.vo.DealOrderRespVO;
import com.techtron.onebook.module.app.controller.admin.dealorder.vo.DealOrderSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.dealorder.DealOrderDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 批量交易成交 Service 接口
 *
 * @author 超级管理员
 */
public interface DealOrderService {

    /**
     * 创建批量交易成交
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDealOrder(@Valid DealOrderSaveReqVO createReqVO);

    /**
     * 更新批量交易成交
     *
     * @param updateReqVO 更新信息
     */
    void updateDealOrder(@Valid DealOrderSaveReqVO updateReqVO);

    /**
     * 删除批量交易成交
     *
     * @param id 编号
     */
    void deleteDealOrder(Long id);

    /**
    * 批量删除批量交易成交
    *
    * @param ids 编号
    */
    void deleteDealOrderListByIds(List<Long> ids);

    /**
     * 获得批量交易成交
     *
     * @param id 编号
     * @return 批量交易成交
     */
    DealOrderDO getDealOrder(Long id);

    /**
     * 获得批量交易成交分页
     *
     * @param pageReqVO 分页查询
     * @return 批量交易成交分页
     */
    PageResult<DealOrderDO> getDealOrderPage(DealOrderPageReqVO pageReqVO);

    List<DealOrderRespVO> getDealOrderList(Long categoryId);

}