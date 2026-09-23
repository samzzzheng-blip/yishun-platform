package com.techtron.onebook.module.pay.api.wallet;

import cn.hutool.core.lang.Assert;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletReduceBalanceReqDTO;
import com.techtron.onebook.module.pay.api.wallet.dto.PayWalletRespDTO;
import com.techtron.onebook.module.pay.dal.dataobject.wallet.PayWalletDO;
import com.techtron.onebook.module.pay.enums.wallet.PayWalletBizTypeEnum;
import com.techtron.onebook.module.pay.service.wallet.PayWalletService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 钱包 API 实现类
 *
 * @author 芋道源码
 */
@Service
public class PayWalletApiImpl implements PayWalletApi {

    @Resource
    private PayWalletService payWalletService;

    @Override
    public void addWalletBalance(PayWalletAddBalanceReqDTO reqDTO) {
        // 创建或获取钱包
        PayWalletDO wallet = payWalletService.getOrCreateWallet(reqDTO.getUserId(), reqDTO.getUserType());
        Assert.notNull(wallet, "钱包({}/{})不存在", reqDTO.getUserId(), reqDTO.getUserType());

        // 增加余额
        PayWalletBizTypeEnum bizType = PayWalletBizTypeEnum.valueOf(reqDTO.getBizType());
        payWalletService.addWalletBalance(wallet.getId(), reqDTO.getBizId(), bizType, reqDTO.getPrice());
    }

    @Override
    public PayWalletRespDTO getOrCreateWallet(Long userId, Integer userType) {
        PayWalletDO wallet = payWalletService.getOrCreateWallet(userId, userType);
        return BeanUtils.toBean(wallet, PayWalletRespDTO.class);
    }

    @Override
    public void reduceWalletBalance(PayWalletReduceBalanceReqDTO reqDTO) {
        // 创建或获取钱包
        PayWalletDO wallet = payWalletService.getOrCreateWallet(reqDTO.getUserId(), reqDTO.getUserType());
        Assert.notNull(wallet, "钱包({}/{})不存在", reqDTO.getUserId(), reqDTO.getUserType());
        // 增加余额
        PayWalletBizTypeEnum bizType = PayWalletBizTypeEnum.valueOf(reqDTO.getBizType());
        payWalletService.reduceWalletBalance(wallet.getId(), reqDTO.getBizId(), bizType, reqDTO.getPrice());
    }

    @Override
    public void freezeWalletBalance(Long userId, Integer userType, Integer price) {
        PayWalletDO wallet = payWalletService.getOrCreateWallet(userId, userType);
        Assert.notNull(wallet, "钱包({}/{})不存在", userId, userType);
        payWalletService.freezePrice(wallet.getId(), price);
    }

    @Override
    public void unfreezeWalletBalance(Long userId, Integer userType, Integer price) {
        PayWalletDO wallet = payWalletService.getOrCreateWallet(userId, userType);
        Assert.notNull(wallet, "钱包({}/{})不存在", userId, userType);
        payWalletService.unfreezePrice(wallet.getId(), price);
    }

    @Override
    public void consumeFrozenBalance(Long userId, Integer userType, Integer price, String bizId) {
        PayWalletDO wallet = payWalletService.getOrCreateWallet(userId, userType);
        Assert.notNull(wallet, "钱包({}/{})不存在", userId, userType);
        payWalletService.consumeFreezePrice(wallet.getId(), price, bizId);
    }

}
