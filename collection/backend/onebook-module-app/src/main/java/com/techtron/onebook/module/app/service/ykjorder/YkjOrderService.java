package com.techtron.onebook.module.app.service.ykjorder;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.app.ykjorder.vo.YkjOrderPageReqVO;
import com.techtron.onebook.module.app.controller.app.ykjorder.vo.YkjOrderSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.ykjorder.YkjOrderDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 一口价买单 Service 接口
 *
 * @author 超级管理员
 */
public interface YkjOrderService {

    /**
     * 创建一口价买单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createYkjOrder(@Valid YkjOrderSaveReqVO createReqVO);

    /**
     * 更新一口价买单
     *
     * @param updateReqVO 更新信息
     */
    void updateYkjOrder(@Valid YkjOrderSaveReqVO updateReqVO);

    /**
     * 删除一口价买单
     *
     * @param id 编号
     */
    void deleteYkjOrder(Long id);

    /**
    * 批量删除一口价买单
    *
    * @param ids 编号
    */
    void deleteYkjOrderListByIds(List<Long> ids);

    /**
     * 获得一口价买单
     *
     * @param id 编号
     * @return 一口价买单
     */
    YkjOrderDO getYkjOrder(Long id);

    /**
     * 获得一口价买单分页
     *
     * @param pageReqVO 分页查询
     * @return 一口价买单分页
     */
    PageResult<YkjOrderDO> getYkjOrderPage(YkjOrderPageReqVO pageReqVO);

    void updateOrderPaid(Long merchantOrderId, Long buyOrderId);

}