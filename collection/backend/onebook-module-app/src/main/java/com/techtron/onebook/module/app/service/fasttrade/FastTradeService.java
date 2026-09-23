package com.techtron.onebook.module.app.service.fasttrade;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradePageReqVO;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradeRespVO;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradeSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.fasttrade.FastTradeDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 快速变现 Service 接口
 *
 * @author 超级管理员
 */
public interface FastTradeService {

    /**
     * 创建快速变现
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    void createFastTrade(@Valid FastTradeSaveReqVO createReqVO);

    /**
     * 更新快速变现
     *
     * @param updateReqVO 更新信息
     */
    void updateFastTrade(@Valid FastTradeSaveReqVO updateReqVO);

    /**
     * 删除快速变现
     *
     * @param id 编号
     */
    void deleteFastTrade(Long id);

    /**
    * 批量删除快速变现
    *
    * @param ids 编号
    */
    void deleteFastTradeListByIds(List<Long> ids);

    /**
     * 获得快速变现
     *
     * @param id 编号
     * @return 快速变现
     */
    FastTradeDO getFastTrade(Long id);

    /**
     * 获得快速变现分页
     *
     * @param pageReqVO 分页查询
     * @return 快速变现分页
     */
    PageResult<FastTradeRespVO> getFastTradePage(FastTradePageReqVO pageReqVO);

}