package com.techtron.onebook.module.app.service.exchange;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.exchange.vo.ExchangePageReqVO;
import com.techtron.onebook.module.app.controller.admin.exchange.vo.ExchangeRespVO;
import com.techtron.onebook.module.app.controller.admin.exchange.vo.ExchangeSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.exchange.ExchangeDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 兑换品 Service 接口
 *
 * @author 超级管理员
 */
public interface ExchangeService {

    /**
     * 创建兑换品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createExchange(@Valid ExchangeSaveReqVO createReqVO);

    /**
     * 更新兑换品
     *
     * @param updateReqVO 更新信息
     */
    void updateExchange(@Valid ExchangeSaveReqVO updateReqVO);

    /**
     * 删除兑换品
     *
     * @param id 编号
     */
    void deleteExchange(Long id);

    /**
    * 批量删除兑换品
    *
    * @param ids 编号
    */
    void deleteExchangeListByIds(List<Long> ids);

    /**
     * 获得兑换品
     *
     * @param id 编号
     * @return 兑换品
     */
    ExchangeRespVO getExchange(Long id);

    /**
     * 获得兑换品分页
     *
     * @param pageReqVO 分页查询
     * @return 兑换品分页
     */
    PageResult<ExchangeDO> getExchangePage(ExchangePageReqVO pageReqVO);

    List<ExchangeDO> getExchangeList();

}