package com.techtron.onebook.module.app.service.yikoujia;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaPageReqVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaRespVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaSaveReqVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaImportReqVO;
import com.techtron.onebook.module.app.controller.admin.yikoujia.vo.YikoujiaImportRespVO;
import com.techtron.onebook.module.app.controller.app.yikoujia.vo.AppYikoujiaSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.service.yikoujia.bo.GoofishAuctionBO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 一口价 Service 接口
 *
 * @author 超级管理员
 */
public interface YikoujiaService {
    void updateImageOrder(Long id, List<String> original, List<String> reordered);
    com.fasterxml.jackson.databind.JsonNode requestGoofishProductDetail(String productId);

    /**
     * 创建一口价
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    void createYikoujia(@Valid AppYikoujiaSaveReqVO createReqVO);


    /**
     * 获得一口价分页
     *
     * @param pageReqVO 分页查询
     * @return 一口价分页
     */
//    PageResult<YikoujiaRespVO> getYikoujiaPage(YikoujiaPageReqVO pageReqVO);

    /**
     * 更新一口价
     *
     * @param updateReqVO 更新信息
     */
    void updateYikoujia(@Valid YikoujiaSaveReqVO updateReqVO);

    /**
     * 用户下架自己正在出售的一口价商品，并恢复对应藏品的可操作库存。
     *
     * @param id 一口价商品编号
     * @param userId 当前用户编号
     */
    void delistYikoujia(Long id, Long userId);

    /**
     * 为藏品取回准备库存：对正在一口价挂售的藏品先安全下架。
     *
     * <p>方法会锁定挂售单和藏品，并拦截存在待处理付款单的商品。</p>
     *
     * @param collectionIds 待取回的藏品编号
     * @param userId 当前用户编号
     */
    void delistCollectionsForGetback(List<Long> collectionIds, Long userId);

    /**
     * 获得一口价
     *
     * @param id 编号
     * @return 一口价
     */
    YikoujiaDO getYikoujia(Long id);

    /**
     * 获得一口价分页
     *
     * @param pageReqVO 分页查询
     * @return 一口价分页
     */
    PageResult<YikoujiaDO> getYikoujiaPage(YikoujiaPageReqVO pageReqVO);

    PageResult<YikoujiaRespVO> getYikoujiaPage1(YikoujiaPageReqVO pageReqVO);

    int executeUpdate();

    /**
     * 校验闲管家推送签名，避免伪造回调改变商品状态。
     */
    boolean verifyGoofishCallback(String appId, long timestamp, String sign, String rawBody);

    /**
     * 根据闲管家真实商品详情同步本地商品状态。
     */
    void syncGoofishProduct(String productId);

    /**
     * 根据闲管家订单详情定位商品并同步本地商品状态。
     */
    void syncGoofishOrder(String orderNo, String productId);

    void createYikoujiaByAdmin(@Valid YikoujiaSaveReqVO createReqVO);

    void test();

    void downProduct(String productId);

    List<YikoujiaRespVO> getMyYikoujiaList(YikoujiaPageReqVO pageReqVO);

    YikoujiaImportRespVO previewGoofishProduct(String source);

    PageResult<YikoujiaImportRespVO> getGoofishProductPage(Integer pageNo, Integer pageSize,
                                                            Integer productStatus, String source);

    Long importGoofishProduct(@Valid YikoujiaImportReqVO importReqVO);

    GoofishAuctionBO getGoofishAuction(String productId);

    GoofishAuctionBO getGoofishAuctionBySource(String source);

}
