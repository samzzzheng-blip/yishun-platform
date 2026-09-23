package com.techtron.onebook.module.app.service.ads;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.ads.vo.AdsPageReqVO;
import com.techtron.onebook.module.app.controller.admin.ads.vo.AdsSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.ads.AdsDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 广告 Service 接口
 *
 * @author 超级管理员
 */
public interface AdsService {

    /**
     * 创建广告
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAds(@Valid AdsSaveReqVO createReqVO);

    /**
     * 更新广告
     *
     * @param updateReqVO 更新信息
     */
    void updateAds(@Valid AdsSaveReqVO updateReqVO);

    /**
     * 删除广告
     *
     * @param id 编号
     */
    void deleteAds(Long id);

    /**
    * 批量删除广告
    *
    * @param ids 编号
    */
    void deleteAdsListByIds(List<Long> ids);

    /**
     * 获得广告
     *
     * @param id 编号
     * @return 广告
     */
    AdsDO getAds(Long id);

    /**
     * 获得广告分页
     *
     * @param pageReqVO 分页查询
     * @return 广告分页
     */
    PageResult<AdsDO> getAdsPage(AdsPageReqVO pageReqVO);

    List<AdsDO> getAdsList();

}