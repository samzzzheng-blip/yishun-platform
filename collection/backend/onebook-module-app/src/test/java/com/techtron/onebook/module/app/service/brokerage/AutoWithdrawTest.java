package com.techtron.onebook.module.app.service.brokerage;

import com.techtron.onebook.module.app.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import com.techtron.onebook.module.app.dal.dataobject.brokerage.BrokerageWithdrawDO;
import com.techtron.onebook.module.app.dal.mysql.brokerage.BrokerageWithdrawMapper;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionSettlementMapper;
import com.techtron.onebook.module.pay.api.transfer.PayTransferApi;
import com.techtron.onebook.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import com.techtron.onebook.module.pay.api.transfer.dto.PayTransferRespDTO;
import com.techtron.onebook.module.pay.api.wallet.PayWalletApi;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;

class AutoWithdrawTest {
    final BrokerageWithdrawMapper mapper = mock(BrokerageWithdrawMapper.class);
    final PayWalletApi wallet = mock(PayWalletApi.class);
    final PayTransferApi transfer = mock(PayTransferApi.class);
    final AuctionSettlementMapper settlements = mock(AuctionSettlementMapper.class);
    final BrokerageWithdrawServiceImpl service = new BrokerageWithdrawServiceImpl();
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service,"brokerageWithdrawMapper",mapper);
        ReflectionTestUtils.setField(service,"payWalletApi",wallet);
        ReflectionTestUtils.setField(service,"payTransferApi",transfer);
        ReflectionTestUtils.setField(service,"auctionSettlementMapper",settlements);
        ReflectionTestUtils.setField(service,"validator",Validation.buildDefaultValidatorFactory().getValidator());
        when(settlements.selectList(any())).thenReturn(List.of());
        doAnswer(i -> { ((BrokerageWithdrawDO)i.getArgument(0)).setId(10L); return 1; })
                .when(mapper).insert(any(BrokerageWithdrawDO.class));
        when(transfer.createTransfer(any())).thenReturn(new PayTransferCreateRespDTO().setId(20L));
    }
    AppBrokerageWithdrawCreateReqVO request() {
        var r = new AppBrokerageWithdrawCreateReqVO();
        r.setType(5); r.setPrice(100); r.setUserAccount("test-openid");
        r.setUserName("测试"); r.setTransferChannelCode("wx_lite"); return r;
    }
    @Test void submitsTransferAutomaticallyAfterDebit() {
        assertEquals(10L,service.createBrokerageWithdraw(1L,request()));
        verify(mapper).insert(argThat((BrokerageWithdrawDO w) -> w.getStatus()==10 && w.getAuditUserId()==null));
        var order = inOrder(wallet,transfer);
        order.verify(wallet).reduceWalletBalance(any());
        order.verify(transfer).createTransfer(argThat(t -> t.getPrice()==100 && "10".equals(t.getMerchantTransferId())));
    }
    @Test void insufficientBalanceNeverStartsTransfer() {
        doThrow(new IllegalStateException("insufficient balance")).when(wallet).reduceWalletBalance(any());
        assertThrows(IllegalStateException.class,()->service.createBrokerageWithdraw(1L,request()));
        verifyNoInteractions(transfer);
    }
    @Test void manualChannelRejectedBeforeDebit() {
        var r=request(); r.setType(3);
        assertThrows(IllegalArgumentException.class,()->service.createBrokerageWithdraw(1L,r));
        verifyNoInteractions(wallet,transfer);
    }
    @Test void zeroAmountRejected() {
        var r=request(); r.setPrice(0);
        assertThrows(RuntimeException.class,()->service.createBrokerageWithdraw(1L,r));
        verifyNoInteractions(wallet,transfer);
    }
    void failedTransfer() {
        when(mapper.selectById(10L)).thenReturn(new BrokerageWithdrawDO().setId(10L).setUserId(1L)
                .setStatus(10).setPayTransferId(20L).setPrice(100).setFeePrice(0).setTransferChannelCode("wx_lite"));
        when(transfer.getTransfer(20L)).thenReturn(new PayTransferRespDTO().setId(20L).setStatus(20)
                .setPrice(100).setMerchantTransferId("10").setChannelCode("wx_lite"));
    }
    @Test void failedTransferRefundsOnlyWinningCallback() {
        failedTransfer();
        when(mapper.updateByIdAndStatus(eq(10L),eq(10),any())).thenReturn(1,0);
        service.updateBrokerageWithdrawTransferred(10L,20L);
        service.updateBrokerageWithdrawTransferred(10L,20L);
        verify(wallet,times(1)).addWalletBalance(argThat(t -> t.getPrice()==100));
    }
    @Test void wrongTransferIdRejectedWithoutRefund() {
        failedTransfer();
        assertThrows(RuntimeException.class,()->service.updateBrokerageWithdrawTransferred(10L,21L));
        verifyNoInteractions(wallet);
    }
}
