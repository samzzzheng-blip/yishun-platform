package com.techtron.onebook.module.app.service.stoneexchange;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.stoneexchange.vo.StoneAddReqVO;
import com.techtron.onebook.module.app.controller.admin.stoneexchange.vo.StoneExchangePageReqVO;
import com.techtron.onebook.module.app.controller.admin.stoneexchange.vo.StoneExchangeRespVO;
import com.techtron.onebook.module.app.controller.admin.stoneexchange.vo.StoneExchangeSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.stoneexchange.StoneExchangeDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 能量石兑换 Service 接口
 *
 * @author 超级管理员
 */
public interface StoneExchangeService {

    /**
     * 创建能量石兑换
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStoneExchange(@Valid StoneExchangeSaveReqVO createReqVO);

    /**
     * 更新能量石兑换
     *
     * @param updateReqVO 更新信息
     */
    void updateStoneExchange(@Valid StoneExchangeSaveReqVO updateReqVO);

    /**
     * 删除能量石兑换
     *
     * @param id 编号
     */
    void deleteStoneExchange(Long id);

    /**
    * 批量删除能量石兑换
    *
    * @param ids 编号
    */
    void deleteStoneExchangeListByIds(List<Long> ids);

    /**
     * 获得能量石兑换
     *
     * @param id 编号
     * @return 能量石兑换
     */
    StoneExchangeDO getStoneExchange(Long id);

    /**
     * 获得能量石兑换分页
     *
     * @param pageReqVO 分页查询
     * @return 能量石兑换分页
     */
    PageResult<StoneExchangeRespVO> getStoneExchangePage(StoneExchangePageReqVO pageReqVO);

    Boolean addStone(StoneAddReqVO reqVO);

    Integer batchExchangeByCategory(Long userId, Integer needStoneAmount);

}