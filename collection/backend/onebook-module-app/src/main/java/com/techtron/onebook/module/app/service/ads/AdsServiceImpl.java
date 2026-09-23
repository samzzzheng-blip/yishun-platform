package com.techtron.onebook.module.app.service.ads;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.ads.vo.AdsPageReqVO;
import com.techtron.onebook.module.app.controller.admin.ads.vo.AdsSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.ads.AdsDO;
import com.techtron.onebook.module.app.dal.mysql.ads.AdsMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import com.techtron.onebook.framework.common.exception.ErrorCode;
import java.util.Objects;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.ADS_NOT_EXISTS;

/**
 * 广告 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class AdsServiceImpl implements AdsService {

    @Resource
    private AdsMapper adsMapper;

    @Resource
    private YikoujiaMapper yikoujiaMapper;

    private void validateTarget(Long productId, Long previousId) {
        if (productId == null || Objects.equals(productId, previousId)) return;
        var product = yikoujiaMapper.selectById(productId);
        if (product == null || !Objects.equals(product.getStatus(), 3)) {
            throw exception(new ErrorCode(2_001_010_010, "请选择在售的一口价商品"));
        }
    }

    private void fillTargetNames(List<AdsDO> ads) {
        var ids = ads.stream().map(AdsDO::getTargetProductId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return;
        var products = yikoujiaMapper.selectByIds(ids);
        Map<Long, String> names = products.stream().collect(Collectors.toMap(
                p -> p.getId(), p -> Objects.toString(p.getName(), "未命名商品")));
        ads.forEach(ad -> {
            if (ad.getTargetProductId() != null) {
                ad.setTargetProductName(names.getOrDefault(ad.getTargetProductId(), "商品已删除"));
            }
        });
    }

    @Override
    public Long createAds(AdsSaveReqVO createReqVO) {
        validateTarget(createReqVO.getTargetProductId(), null);
        // 插入
        AdsDO ads = BeanUtils.toBean(createReqVO, AdsDO.class);
        adsMapper.insert(ads);

        // 返回
        return ads.getId();
    }

    @Override
    public void updateAds(AdsSaveReqVO updateReqVO) {
        // 校验存在
        validateAdsExists(updateReqVO.getId());
        validateTarget(updateReqVO.getTargetProductId(), adsMapper.selectById(updateReqVO.getId()).getTargetProductId());
        // 更新
        AdsDO updateObj = BeanUtils.toBean(updateReqVO, AdsDO.class);
        adsMapper.updateById(updateObj);
    }

    @Override
    public void deleteAds(Long id) {
        // 校验存在
        validateAdsExists(id);
        // 删除
        adsMapper.deleteById(id);
    }

    @Override
        public void deleteAdsListByIds(List<Long> ids) {
        // 删除
        adsMapper.deleteByIds(ids);
        }


    private void validateAdsExists(Long id) {
        if (adsMapper.selectById(id) == null) {
            throw exception(ADS_NOT_EXISTS);
        }
    }

    @Override
    public AdsDO getAds(Long id) {
        AdsDO ads = adsMapper.selectById(id);
        if (ads != null) fillTargetNames(List.of(ads));
        return ads;
    }

    @Override
    public PageResult<AdsDO> getAdsPage(AdsPageReqVO pageReqVO) {
        var page = adsMapper.selectPage(pageReqVO);
        fillTargetNames(page.getList());
        return page;
    }

    @Override
    public List<AdsDO> getAdsList() {
        return adsMapper.selectList(new LambdaQueryWrapper<AdsDO>().eq(AdsDO::getStatus, 0).last("limit 10"));
    }
}