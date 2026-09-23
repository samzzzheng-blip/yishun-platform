package com.techtron.onebook.module.app.service.category;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategoryPageReqVO;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategorySaveReqVO;
import com.techtron.onebook.module.app.controller.app.category.vo.AppCollectionCategoryReqVO;
import com.techtron.onebook.module.app.controller.app.category.vo.AppCollectionCategoryRespVO;
import com.techtron.onebook.module.app.controller.app.category.vo.AppTradeInfoRespVO;
import com.techtron.onebook.module.app.controller.app.category.vo.TradeInfo;
import com.techtron.onebook.module.app.dal.dataobject.buyorder.BuyOrderDO;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.sellorder.SellOrderDO;
import com.techtron.onebook.module.app.dal.mysql.buyorder.BuyOrderMapper;
import com.techtron.onebook.module.app.dal.mysql.category.CollectionCategoryMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.sellorder.SellOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.COLLECTION_CATEGORY_CANT_DELETE;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.COLLECTION_CATEGORY_NOT_EXISTS;

/**
 * 藏品分类 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class CollectionCategoryServiceImpl implements CollectionCategoryService {
    @Resource
    private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Override
    public CollectionCategoryDO getPurchaseCategory(Long sourceCategoryId) {
        // Standard public categories survive ownership transfer. Never transfer a seller's private category.
        if (sourceCategoryId != null) {
            var sources = jdbc.queryForList(
                    "SELECT standard.id,standard.name FROM app_category source JOIN app_category standard ON standard.id=CASE WHEN source.user_id=0 THEN source.id ELSE source.copy_id END WHERE source.id=? AND standard.user_id=0 AND standard.deleted=0", sourceCategoryId);
            if (!sources.isEmpty()) {
                var row = sources.get(0);
                String name = (String) row.get("name");
                if (name != null && !name.equals("直购物品") && !name.matches("直购[0-9]+")) {
                    return new CollectionCategoryDO().setId(((Number) row.get("id")).longValue()).setName(name);
                }
            }
        }
        var ids = jdbc.queryForList(
                "SELECT id FROM app_category WHERE user_id=0 AND name=? AND deleted=0 ORDER BY id LIMIT 1",
                Long.class, "自定义");
        if (ids.isEmpty()) throw new IllegalStateException("系统公共分类“自定义”未配置");
        return new CollectionCategoryDO().setId(ids.get(0)).setName("自定义");
    }

    @Resource
    private CollectionCategoryMapper collectionCategoryMapper;
    @Autowired
    private SellOrderMapper sellOrderMapper;
    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private BuyOrderMapper buyOrderMapper;

    @Override
    public Long createCollectionCategory(CollectionCategorySaveReqVO createReqVO) {
        // 插入
        CollectionCategoryDO collectionCategory = BeanUtils.toBean(createReqVO, CollectionCategoryDO.class);
        collectionCategoryMapper.insert(collectionCategory);

        // 返回
        return collectionCategory.getId();
    }

    @Override
    public void updateCollectionCategory(CollectionCategorySaveReqVO updateReqVO) {
        // 校验存在
        validateCollectionCategoryExists(updateReqVO.getId());
        // 更新
        CollectionCategoryDO updateObj = BeanUtils.toBean(updateReqVO, CollectionCategoryDO.class);
        collectionCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteCollectionCategory(Long id) {
        // 校验存在
        validateCollectionCategoryExists(id);

        List<CollectionDO> collectionDOS = collectionMapper.selectList(new LambdaQueryWrapper<CollectionDO>()
                .eq(CollectionDO::getCategoryId, id)
                .gt(CollectionDO::getRealStock, 0)
        );

        if (!collectionDOS.isEmpty()) {
            throw exception(COLLECTION_CATEGORY_CANT_DELETE);
        }
        // 删除
        collectionCategoryMapper.deleteById(id);
    }

    @Override
        public void deleteCollectionCategoryListByIds(List<Long> ids) {
        // 删除
        collectionCategoryMapper.deleteByIds(ids);
        }


    private void validateCollectionCategoryExists(Long id) {
        if (collectionCategoryMapper.selectById(id) == null) {
            throw exception(COLLECTION_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public CollectionCategoryDO getCollectionCategory(Long id) {
        return collectionCategoryMapper.selectById(id);
    }

    @Override
    public PageResult<CollectionCategoryDO> getCollectionCategoryPage(CollectionCategoryPageReqVO pageReqVO) {
        return collectionCategoryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CollectionCategoryDO> getCollectionCategoryList(Long userId) {
        if (userId == null) {
            return collectionCategoryMapper.selectList(new LambdaQueryWrapper<CollectionCategoryDO>()
                    .eq(CollectionCategoryDO::getUserId, 0));
        }
        return collectionCategoryMapper.selectList(new LambdaQueryWrapper<CollectionCategoryDO>()
                .eq(CollectionCategoryDO::getUserId, userId)
                .or()
                .eq(CollectionCategoryDO::getUserId, 0));
    }

    @Override
    public List<AppCollectionCategoryRespVO> getCollectionCategoryList(AppCollectionCategoryReqVO reqVO) {
        var held = warehouseView.list(reqVO.getUserId());
        if (held.isEmpty()) return List.of();
        var groups = held.stream().filter(item -> item.getCategoryId() != null)
            .collect(Collectors.groupingBy(com.techtron.onebook.module.app.controller.app.collection.vo.AppCollectionRespVO::getCategoryId));
        if (groups.isEmpty()) return List.of();
        var categories = collectionCategoryMapper.selectList(new LambdaQueryWrapper<CollectionCategoryDO>()
            .in(CollectionCategoryDO::getId, groups.keySet()));
        List<AppCollectionCategoryRespVO> result = new ArrayList<>();
        for (var category : categories) {
            var items = groups.get(category.getId());
            String keyword = reqVO.getCategoryName();
            if (keyword != null && !keyword.isBlank() && (category.getName() == null || !category.getName().contains(keyword))
                && items.stream().noneMatch(item -> item.getName() != null && item.getName().contains(keyword))) continue;
            var view = BeanUtils.toBean(category, AppCollectionCategoryRespVO.class);
            view.setStock(items.stream().mapToInt(item -> item.getStock() == null ? 0 : item.getStock()).sum());
            view.setTradeStatus(items.stream().mapToInt(item -> item.getTradeStatus() == null ? 0 : item.getTradeStatus()).max().orElse(0));
            view.setGetbackStatus(items.stream().mapToInt(item -> item.getGetbackStatus() == null ? 0 : item.getGetbackStatus()).max().orElse(0));
            if (view.getPicUrl() == null || view.getPicUrl().isBlank()) {
                var first = items.get(0);
                view.setPicUrl(first.getPicUrls() != null && !first.getPicUrls().isEmpty() ? first.getPicUrls().get(0) : first.getPicUrl());
            }
            result.add(view);
        }
        return result;
    }

    @Resource
    private com.techtron.onebook.module.app.service.collection.WarehouseViewService warehouseView;

    @Override
    public int getLatestPrice(Long id) {
        SellOrderDO latestOrder = sellOrderMapper.selectOne(new LambdaQueryWrapper<SellOrderDO>()
                .eq(SellOrderDO::getCategoryId, id)
                .apply("deal_amount > amount")
                .orderByDesc(SellOrderDO::getUpdateTime)
                .last("limit 1")
        );
        if(latestOrder == null){
            return 0;
        }
        return latestOrder.getPrice();
    }

    @Override
    public AppTradeInfoRespVO getTradeInfo(Long id) {
        QueryWrapper<SellOrderDO> queryWrapperSell = new QueryWrapper<>();
        queryWrapperSell.select("price", "SUM(amount) as totalAmount", "MAX(create_time) as time")
                .eq("category_id", id)
                .gt("amount", 0)
                .groupBy("price")
                .orderByAsc("price")
                .last("limit 10");
        List<Map<String, Object>> resultSell = sellOrderMapper.selectMaps(queryWrapperSell);
        List<TradeInfo> sellList = BeanUtils.toBean(resultSell, TradeInfo.class);


        QueryWrapper<BuyOrderDO> queryWrapperBuy = new QueryWrapper<>();
        queryWrapperBuy.select("price", "SUM(amount) as totalAmount", "MAX(create_time) as time")
                .eq("category_id", id)
                .gt("amount", 0)
                .eq("status", 1)
                .groupBy("price")
                .orderByDesc("price")
                .last("limit 10");

        List<Map<String, Object>> resultBuy = buyOrderMapper.selectMaps(queryWrapperBuy);

        List<TradeInfo> buyList = BeanUtils.toBean(resultBuy, TradeInfo.class);
        return new AppTradeInfoRespVO().setBuyList(buyList).setSellList(sellList);
    }

    @Override
    public void changeCategory(Long categoryId, List<Long> collectionIds) {

        CollectionCategoryDO categoryDO = collectionCategoryMapper.selectById(categoryId);
        if(categoryDO == null){
            throw exception(COLLECTION_CATEGORY_NOT_EXISTS);
        }

        if (collectionIds == null || collectionIds.isEmpty()) {
            throw exception(COLLECTION_CATEGORY_NOT_EXISTS);
        }
        collectionMapper.update(null, new LambdaUpdateWrapper<CollectionDO>()
                .in(CollectionDO::getId, collectionIds)
                .set(CollectionDO::getCategoryId, categoryId)
                .set(CollectionDO::getCategoryName, categoryDO.getName()));
    }

}
