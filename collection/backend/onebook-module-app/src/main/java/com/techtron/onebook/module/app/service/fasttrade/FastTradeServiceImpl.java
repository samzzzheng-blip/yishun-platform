package com.techtron.onebook.module.app.service.fasttrade;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradeItemVO;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradePageReqVO;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradeRespVO;
import com.techtron.onebook.module.app.controller.admin.fasttrade.vo.FastTradeSaveReqVO;
import com.techtron.onebook.module.app.convert.fastrade.FastTradeConvert;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.fasttrade.FastTradeDO;
import com.techtron.onebook.module.app.dal.dataobject.fasttrade.FastTradeItemDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.fasttrade.FastTradeItemMapper;
import com.techtron.onebook.module.app.dal.mysql.fasttrade.FastTradeMapper;
import com.techtron.onebook.module.app.enums.NotifySceneEnum;
import com.techtron.onebook.module.app.mq.producer.DealNotifyProducer;
import com.techtron.onebook.module.app.service.collection.CollectionService;
import com.techtron.onebook.module.infra.api.config.ConfigApi;
import com.techtron.onebook.module.pay.api.wallet.PayWalletApi;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import com.techtron.onebook.module.pay.enums.wallet.PayWalletBizTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.techtron.onebook.framework.common.enums.UserTypeEnum.MEMBER;
import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.FAST_TRADE_NOT_EXISTS;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.SELL_NO_COLLECTION;

/**
 * 快速变现 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class FastTradeServiceImpl implements FastTradeService {

    @Resource
    private FastTradeMapper fastTradeMapper;

    @Resource
    private CollectionService collectionService;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private ConfigApi  configApi;

    @Resource
    private PayWalletApi payWalletApi;

    @Resource
    private DealNotifyProducer dealNotifyProducer;

    @Resource
    private FastTradeItemMapper fastTradeItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFastTrade(FastTradeSaveReqVO createReqVO) {
        List<CollectionDO> list = collectionMapper.selectForUpdate(createReqVO.getCollectionIds(), createReqVO.getUserId());
        if (list == null || list.isEmpty()) {
            throw exception(SELL_NO_COLLECTION);
        }
        List<FastTradeItemDO> ftItemList = FastTradeConvert.INSTANCE.convert(list);
        FastTradeDO fastTradeDO = BeanUtils.toBean(createReqVO, FastTradeDO.class);
        fastTradeMapper.insert(fastTradeDO);
        long id = fastTradeDO.getId();
        ftItemList.forEach(item -> item.setFastId(id));
        fastTradeItemMapper.insertBatch(ftItemList);
        List<CollectionDO> updateList = list.stream().map(collectionDO -> new CollectionDO().setId(collectionDO.getId())
                .setStock(0).setTradeStatus(1)).toList();
        collectionMapper.updateBatch(updateList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFastTrade(FastTradeSaveReqVO updateReqVO) {
        // 校验存在
        FastTradeDO fastTradeDO = validateFastTradeExists(updateReqVO.getId());
        // 更新
        FastTradeDO updateObj = BeanUtils.toBean(updateReqVO, FastTradeDO.class);
        fastTradeMapper.updateById(updateObj);
        List<FastTradeItemDO> fastTradeItemDOS = fastTradeItemMapper.selectList(new LambdaQueryWrapper<FastTradeItemDO>()
                .eq(FastTradeItemDO::getFastId, updateReqVO.getId()));
        List<Long> ids = fastTradeItemDOS.stream().map(FastTradeItemDO::getCollectionId).toList();
        if (updateReqVO.getStatus() == 1) {
            String fastFee = configApi.getConfigValueByKey("fastfee");
            float rate = (10000 - Float.parseFloat(fastFee) * 100) /10000;
            int price = (int) (updateReqVO.getPrice() * rate);
            PayWalletAddBalanceReqDTO payWalletAddBalanceReqDTO = new PayWalletAddBalanceReqDTO()
                    .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.INCOME.getType())
                    .setUserId(fastTradeDO.getUserId()).setBizId(updateReqVO.getId().toString())
                    .setPrice(price);

            payWalletApi.addWalletBalance(payWalletAddBalanceReqDTO);

            List<CollectionDO> list = collectionMapper.selectForUpdate0(ids, fastTradeDO.getUserId())
                    .stream().map(item -> new CollectionDO()
                            .setId(item.getId())
                            .setTradeStatus(0)
                            .setRealStock(0)
                            .setName(item.getName())
                    ).toList();
            collectionMapper.updateBatch(list);
            String names = list.stream().map(CollectionDO::getName).collect(Collectors.joining(","));
            // 发消息
            dealNotifyProducer.sendNotifySendMessage(NotifySceneEnum.FAST_NOTIFY.getTemplateCode(), fastTradeDO.getUserId(), names, updateReqVO.getPrice());
        } else {
            // 驳回
            List<CollectionDO> list = collectionMapper.selectForUpdate0(ids, fastTradeDO.getUserId())
                    .stream().map(item -> new CollectionDO()
                            .setId(item.getId())
                            .setTradeStatus(0)
                            .setStock(item.getRealStock())
                            .setName(item.getName())
                    ).toList();
            collectionMapper.updateBatch(list);
        }
    }

    @Override
    public void deleteFastTrade(Long id) {
        // 校验存在
        validateFastTradeExists(id);
        // 删除
        fastTradeMapper.deleteById(id);
    }

    @Override
        public void deleteFastTradeListByIds(List<Long> ids) {
        // 删除
        fastTradeMapper.deleteByIds(ids);
        }


    private FastTradeDO validateFastTradeExists(Long id) {
        FastTradeDO fastTradeDO = fastTradeMapper.selectById(id);
        if (fastTradeDO == null) {
            throw exception(FAST_TRADE_NOT_EXISTS);
        }
        return fastTradeDO;
    }

    @Override
    public FastTradeDO getFastTrade(Long id) {
        return fastTradeMapper.selectById(id);
    }

    @Override
    public PageResult<FastTradeRespVO> getFastTradePage(FastTradePageReqVO pageReqVO) {
        PageResult<FastTradeDO> result = fastTradeMapper.selectPage(pageReqVO);
        PageResult<FastTradeRespVO> pageResult = BeanUtils.toBean(result, FastTradeRespVO.class);
        List<FastTradeRespVO> list = pageResult.getList();
        if (list.isEmpty()) {
            return pageResult;
        }
        List<Long> ids = list.stream().map(FastTradeRespVO::getId).toList();
         MPJLambdaWrapper<FastTradeItemDO> wrapper = new MPJLambdaWrapper<FastTradeItemDO>()
                        .selectAll(FastTradeItemDO.class)
                        .select(CollectionDO::getName, CollectionDO::getPicUrls)
                        .leftJoin(CollectionDO.class, CollectionDO::getId, FastTradeItemDO::getCollectionId)
                        .in(FastTradeItemDO::getFastId, ids);
            List<FastTradeItemVO> fastTradeItemDOS = fastTradeItemMapper.selectJoinList(FastTradeItemVO.class, wrapper);
            Map<Long, List<FastTradeItemVO>> itemMap = fastTradeItemDOS.stream().collect(Collectors.groupingBy(FastTradeItemVO::getFastId));
            list.forEach(item -> {
                List<FastTradeItemVO> fastTradeItems = itemMap.get(item.getId());
                item.setItems(fastTradeItems);
            });
        return pageResult;
    }

}