package com.techtron.onebook.module.app.service.getback;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.getback.vo.*;
import com.techtron.onebook.module.app.convert.getback.GetbackConvert;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionRecordDO;
import com.techtron.onebook.module.app.dal.dataobject.getback.GetbackDO;
import com.techtron.onebook.module.app.dal.dataobject.getback.GetbackItemDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionRecordMapper;
import com.techtron.onebook.module.app.dal.mysql.getback.GetbackItemMapper;
import com.techtron.onebook.module.app.dal.mysql.getback.GetbackMapper;
import com.techtron.onebook.module.app.enums.NotifySceneEnum;
import com.techtron.onebook.module.app.mq.producer.GetbackNotifyProducer;
import com.techtron.onebook.module.app.service.notify.AppSubscribeMessageService;
import com.techtron.onebook.module.app.service.yikoujia.YikoujiaService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.*;

/**
 * 取回 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class GetbackServiceImpl implements GetbackService {

    @Resource
    private GetbackMapper getbackMapper;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private CollectionRecordMapper collectionRecordMapper;

    @Resource
    private GetbackNotifyProducer getbackNotifyProducer;

    @Resource
    private GetbackItemMapper getbackItemMapper;

    @Resource
    private YikoujiaService yikoujiaService;

    @Resource
    private AppSubscribeMessageService subscribeMessageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGetback(GetbackSaveReqVO createReqVO) {
        List<Long> collectionIds = createReqVO.getCollectionIds() == null
                ? Collections.emptyList()
                : createReqVO.getCollectionIds().stream().filter(Objects::nonNull).distinct().sorted().toList();
        if (collectionIds.isEmpty() || collectionIds.size() != createReqVO.getCollectionIds().size()) {
            throw exception(GETBACK_COLLECTION_NOT_EXISTS);
        }

        // 取回是一个组合操作：出售中的藏品先自动下架，再生成取回单。
        yikoujiaService.delistCollectionsForGetback(collectionIds, createReqVO.getUserId());
        List<CollectionDO> list = collectionMapper.selectForUpdate(collectionIds, createReqVO.getUserId());

        if (list == null || list.size() != collectionIds.size()) {
            throw exception(GETBACK_COLLECTION_NOT_EXISTS);
        }
        for (CollectionDO collectionDO : list) {
            if (!Objects.equals(collectionDO.getGetbackStatus(), 0)) {
                throw exception(GETBACK_EXIST);
            }
            if (!Objects.equals(collectionDO.getTradeStatus(), 0)) {
                throw exception(GETBACK_COLLECTION_STATUS_INVALID);
            }
        }
        // 插入
        GetbackDO getback = BeanUtils.toBean(createReqVO, GetbackDO.class);
        getbackMapper.insert(getback);
        long id = getback.getId();
        List<GetbackItemDO> getbackItemList = GetbackConvert.INSTANCE.convert(list);
        getbackItemList.forEach(item -> item.setGetbackId(id));
        getbackItemMapper.insertBatch(getbackItemList);
        List<CollectionDO> updateList = list.stream().map(collectionDO -> new CollectionDO()
                .setId(collectionDO.getId()).setStock(0).setGetbackStatus(1)
        ).toList();
        collectionMapper.updateBatch(updateList);

        // 新增藏品变更记录（取回）
        Integer total = list.stream().reduce(0, (sum, c) -> sum + (c.getStock() != null ? c.getStock() : 0), Integer::sum);
        CollectionDO firstCollection = list.get(0);
        collectionRecordMapper.insert(CollectionRecordDO.builder()
                .userId(firstCollection.getUserId())
                .categoryId(firstCollection.getCategoryId())
                .amount(-total)
                .type(7)
                .build());

        String names = list.stream().map(CollectionDO::getName).collect(Collectors.joining(","));
        subscribeMessageService.notifyGetbackAccepted(createReqVO.getUserId(), names, getback.getCreateTime());

        // 返回
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGetback(GetbackUpdateReqVO updateReqVO) {
        GetbackDO getback = getbackMapper.selectByIdForUpdate(updateReqVO.getId());
        if (getback == null) {
            throw exception(GETBACK_NOT_EXISTS);
        }
        if (Objects.equals(updateReqVO.getStatus(), 1)) {
            if (!Objects.equals(getback.getStatus(), 0)) {
                throw exception(GETBACK_STATUS_INVALID);
            }
        } else if (Objects.equals(updateReqVO.getStatus(), 2)) {
            if (!Objects.equals(getback.getStatus(), 1)) {
                throw exception(GETBACK_STATUS_INVALID);
            }
        } else {
            throw exception(GETBACK_STATUS_INVALID);
        }

        if (Objects.equals(updateReqVO.getStatus(), 1)) {
            String carrier = updateReqVO.getExpressCompany();
            String number = updateReqVO.getDeliverCode();
            if (number == null || number.isBlank() || number.length() > 64
                || !com.techtron.onebook.module.app.service.logistics.GetbackLogisticsService.CARRIERS.contains(carrier == null ? "" : carrier)) {
                throw exception(GETBACK_EXPRESS_REQUIRED);
            }
            updateReqVO.setDeliverCode(number.trim());
        } else {
            // A member confirming receipt cannot rewrite the carrier or waybill.
            updateReqVO.setExpressCompany(null);
            updateReqVO.setDeliverCode(null);
        }

        List<GetbackItemDO> getbackItemDOS = getbackItemMapper.selectList(new LambdaQueryWrapper<GetbackItemDO>()
                .eq(GetbackItemDO::getGetbackId, updateReqVO.getId()));
        List<Long> ids = getbackItemDOS.stream().map(GetbackItemDO::getCollectionId).toList();
        // 确认收货
        if (Objects.equals(updateReqVO.getStatus(), 2)) {
            if (!Objects.equals(updateReqVO.getUserId(), getback.getUserId())) {
                throw exception(GETBACK_FAIL);
            }
            List<CollectionDO> list = collectionMapper.selectForUpdate0(ids, getback.getUserId())
                    .stream().map(item -> new CollectionDO()
                            .setId(item.getId())
                            .setGetbackStatus(0)
                            .setRealStock(0)
                    ).toList();

            collectionMapper.updateBatch(list);
        } else {
            List<CollectionDO> list = collectionMapper.selectForUpdate0(ids, getback.getUserId())
                    .stream().map(item -> new CollectionDO()
                            .setId(item.getId())
                            .setGetbackStatus(2)
                            .setName(item.getName())
                    ).toList();

            collectionMapper.updateBatch(list);
            String names = list.stream().map(CollectionDO::getName).collect(Collectors.joining(","));
            // 发消息
            getbackNotifyProducer.sendNotifySendMessage(NotifySceneEnum.GETBACK_NOTIFY.getTemplateCode(), getback.getUserId(), names, updateReqVO.getDeliverCode());
            subscribeMessageService.notifyOrderStatus(getback.getUserId(), updateReqVO.getDeliverCode(),
                    "已发货", names);
        }

        GetbackDO updateObj = BeanUtils.toBean(updateReqVO, GetbackDO.class);
        getbackMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelGetback(Long id) {
        GetbackDO getback = getbackMapper.selectByIdForUpdate(id);
        if (getback == null) {
            throw exception(GETBACK_NOT_EXISTS);
        }
        if (!Objects.equals(getback.getStatus(), 0)) {
            throw exception(GETBACK_STATUS_INVALID);
        }

        List<GetbackItemDO> items = getbackItemMapper.selectList(new LambdaQueryWrapper<GetbackItemDO>()
                .eq(GetbackItemDO::getGetbackId, id));
        if (items.isEmpty()) {
            throw exception(GETBACK_CANCEL_COLLECTION_INVALID);
        }
        if (items.stream().anyMatch(item -> item.getCollectionId() == null
                || item.getAmount() == null || item.getAmount() <= 0)) {
            throw exception(GETBACK_CANCEL_COLLECTION_INVALID);
        }
        Map<Long, Integer> amountByCollectionId = items.stream().collect(Collectors.toMap(
                GetbackItemDO::getCollectionId,
                GetbackItemDO::getAmount,
                Integer::sum
        ));
        List<Long> collectionIds = amountByCollectionId.keySet().stream().toList();
        List<CollectionDO> collections = collectionMapper.selectForUpdate0(collectionIds, getback.getUserId());
        if (collections.size() != collectionIds.size()
                || collections.stream().anyMatch(item -> !Objects.equals(item.getGetbackStatus(), 1)
                || !Objects.equals(item.getStock(), 0))) {
            throw exception(GETBACK_CANCEL_COLLECTION_INVALID);
        }

        List<CollectionDO> updateList = collections.stream().map(item -> new CollectionDO()
                .setId(item.getId())
                .setStock(amountByCollectionId.get(item.getId()))
                .setGetbackStatus(0)
        ).toList();
        collectionMapper.updateBatch(updateList);

        int total = amountByCollectionId.values().stream().mapToInt(Integer::intValue).sum();
        CollectionDO firstCollection = collections.get(0);
        collectionRecordMapper.insert(CollectionRecordDO.builder()
                .userId(getback.getUserId())
                .categoryId(firstCollection.getCategoryId())
                .amount(total)
                .type(7)
                .build());

        // 逻辑删除保留审计数据；关联明细保留，用于后续问题追溯。
        getbackMapper.deleteById(id);
    }

    @Override
    public void deleteGetback(Long id) {
        // 校验存在
        validateGetbackExists(id);
        // 删除
        getbackMapper.deleteById(id);
    }

    @Override
        public void deleteGetbackListByIds(List<Long> ids) {
        // 删除
        getbackMapper.deleteByIds(ids);
        }


    private GetbackDO validateGetbackExists(Long id) {
        GetbackDO getback = getbackMapper.selectById(id);
        if (getback == null) {
            throw exception(GETBACK_NOT_EXISTS);
        }
        return getback;
    }

    @Override
    public GetbackDO getGetback(Long id) {
        return getbackMapper.selectById(id);
    }

    @Override
    public PageResult<GetbackRespVO> getGetbackPage(GetbackPageReqVO pageReqVO) {
        PageResult<GetbackDO> result = getbackMapper.selectPage(pageReqVO);
        PageResult<GetbackRespVO> pageResult = BeanUtils.toBean(result, GetbackRespVO.class);
        List<GetbackRespVO> list = pageResult.getList();
        List<Long> ids = list.stream().map(GetbackRespVO::getId).toList();
         MPJLambdaWrapper<GetbackItemDO> wrapper = new MPJLambdaWrapper<GetbackItemDO>()
                                .selectAll(GetbackItemDO.class)
                                .select(CollectionDO::getName, CollectionDO::getPicUrls)
                                .leftJoin(CollectionDO.class, CollectionDO::getId, GetbackItemDO::getCollectionId)
                                .in(GetbackItemDO::getGetbackId, ids);
                    List<GetbackItemVO> GetbackItemDOS = getbackItemMapper.selectJoinList(GetbackItemVO.class, wrapper);
                    Map<Long, List<GetbackItemVO>> itemMap = GetbackItemDOS.stream().collect(Collectors.groupingBy(GetbackItemVO::getGetbackId));
                    list.forEach(item -> {
                        List<GetbackItemVO> getbackItems = itemMap.get(item.getId());
                        item.setItems(getbackItems);
                    });
        return pageResult;
    }

    @Override
    public GetbackDO getDeliver(Long collectionId, Long userId) {
        GetbackItemDO getbackItemDO = getbackItemMapper.selectOne(
                new LambdaQueryWrapper<GetbackItemDO>()
                .eq(GetbackItemDO::getCollectionId, collectionId)
                .last("limit 1")
        );
        return getbackMapper.selectOne(new QueryWrapper<GetbackDO>()
                .eq("id", getbackItemDO.getGetbackId()).eq("user_id", userId).eq("status", 1));
    }

    @Override
    public List<GetbackRespVO> getGetbackListByUserId(Long userId) {
        List<GetbackDO> list = getbackMapper.selectListByUserId(userId);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<GetbackRespVO> result = BeanUtils.toBean(list, GetbackRespVO.class);
        List<Long> ids = result.stream().map(GetbackRespVO::getId).toList();
        MPJLambdaWrapper<GetbackItemDO> wrapper = new MPJLambdaWrapper<GetbackItemDO>()
                .selectAll(GetbackItemDO.class)
                .select(CollectionDO::getName, CollectionDO::getPicUrls)
                .leftJoin(CollectionDO.class, CollectionDO::getId, GetbackItemDO::getCollectionId)
                .in(GetbackItemDO::getGetbackId, ids);
        List<GetbackItemVO> getbackItemList = getbackItemMapper.selectJoinList(GetbackItemVO.class, wrapper);
        Map<Long, List<GetbackItemVO>> itemMap = getbackItemList.stream().collect(Collectors.groupingBy(GetbackItemVO::getGetbackId));
        result.forEach(item -> {
            List<GetbackItemVO> getbackItems = itemMap.get(item.getId());
            item.setItems(getbackItems);
        });
        return result;
    }

}
