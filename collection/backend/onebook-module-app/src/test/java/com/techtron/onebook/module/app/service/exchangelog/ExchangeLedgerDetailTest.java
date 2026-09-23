package com.techtron.onebook.module.app.service.exchangelog;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.module.app.controller.admin.exchangelog.vo.ExchangeLogSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.energystone.EnergyStoneDO;
import com.techtron.onebook.module.app.dal.dataobject.exchange.ExchangeDO;
import com.techtron.onebook.module.app.dal.dataobject.exchangelog.ExchangeLogDO;
import com.techtron.onebook.module.app.dal.dataobject.stonerecord.StoneRecordDO;
import com.techtron.onebook.module.app.dal.mysql.energystone.EnergyStoneMapper;
import com.techtron.onebook.module.app.dal.mysql.exchange.ExchangeMapper;
import com.techtron.onebook.module.app.dal.mysql.exchangelog.ExchangeLogMapper;
import com.techtron.onebook.module.app.dal.mysql.stonerecord.StoneRecordMapper;
import com.techtron.onebook.module.app.mq.producer.ExchangeNotifyProducer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExchangeLedgerDetailTest {
    @Test void snapshotsServerNameAndLinksCreatedOrder() {
        var service = new ExchangeLogServiceImpl();
        var goods = mock(ExchangeMapper.class);
        var energy = mock(EnergyStoneMapper.class);
        var orders = mock(ExchangeLogMapper.class);
        var ledger = mock(StoneRecordMapper.class);
        ReflectionTestUtils.setField(service, "exchangeMapper", goods);
        ReflectionTestUtils.setField(service, "energyStoneMapper", energy);
        ReflectionTestUtils.setField(service, "exchangeLogMapper", orders);
        ReflectionTestUtils.setField(service, "stoneRecordMapper", ledger);
        ReflectionTestUtils.setField(service, "exchangeNotifyProducer", mock(ExchangeNotifyProducer.class));
        when(goods.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new ExchangeDO().setId(3L).setName("生天目仁美").setStock(2).setAmount(100));
        when(energy.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new EnergyStoneDO().setId(4L).setAmount(200));
        when(ledger.selectLatestByUserId(42L)).thenReturn(new StoneRecordDO().setBalance(200));
        doAnswer(invocation -> { ((ExchangeLogDO) invocation.getArgument(0)).setId(9L); return 1; }).when(orders).insert(any(ExchangeLogDO.class));
        var req = new ExchangeLogSaveReqVO();
        req.setUserId(42L); req.setExchangeId(3L); req.setExchangeName("客户端错误名称"); req.setAmount(10);
        assertEquals(9L, service.createExchangeLog(req));
        var record = ArgumentCaptor.forClass(StoneRecordDO.class);
        verify(ledger).insert(record.capture());
        assertEquals("生天目仁美", record.getValue().getExchangeName());
        assertEquals(9L, record.getValue().getExchangeLogId());
        assertEquals(1, record.getValue().getExchangeQuantity());
        assertEquals(-100, record.getValue().getAmount());
        assertEquals(100, record.getValue().getBalance());
    }
}
