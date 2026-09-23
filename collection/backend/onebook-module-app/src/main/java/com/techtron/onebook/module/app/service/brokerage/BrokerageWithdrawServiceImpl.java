package com.techtron.onebook.module.app.service.brokerage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.base.Objects;
import com.techtron.onebook.framework.common.enums.UserTypeEnum;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.json.JsonUtils;
import com.techtron.onebook.framework.common.util.number.MoneyUtils;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.framework.common.util.object.ObjectUtils;
import com.techtron.onebook.module.app.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import com.techtron.onebook.module.app.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionDO;
import com.techtron.onebook.module.app.dal.dataobject.auction.AuctionSettlementDO;
import com.techtron.onebook.module.app.dal.dataobject.brokerage.BrokerageWithdrawDO;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionMapper;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionSettlementMapper;
import com.techtron.onebook.module.app.dal.mysql.brokerage.BrokerageWithdrawMapper;
import com.techtron.onebook.module.app.enums.brokerage.BrokerageWithdrawStatusEnum;
import com.techtron.onebook.module.app.enums.brokerage.BrokerageWithdrawTypeEnum;
import com.techtron.onebook.module.app.service.brokerage.bo.BrokerageWithdrawSummaryRespBO;
import com.techtron.onebook.module.app.service.notify.AppSubscribeMessageService;
import com.techtron.onebook.module.pay.api.transfer.PayTransferApi;
import com.techtron.onebook.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import com.techtron.onebook.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import com.techtron.onebook.module.pay.api.transfer.dto.PayTransferRespDTO;
import com.techtron.onebook.module.pay.api.wallet.PayWalletApi;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletReduceBalanceReqDTO;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletRespDTO;
import com.techtron.onebook.module.pay.enums.PayChannelEnum;
import com.techtron.onebook.module.pay.enums.transfer.PayTransferStatusEnum;
import com.techtron.onebook.module.pay.enums.wallet.PayWalletBizTypeEnum;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.techtron.onebook.framework.common.enums.UserTypeEnum.MEMBER;
import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.framework.common.util.collection.CollectionUtils.convertSet;
import static com.techtron.onebook.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.*;
import static com.techtron.onebook.module.pay.enums.ErrorCodeConstants.PAY_TRANSFER_NOT_FOUND;

/**
 * 提现 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class BrokerageWithdrawServiceImpl implements BrokerageWithdrawService {

    @Resource
    private BrokerageWithdrawMapper brokerageWithdrawMapper;

    @Resource
    private PayTransferApi payTransferApi;
    @Resource
    private PayWalletApi payWalletApi;
    @Resource
    private AuctionMapper auctionMapper;
    @Resource
    private AuctionSettlementMapper auctionSettlementMapper;
    @Resource
    private AppSubscribeMessageService subscribeMessageService;

    @Resource
    private Validator validator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditBrokerageWithdraw(Long adminUserId, Long id, BrokerageWithdrawStatusEnum status, String auditReason, String userIp) {
        // 1.1 校验存在
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        // 1.2 校验状态为审核中
        if (ObjectUtil.notEqual(BrokerageWithdrawStatusEnum.AUDITING.getStatus(), withdraw.getStatus())) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 2. 更新状态
        int updateCount = brokerageWithdrawMapper.updateByIdAndStatus(id, withdraw.getStatus(),
                new BrokerageWithdrawDO().setStatus(status.getStatus()).setAuditReason(auditReason)
                        .setAuditTime(LocalDateTime.now()).setAuditUserId(adminUserId));
        if (updateCount == 0) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 3.1 审批通过的后续处理
        if (BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.equals(status)) {
            auditBrokerageWithdrawSuccess(withdraw);
            // 3.2 审批不通过的后续处理
        } else if (BrokerageWithdrawStatusEnum.AUDIT_FAIL.equals(status)) {
            // 提现申请创建时已经从钱包预扣。审核拒绝必须原路退回，状态条件更新保证只退一次。
            payWalletApi.addWalletBalance(new PayWalletAddBalanceReqDTO()
                    .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.INCOME.getType())
                    .setUserId(withdraw.getUserId()).setBizId("withdraw-reject:" + withdraw.getId())
                    .setPrice(withdraw.getPrice()));
        } else {
            throw new IllegalArgumentException("不支持的提现状态：" + status);
        }
        subscribeMessageService.notifyWithdrawStatus(withdraw.getUserId(), withdraw.getCreateTime(),
                withdraw.getPrice(), status.getName(), auditReason);
    }

    private void auditBrokerageWithdrawSuccess(BrokerageWithdrawDO withdraw) {
        // 情况一：通过 API 转账
        if (BrokerageWithdrawTypeEnum.isApi(withdraw.getType())) {
            createPayTransfer(withdraw);
            return;
        }

        // 情况二：非 API 转账（手动打款）
        brokerageWithdrawMapper.updateByIdAndStatus(withdraw.getId(), BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus(),
                new BrokerageWithdrawDO().setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus()));
    }

    private void createPayTransfer(BrokerageWithdrawDO withdraw) {
        // 1.1 获取基础信息
        String userAccount = withdraw.getUserAccount();
        String userName = withdraw.getUserName();
        String channelCode = null;
        Map<String, String> channelExtras = null;
        if (Objects.equal(withdraw.getType(), BrokerageWithdrawTypeEnum.ALIPAY_API.getType())) {
            channelCode = PayChannelEnum.ALIPAY_PC.getCode();
        } else if (Objects.equal(withdraw.getType(), BrokerageWithdrawTypeEnum.WECHAT_API.getType())) {
            channelCode = withdraw.getTransferChannelCode();
            userAccount = withdraw.getUserAccount();
            // 特殊：微信需要有报备信息
            channelExtras = PayTransferCreateReqDTO.buildWeiXinChannelExtra1000("提现", "提现");
        } else if (Objects.equal(withdraw.getType(), BrokerageWithdrawTypeEnum.WALLET.getType())) {
            PayWalletRespDTO wallet = payWalletApi.getOrCreateWallet(withdraw.getUserId(), UserTypeEnum.MEMBER.getValue());
            Assert.notNull(wallet, "钱包不存在");
            channelCode = PayChannelEnum.WALLET.getCode();
            userAccount = wallet.getId().toString();
        }
        // 1.2 构建请求
        Integer transferPrice = withdraw.getPrice() - withdraw.getFeePrice(); // 计算实际转账金额（提现金额 - 手续费）
        PayTransferCreateReqDTO transferReqDTO = new PayTransferCreateReqDTO()
                .setAppKey("wallet").setChannelCode(channelCode)
                .setMerchantTransferId(withdraw.getId().toString()).setSubject("提现").setPrice(transferPrice)
                .setUserAccount(userAccount).setUserName(userName).setUserIp(getClientIP())
                .setUserId(withdraw.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue()) // 用户信息
                .setChannelExtras(channelExtras);
        // 1.3 发起请求
        PayTransferCreateRespDTO transferRespDTO = payTransferApi.createTransfer(transferReqDTO);

        // 2. 更新提现记录
        brokerageWithdrawMapper.updateById(new BrokerageWithdrawDO().setId(withdraw.getId())
                .setPayTransferId(transferRespDTO.getId()).setTransferChannelCode(channelCode));
    }

    private BrokerageWithdrawDO validateBrokerageWithdrawExists(Long id) {
        BrokerageWithdrawDO withdraw = brokerageWithdrawMapper.selectById(id);
        if (withdraw == null) {
            throw exception(BROKERAGE_WITHDRAW_NOT_EXISTS);
        }
        return withdraw;
    }

    @Override
    public BrokerageWithdrawDO getBrokerageWithdraw(Long id) {
        return brokerageWithdrawMapper.selectById(id);
    }

    @Override
    public PageResult<BrokerageWithdrawDO> getBrokerageWithdrawPage(BrokerageWithdrawPageReqVO pageReqVO) {
        return brokerageWithdrawMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBrokerageWithdraw(Long userId, AppBrokerageWithdrawCreateReqVO createReqVO) {
        // 1.2 校验提现参数
        createReqVO.validate(validator);
        if (!BrokerageWithdrawTypeEnum.isApi(createReqVO.getType())) {
            throw new IllegalArgumentException("当前仅支持自动转账提现，请选择微信零钱等支持的方式");
        }
        if (createReqVO.getPrice() == null || createReqVO.getPrice() <= 0) {
            throw new IllegalArgumentException("提现金额必须大于零");
        }

        AuctionDO sourceAuction = null;
        AuctionSettlementDO sourceSettlement = null;
        if (createReqVO.getAuctionId() == null) {
            // 竞拍款虽然先进入用户钱包，但必须保留商品来源才能进行二次核对。
            // 只要存在新流程产生且未完成打款的竞拍结算，就不允许从普通提现入口绕过来源校验。
            List<AuctionSettlementDO> pendingAuctionSettlements = auctionSettlementMapper.selectList(
                    new LambdaQueryWrapper<AuctionSettlementDO>()
                            .eq(AuctionSettlementDO::getSellerId, userId)
                            .eq(AuctionSettlementDO::getStatus, 1)
                            .isNotNull(AuctionSettlementDO::getConfirmUserId));
            for (AuctionSettlementDO settlement : pendingAuctionSettlements) {
                long successCount = brokerageWithdrawMapper.selectCount(new LambdaQueryWrapper<BrokerageWithdrawDO>()
                        .eq(BrokerageWithdrawDO::getAuctionId, settlement.getAuctionId())
                        .eq(BrokerageWithdrawDO::getStatus,
                                BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus()));
                if (successCount == 0) {
                    throw exception(AUCTION_WITHDRAW_SOURCE_REQUIRED);
                }
            }
        } else {
            sourceAuction = auctionMapper.selectByIdForUpdate(createReqVO.getAuctionId());
            sourceSettlement = auctionSettlementMapper.selectByAuctionId(createReqVO.getAuctionId());
            long pendingCount = brokerageWithdrawMapper.selectCount(new LambdaQueryWrapper<BrokerageWithdrawDO>()
                    .eq(BrokerageWithdrawDO::getAuctionId, createReqVO.getAuctionId())
                    .in(BrokerageWithdrawDO::getStatus,
                            BrokerageWithdrawStatusEnum.AUDITING.getStatus(),
                            BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus(),
                            BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus()));
            if (sourceAuction == null || sourceSettlement == null
                    || !java.util.Objects.equals(sourceAuction.getSellerId(), userId)
                    || !java.util.Objects.equals(sourceSettlement.getSellerId(), userId)
                    || sourceAuction.getStatus() != 3 || sourceSettlement.getStatus() != 1
                    || !java.util.Objects.equals(createReqVO.getPrice(), sourceSettlement.getSellerIncome())
                    || pendingCount > 0) {
                throw exception(AUCTION_WITHDRAW_INVALID);
            }
        }

        // 2.1 竞拍平台手续费已在入钱包前扣除，此处不重复收取
        Integer feePrice = 0;
        // 2.2 创建提现记录
        BrokerageWithdrawDO withdraw = BeanUtils.toBean(createReqVO, BrokerageWithdrawDO.class)
                .setUserId(userId).setFeePrice(feePrice)
                .setStatus(BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus())
                .setAuditReason("系统自动发起转账，无需人工审核")
                .setAuditTime(LocalDateTime.now());
        if (sourceAuction != null) {
            withdraw.setSourceCollectionName(sourceAuction.getCollectionName())
                    .setSourceGrossAmount(sourceSettlement.getGrossAmount())
                    .setSourceFeeRate(sourceSettlement.getFeeRate())
                    .setSourceFeeAmount(sourceSettlement.getFeeAmount());
        }
        brokerageWithdrawMapper.insert(withdraw);

        // 3. 创建用户余额记录
        // 注意，余额是否充足
        PayWalletReduceBalanceReqDTO payWalletReduceBalanceReqDTO = new PayWalletReduceBalanceReqDTO()
                .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.WITHDRAW.getType())
                .setUserId(userId).setBizId(withdraw.getId())
                .setPrice(createReqVO.getPrice());

        payWalletApi.reduceWalletBalance(payWalletReduceBalanceReqDTO);
        // 先校验来源并预扣余额，再使用提现单 ID 发起唯一转账。
        // 不将状态标记为成功；最终结果由支付渠道回调确认。
        createPayTransfer(withdraw);
        return withdraw.getId();
    }

    /**
     * 计算提现手续费
     *
     * @param withdrawPrice 提现金额
     * @param percent       手续费百分比
     * @return 提现手续费
     */
    private Integer calculateFeePrice(Integer withdrawPrice, Integer percent) {
        Integer feePrice = 0;
        if (percent != null && percent > 0) {
            feePrice = MoneyUtils.calculateRatePrice(withdrawPrice, Double.valueOf(percent));
        }
        return feePrice;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBrokerageWithdrawTransferred(Long id, Long payTransferId) {
        // 1.1 校验提现单是否存在
        BrokerageWithdrawDO withdraw = brokerageWithdrawMapper.selectById(id);
        if (withdraw == null) {
            log.error("[updateBrokerageWithdrawTransferred][withdraw({}) payTransfer({}) 不存在提现单，请进行处理！]", id, payTransferId);
            throw exception(BROKERAGE_WITHDRAW_NOT_EXISTS);
        }
        // 1.2 校验提现单已经结束（成功或失败）
        if (ObjectUtils.equalsAny(withdraw.getStatus(), BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus(),
                BrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus())) {
            // 特殊：转账单编号相同，直接返回，说明重复回调
            if (ObjectUtil.equal(withdraw.getPayTransferId(), payTransferId)) {
                log.warn("[updateBrokerageWithdrawTransferred][withdraw({}) 已结束，且转账单编号相同({})，直接返回]", withdraw, payTransferId);
                return;
            }
            // 异常：转账单编号不同，说明转账单编号错误
            log.error("[updateBrokerageWithdrawTransferred][withdraw({}) 转账单不匹配({})，请进行处理！]", withdraw, payTransferId);
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_ID_ERROR);
        }

        // 2. 校验转账单的合法性
        PayTransferRespDTO payTransfer = validateBrokerageTransferStatusCanUpdate(withdraw, payTransferId);

        // 3. 更新提现单状态
        Integer newStatus = PayTransferStatusEnum.isSuccess(payTransfer.getStatus()) ? BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus() :
                PayTransferStatusEnum.isClosed(payTransfer.getStatus()) ? BrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus() : null;
        Assert.notNull(newStatus, "转账单状态({}) 不合法", payTransfer.getStatus());
        int updated = brokerageWithdrawMapper.updateByIdAndStatus(withdraw.getId(), withdraw.getStatus(),
                new BrokerageWithdrawDO().setStatus(newStatus)
                        .setTransferTime(payTransfer.getSuccessTime())
                        .setTransferErrorMsg(payTransfer.getChannelErrorMsg()));
        if (updated == 0) return; // 并发回调已处理，不能重复退款
        if (newStatus != null && newStatus.equals(BrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus())) {
            PayWalletAddBalanceReqDTO payWalletAddBalanceReqDTO = new PayWalletAddBalanceReqDTO()
                    .setUserType(MEMBER.getValue()).setBizType(PayWalletBizTypeEnum.INCOME.getType())
                    .setUserId(withdraw.getUserId()).setBizId("withdraw-transfer-fail:" + withdraw.getId())
                    .setPrice(withdraw.getPrice());

            payWalletApi.addWalletBalance(payWalletAddBalanceReqDTO);
        }
    }

    private PayTransferRespDTO validateBrokerageTransferStatusCanUpdate(BrokerageWithdrawDO withdraw, Long payTransferId) {
        if (withdraw.getPayTransferId() != null && !ObjectUtil.equal(withdraw.getPayTransferId(), payTransferId)) {
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_ID_ERROR);
        }
        // 1. 校验转账单是否存在
        PayTransferRespDTO payTransfer = payTransferApi.getTransfer(payTransferId);
        if (payTransfer == null) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 不存在，请进行处理！]", withdraw.getId(), payTransferId);
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }

        // 2.1 校验转账单已成功或关闭
        if (!PayTransferStatusEnum.isSuccessOrClosed(payTransfer.getStatus())) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 未结束，请进行处理！payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(payTransfer));
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_STATUS_NOT_SUCCESS_OR_CLOSED);
        }
        // 2.2 校验转账金额一致
        Integer expectedTransferPrice = withdraw.getPrice() - withdraw.getFeePrice(); // 转账金额 = 提现金额 - 手续费
        if (ObjectUtil.notEqual(payTransfer.getPrice(), expectedTransferPrice)) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 转账金额不匹配，请进行处理！withdraw 数据是：{}，payTransfer 数据是：{}，期望转账金额：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(withdraw), JsonUtils.toJsonString(payTransfer), expectedTransferPrice);
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_PRICE_NOT_MATCH);
        }
        // 2.3 校验转账订单匹配
        if (ObjectUtil.notEqual(payTransfer.getMerchantTransferId(), withdraw.getId().toString())) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) 转账单不匹配({})，请进行处理！payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(payTransfer));
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_MERCHANT_EXISTS);
        }
        // 2.4 校验转账渠道一致
        if (ObjectUtil.notEqual(payTransfer.getChannelCode(), withdraw.getTransferChannelCode())) {
            log.error("[validateBrokerageTransferStatusCanUpdate][withdraw({}) payTransfer({}) 转账渠道不匹配，请进行处理！withdraw 数据是：{}，payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(withdraw), JsonUtils.toJsonString(payTransfer));
            throw exception(BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_CHANNEL_NOT_MATCH);
        }
        return payTransfer;
    }

    @Override
    public List<BrokerageWithdrawSummaryRespBO> getWithdrawSummaryListByUserId(Collection<Long> userIds,
                                                                               Collection<BrokerageWithdrawStatusEnum> statuses) {
        if (CollUtil.isEmpty(userIds) || CollUtil.isEmpty(statuses)) {
            return Collections.emptyList();
        }
        return brokerageWithdrawMapper.selectCountAndSumPriceByUserIdAndStatus(userIds,
                convertSet(statuses, BrokerageWithdrawStatusEnum::getStatus));
    }

}
