package com.techtron.onebook.module.app.service.exchangelog;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.exchangelog.vo.ExchangeLogPageReqVO;
import com.techtron.onebook.module.app.controller.admin.exchangelog.vo.ExchangeLogSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.energystone.EnergyStoneDO;
import com.techtron.onebook.module.app.dal.dataobject.exchange.ExchangeDO;
import com.techtron.onebook.module.app.dal.dataobject.exchangelog.ExchangeLogDO;
import com.techtron.onebook.module.app.dal.dataobject.stonerecord.StoneRecordDO;
import com.techtron.onebook.module.app.dal.mysql.category.CollectionCategoryMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.energystone.EnergyStoneMapper;
import com.techtron.onebook.module.app.dal.mysql.exchange.ExchangeMapper;
import com.techtron.onebook.module.app.dal.mysql.exchangelog.ExchangeLogMapper;
import com.techtron.onebook.module.app.dal.mysql.stonerecord.StoneRecordMapper;
import com.techtron.onebook.module.app.enums.NotifySceneEnum;
import com.techtron.onebook.module.app.enums.stonerecord.StoneRecordTypeEnum;
import com.techtron.onebook.module.app.mq.producer.ExchangeNotifyProducer;
import com.techtron.onebook.module.app.mq.producer.GetbackNotifyProducer;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.*;

/**
 * 兑换记录 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class ExchangeLogServiceImpl implements ExchangeLogService {

    @Resource
    private ExchangeLogMapper exchangeLogMapper;

    @Resource
    private ExchangeMapper exchangeMapper;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private CollectionCategoryMapper collectionCategoryMapper;

    @Resource
    private ExchangeNotifyProducer exchangeNotifyProducer;

    @Resource
    private EnergyStoneMapper energyStoneMapper;

    @Resource
    private GetbackNotifyProducer getbackNotifyProducer;

    @Resource
    private StoneRecordMapper stoneRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createExchangeLog(ExchangeLogSaveReqVO createReqVO) {
        createReqVO.setAmount(1);
        // 校验兑换品数量够不够
        ExchangeDO exchangeDO = exchangeMapper.selectOne(new LambdaQueryWrapper<ExchangeDO>()
                .eq(ExchangeDO::getId, createReqVO.getExchangeId())
                .eq(ExchangeDO::getStatus, 0)
                .last("FOR UPDATE")
        );
        if (exchangeDO == null) {
            throw exception(EXCHANGE_NOT_EXISTS);
        }
        if (exchangeDO.getStock() < 1) {
            throw exception(EXCHANGE_NOT_EXISTS);
        }
        // 校验能量石数量够不够
        EnergyStoneDO energyStoneDO = energyStoneMapper.selectOne(new LambdaQueryWrapper<EnergyStoneDO>()
                .eq(EnergyStoneDO::getUserId, createReqVO.getUserId())
                .last("FOR UPDATE"));
        if(energyStoneDO == null || energyStoneDO.getAmount()< exchangeDO.getAmount()) {
            throw exception(EXCHANGE_NO_ENOUGH_COLLECTION);
        }
        // 兑换品数量减1
        exchangeMapper.updateById(new ExchangeDO().setId(exchangeDO.getId()).setStock(exchangeDO.getStock() - 1));

        // 能量石数量减少
        energyStoneMapper.updateById(new EnergyStoneDO().setId(energyStoneDO.getId()).setAmount(energyStoneDO.getAmount() - exchangeDO.getAmount()));

        StoneRecordDO lastRecord = stoneRecordMapper.selectLatestByUserId(createReqVO.getUserId());
        Integer lastBalance = lastRecord != null ? lastRecord.getBalance() : 0;
        // 名称取服务端商品，不信任客户端传来的名称；与扣款流水在同一事务内关联。
        ExchangeLogDO exchangeLog = BeanUtils.toBean(createReqVO, ExchangeLogDO.class);
        exchangeLog.setExchangeName(exchangeDO.getName());
        exchangeLogMapper.insert(exchangeLog);
        stoneRecordMapper.insert(new StoneRecordDO()
                .setUserId(createReqVO.getUserId())
                .setAmount(- exchangeDO.getAmount())
                .setType(StoneRecordTypeEnum.EXCHANGE_COLLECTION.getType())
                .setExchangeLogId(exchangeLog.getId())
                .setExchangeName(exchangeLog.getExchangeName())
                .setExchangeQuantity(exchangeLog.getAmount())
                .setBalance(lastBalance - exchangeDO.getAmount()));

        // 发消息
        exchangeNotifyProducer.sendNotifySendMessage(NotifySceneEnum.EXCHANGE_NOTIFY.getTemplateCode(), createReqVO.getUserId(), exchangeLog.getExchangeName());

        // 返回
        return exchangeLog.getId();
    }

    @Override
    public void updateExchangeLog(ExchangeLogSaveReqVO updateReqVO) {
        // 校验存在
        validateExchangeLogExists(updateReqVO.getId());
        // 更新
        ExchangeLogDO updateObj = BeanUtils.toBean(updateReqVO, ExchangeLogDO.class);
        exchangeLogMapper.updateById(updateObj);
        ExchangeLogDO exchangeLog = getExchangeLog(updateObj.getId());
        if (updateObj.getDeliverCode() != null) {
            getbackNotifyProducer.sendNotifySendMessage(NotifySceneEnum.EXCHANGE_DELIVER_NOTIFY.getTemplateCode(), exchangeLog.getUserId(), exchangeLog.getExchangeName(), updateReqVO.getDeliverCode());
        }
    }

    @Override
    public void deleteExchangeLog(Long id) {
        // 校验存在
        validateExchangeLogExists(id);
        // 删除
        exchangeLogMapper.deleteById(id);
    }

    @Override
        public void deleteExchangeLogListByIds(List<Long> ids) {
        // 删除
        exchangeLogMapper.deleteByIds(ids);
        }


    private void validateExchangeLogExists(Long id) {
        if (exchangeLogMapper.selectById(id) == null) {
            throw exception(EXCHANGE_LOG_NOT_EXISTS);
        }
    }

    @Override
    public ExchangeLogDO getExchangeLog(Long id) {
        return exchangeLogMapper.selectById(id);
    }

    @Override
    public PageResult<ExchangeLogDO> getExchangeLogPage(ExchangeLogPageReqVO pageReqVO) {
        return exchangeLogMapper.selectPage(pageReqVO);
    }

}
