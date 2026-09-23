package com.techtron.onebook.module.app.service.exchangelog;

import java.util.*;
import jakarta.validation.*;
import com.techtron.onebook.module.app.controller.admin.exchangelog.vo.*;
import com.techtron.onebook.module.app.dal.dataobject.exchangelog.ExchangeLogDO;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.pojo.PageParam;

/**
 * 兑换记录 Service 接口
 *
 * @author 超级管理员
 */
public interface ExchangeLogService {

    /**
     * 创建兑换记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createExchangeLog(@Valid ExchangeLogSaveReqVO createReqVO);

    /**
     * 更新兑换记录
     *
     * @param updateReqVO 更新信息
     */
    void updateExchangeLog(@Valid ExchangeLogSaveReqVO updateReqVO);

    /**
     * 删除兑换记录
     *
     * @param id 编号
     */
    void deleteExchangeLog(Long id);

    /**
    * 批量删除兑换记录
    *
    * @param ids 编号
    */
    void deleteExchangeLogListByIds(List<Long> ids);

    /**
     * 获得兑换记录
     *
     * @param id 编号
     * @return 兑换记录
     */
    ExchangeLogDO getExchangeLog(Long id);

    /**
     * 获得兑换记录分页
     *
     * @param pageReqVO 分页查询
     * @return 兑换记录分页
     */
    PageResult<ExchangeLogDO> getExchangeLogPage(ExchangeLogPageReqVO pageReqVO);

}