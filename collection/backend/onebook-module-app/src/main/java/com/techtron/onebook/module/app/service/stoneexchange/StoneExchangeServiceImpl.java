package com.techtron.onebook.module.app.service.stoneexchange;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.module.app.controller.admin.stoneexchange.vo.*;
import com.techtron.onebook.module.app.convert.stoneexchange.StoneExchangeConvert;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionRecordDO;
import com.techtron.onebook.module.app.dal.dataobject.energystone.EnergyStoneDO;
import com.techtron.onebook.module.app.dal.dataobject.stoneexchange.StoneExchangeDO;
import com.techtron.onebook.module.app.dal.dataobject.stoneexchange.StoneExchangeItemDO;
import com.techtron.onebook.module.app.dal.dataobject.stonerecord.StoneRecordDO;
import com.techtron.onebook.module.app.dal.mysql.category.CollectionCategoryMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionRecordMapper;
import com.techtron.onebook.module.app.dal.mysql.energystone.EnergyStoneMapper;
import com.techtron.onebook.module.app.dal.mysql.stoneexchange.StoneExchangeItemMapper;
import com.techtron.onebook.module.app.dal.mysql.stoneexchange.StoneExchangeMapper;
import com.techtron.onebook.module.app.dal.mysql.stonerecord.StoneRecordMapper;
import com.techtron.onebook.module.app.enums.stonerecord.StoneRecordTypeEnum;
import com.techtron.onebook.module.member.dal.dataobject.user.MemberUserDO;
import com.techtron.onebook.module.member.dal.mysql.user.MemberUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.*;

/**
 * 能量石兑换 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
@Slf4j
public class StoneExchangeServiceImpl implements StoneExchangeService {

    @Resource
    private StoneExchangeMapper stoneExchangeMapper;

    @Resource
    private StoneExchangeItemMapper stoneExchangeItemMapper;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private CollectionRecordMapper collectionRecordMapper;

    @Resource
    private EnergyStoneMapper energyStoneMapper;

    @Resource
    private CollectionCategoryMapper collectionCategoryMapper;

    @Resource
    private MemberUserMapper memberUserMapper;

    @Resource
    private StoneRecordMapper stoneRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStoneExchange(StoneExchangeSaveReqVO createReqVO) {
        List<CollectionDO> list = collectionMapper.selectForUpdate(createReqVO.getCollectionIds(), createReqVO.getUserId());

        if (list == null || list.isEmpty()) {
            throw exception(STONE_COLLECTION_NOT_EXISTS);
        }
        Long categoryId = list.get(0).getCategoryId();
        CollectionCategoryDO collectionCategoryDO = collectionCategoryMapper.selectById(categoryId);
        Integer exchangeRate = collectionCategoryDO.getExchangeRate();
        if (exchangeRate == null) {
            exchangeRate = 1;
        }
        // 插入
        StoneExchangeDO stoneExchange = BeanUtils.toBean(createReqVO, StoneExchangeDO.class);
        stoneExchangeMapper.insert(stoneExchange);
        long id = stoneExchange.getId();
        List<StoneExchangeItemDO> items = StoneExchangeConvert.INSTANCE.convert(list);
        items.forEach(item -> item.setStoneExchangeId(id));

        // 减少藏品
        List<CollectionDO> updateList = list.stream().map(collectionDO -> new CollectionDO()
                .setId(collectionDO.getId()).setStock(0).setRealStock(0)
        ).toList();
        collectionMapper.updateBatch(updateList);

        stoneExchangeItemMapper.insertBatch(items);
        // 增加能量石
        Integer total = list.stream().reduce(0,(sum, collectionDO) -> sum + collectionDO.getStock(),Integer::sum);
        CollectionDO firstCollection = list.get(0);
        collectionRecordMapper.insert(CollectionRecordDO.builder()
                .userId(firstCollection.getUserId())
                .categoryId(firstCollection.getCategoryId())
                .amount(-total)
                .type(4)
                .build());
        EnergyStoneDO res = energyStoneMapper.selectOne(new LambdaQueryWrapper<EnergyStoneDO>()
                .eq(EnergyStoneDO::getUserId, createReqVO.getUserId())
                .last("FOR UPDATE"));
        if(res == null){
            energyStoneMapper.insert(new EnergyStoneDO().setUserId(createReqVO.getUserId()).setAmount(total * exchangeRate));
        } else {
            energyStoneMapper.updateById(new EnergyStoneDO()
                            .setId(res.getId()).setAmount(res.getAmount() + total * exchangeRate));
        }
        StoneRecordDO lastRecord = stoneRecordMapper.selectLatestByUserId(createReqVO.getUserId());
        Integer lastBalance = lastRecord != null ? lastRecord.getBalance() : 0;
        stoneRecordMapper.insert(new StoneRecordDO()
                .setUserId(createReqVO.getUserId())
                .setAmount(total * exchangeRate)
                .setType(StoneRecordTypeEnum.EXCHANGE_STONE.getType())
                .setBalance(lastBalance + total * exchangeRate));

        // 返回
        return id;
    }

    @Override
    public void updateStoneExchange(StoneExchangeSaveReqVO updateReqVO) {
        // 校验存在
        validateStoneExchangeExists(updateReqVO.getId());
        // 更新
        StoneExchangeDO updateObj = BeanUtils.toBean(updateReqVO, StoneExchangeDO.class);
        stoneExchangeMapper.updateById(updateObj);
    }

    @Override
    public void deleteStoneExchange(Long id) {
        // 校验存在
        validateStoneExchangeExists(id);
        // 删除
        stoneExchangeMapper.deleteById(id);
    }

    @Override
        public void deleteStoneExchangeListByIds(List<Long> ids) {
        // 删除
        stoneExchangeMapper.deleteByIds(ids);
        }


    private void validateStoneExchangeExists(Long id) {
        if (stoneExchangeMapper.selectById(id) == null) {
            throw exception(STONE_EXCHANGE_NOT_EXISTS);
        }
    }

    @Override
    public StoneExchangeDO getStoneExchange(Long id) {
        return stoneExchangeMapper.selectById(id);
    }

    @Override
    public PageResult<StoneExchangeRespVO> getStoneExchangePage(StoneExchangePageReqVO pageReqVO) {
        PageResult<StoneExchangeDO> result = stoneExchangeMapper.selectPage(pageReqVO);
        PageResult<StoneExchangeRespVO> pageResult = BeanUtils.toBean(result, StoneExchangeRespVO.class);
        List<StoneExchangeRespVO> list = pageResult.getList();
        List<Long> ids = list.stream().map(StoneExchangeRespVO::getId).toList();
        MPJLambdaWrapper<StoneExchangeItemDO> wrapper = new MPJLambdaWrapper<StoneExchangeItemDO>()
                .selectAll(StoneExchangeItemDO.class)
                .select(CollectionDO::getName, CollectionDO::getPicUrls)
                .leftJoin(CollectionDO.class, CollectionDO::getId, StoneExchangeItemDO::getCollectionId)
                .in(StoneExchangeItemDO::getStoneExchangeId, ids);
        List<StoneExchangeItemVO> stoneExchangeItemDOS = stoneExchangeItemMapper.selectJoinList(StoneExchangeItemVO.class, wrapper);
        Map<Long, List<StoneExchangeItemVO>> itemMap = stoneExchangeItemDOS.stream().collect(Collectors.groupingBy(StoneExchangeItemVO::getStoneExchangeId));
        list.forEach(item -> {
            List<StoneExchangeItemVO> stoneExchangeItems = itemMap.get(item.getId());
            item.setItems(stoneExchangeItems);
        });
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addStone(StoneAddReqVO reqVO) {
        MemberUserDO memberUserDO = memberUserMapper.selectByMobile(reqVO.getUserId());
        if (memberUserDO == null) {
            throw exception(USER_NOT_EXISTS);
        }
        EnergyStoneDO res = energyStoneMapper.selectOne(new LambdaQueryWrapper<EnergyStoneDO>()
                .eq(EnergyStoneDO::getUserId, memberUserDO.getId())
                .last("FOR UPDATE"));
        if(res == null){
            energyStoneMapper.insert(new EnergyStoneDO().setUserId(memberUserDO.getId()).setAmount(reqVO.getAmount()));
        } else {
            energyStoneMapper.updateById(new EnergyStoneDO()
                    .setId(res.getId()).setAmount(res.getAmount() + reqVO.getAmount()));
        }
        StoneRecordDO lastRecord = stoneRecordMapper.selectLatestByUserId(memberUserDO.getId());
        Integer lastBalance = lastRecord != null ? lastRecord.getBalance() : 0;
        stoneRecordMapper.insert(new StoneRecordDO()
                .setUserId(memberUserDO.getId())
                .setAmount(reqVO.getAmount())
                .setType(StoneRecordTypeEnum.ADMIN_ADD.getType())
                .setBalance(lastBalance + reqVO.getAmount()));
        return Boolean.TRUE;
    }

    @Override
    public Integer batchExchangeByCategory(Long userId, Integer needStoneAmount) {
        if (needStoneAmount != null && needStoneAmount > 0) {
            EnergyStoneDO currentStone = energyStoneMapper.selectOne(new LambdaQueryWrapper<EnergyStoneDO>()
                    .eq(EnergyStoneDO::getUserId, userId));
            if (currentStone != null && currentStone.getAmount() >= needStoneAmount) {
                return 0;
            }
        }

        List<CollectionCategoryDO> categories = collectionCategoryMapper.selectList(new LambdaQueryWrapperX<CollectionCategoryDO>()
                .eq(CollectionCategoryDO::getUserId, 0)
                .or()
                .isNotNull(CollectionCategoryDO::getCopyId));

        if (CollUtil.isEmpty(categories)) {
            return 0;
        }

        List<Long> categoryIds = categories.stream()
                .map(CollectionCategoryDO::getId)
                .collect(Collectors.toList());

        List<CollectionDO> collections = collectionMapper.selectList(new LambdaQueryWrapperX<CollectionDO>()
                .eq(CollectionDO::getUserId, userId)
                .in(CollectionDO::getCategoryId, categoryIds)
                .eq(CollectionDO::getStatus, 1)
                .gt(CollectionDO::getStock, 0));

        if (CollUtil.isEmpty(collections)) {
            return 0;
        }

        Map<Long, List<CollectionDO>> groupedByCategory = collections.stream()
                .collect(Collectors.groupingBy(CollectionDO::getCategoryId));

        int successCount = 0;
        for (Map.Entry<Long, List<CollectionDO>> entry : groupedByCategory.entrySet()) {
            try {
                List<Long> collectionIds = entry.getValue().stream()
                        .map(CollectionDO::getId)
                        .collect(Collectors.toList());

                StoneExchangeSaveReqVO reqVO = new StoneExchangeSaveReqVO();
                reqVO.setUserId(userId);
                reqVO.setCollectionIds(collectionIds);

                getSelf().createStoneExchange(reqVO);
                successCount++;

                if (needStoneAmount != null && needStoneAmount > 0) {
                    EnergyStoneDO updatedStone = energyStoneMapper.selectOne(new LambdaQueryWrapper<EnergyStoneDO>()
                            .eq(EnergyStoneDO::getUserId, userId));
                    if (updatedStone != null && updatedStone.getAmount() >= needStoneAmount) {
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("[batchExchangeByCategory][处理分类 {} 的兑换失败]", entry.getKey(), e);
            }
        }

        return successCount;
    }

    private StoneExchangeServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}