package com.techtron.onebook.module.app.service.storageplan;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.module.app.controller.admin.storageplan.vo.StoragePlanPageReqVO;
import com.techtron.onebook.module.app.controller.admin.storageplan.vo.StoragePlanSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.energystone.EnergyStoneDO;
import com.techtron.onebook.module.app.dal.dataobject.stonerecord.StoneRecordDO;
import com.techtron.onebook.module.app.dal.dataobject.storageplan.StoragePlanDO;
import com.techtron.onebook.module.app.dal.mysql.energystone.EnergyStoneMapper;
import com.techtron.onebook.module.app.dal.mysql.stonerecord.StoneRecordMapper;
import com.techtron.onebook.module.app.dal.mysql.storageplan.StoragePlanMapper;
import com.techtron.onebook.module.app.enums.stonerecord.StoneRecordTypeEnum;
import com.techtron.onebook.module.app.service.collection.CollectionService;
import com.techtron.onebook.module.app.service.stoneexchange.StoneExchangeService;
import com.techtron.onebook.module.member.dal.dataobject.user.MemberUserDO;
import com.techtron.onebook.module.member.dal.mysql.user.MemberUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class StoragePlanServiceImpl implements StoragePlanService {

    @Resource
    private StoragePlanMapper storagePlanMapper;

    @Resource
    private EnergyStoneMapper energyStoneMapper;

    @Resource
    private MemberUserMapper memberUserMapper;

    @Resource
    private CollectionService collectionService;

    @Resource
    private StoneExchangeService stoneExchangeService;

    @Resource
    private StoneRecordMapper stoneRecordMapper;

    @Override
    public Long createStoragePlan(StoragePlanSaveReqVO createReqVO) {
        StoragePlanDO storagePlan = BeanUtils.toBean(createReqVO, StoragePlanDO.class);
        storagePlanMapper.insert(storagePlan);
        return storagePlan.getId();
    }

    @Override
    public void updateStoragePlan(StoragePlanSaveReqVO updateReqVO) {
        validateStoragePlanExists(updateReqVO.getId());
        StoragePlanDO updateObj = BeanUtils.toBean(updateReqVO, StoragePlanDO.class);
        storagePlanMapper.updateById(updateObj);
    }

    @Override
    public void deleteStoragePlan(Long id) {
        validateStoragePlanExists(id);
        storagePlanMapper.deleteById(id);
    }

    private void validateStoragePlanExists(Long id) {
        if (storagePlanMapper.selectById(id) == null) {
            throw exception(STORAGE_PLAN_NOT_EXISTS);
        }
    }

    @Override
    public StoragePlanDO getStoragePlan(Long id) {
        return storagePlanMapper.selectById(id);
    }

    @Override
    public PageResult<StoragePlanDO> getStoragePlanPage(StoragePlanPageReqVO pageReqVO) {
        return storagePlanMapper.selectPage(pageReqVO);
    }

    @Override
    public List<StoragePlanDO> getEnabledStoragePlanList() {
        return storagePlanMapper.selectListAll();
    }

    @Override
    public Long getStoragePlanIdByRealAmount(Integer realAmount) {
        List<StoragePlanDO> planList = getEnabledStoragePlanList();
        if (CollUtil.isEmpty(planList)) {
            return 0L;
        }
        for (StoragePlanDO plan : planList) {
            Integer minCount = ObjectUtil.defaultIfNull(plan.getMinCount(), 0);
            Integer maxCount = ObjectUtil.defaultIfNull(plan.getMaxCount(), 0);
            if (minCount == 0 && maxCount == 0) {
                if (realAmount == 0) {
                    return plan.getId();
                }
            } else if (maxCount == 0) {
                if (realAmount >= minCount) {
                    return plan.getId();
                }
            } else {
                if (realAmount >= minCount && realAmount <= maxCount) {
                    return plan.getId();
                }
            }
        }
        return 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean purchaseStoragePlan(Long userId, Long planId, Integer type) {
        StoragePlanDO plan = storagePlanMapper.selectById(planId);
        if (plan == null) {
            throw exception(STORAGE_PLAN_NOT_EXISTS);
        }
        int basePrice;
        int days;
        if (type == 1) {
            basePrice = plan.getMonthlyPrice();
            days = 30;
        } else if (type == 2) {
            basePrice = plan.getYearlyPrice();
            days = 365;
        } else {
            throw exception(STORAGE_PLAN_TYPE_ERROR);
        }
        MemberUserDO user = memberUserMapper.selectById(userId);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        Integer overDays = ObjectUtil.defaultIfNull(user.getOverDays(), 0);
        int price = (overDays / days + 1) * basePrice;
        EnergyStoneDO energyStone = energyStoneMapper.selectOne(new LambdaQueryWrapper<EnergyStoneDO>()
                .eq(EnergyStoneDO::getUserId, userId)
                .last("FOR UPDATE"));
        if (energyStone == null || energyStone.getAmount() < price) {
            throw exception(ENERGY_STONE_NOT_ENOUGH);
        }
        energyStoneMapper.updateById(new EnergyStoneDO()
                .setId(energyStone.getId())
                .setAmount(energyStone.getAmount() - price));
        StoneRecordDO lastRecord = stoneRecordMapper.selectLatestByUserId(userId);
        Integer lastBalance = lastRecord != null ? lastRecord.getBalance() : 0;
        stoneRecordMapper.insert(new StoneRecordDO()
                .setUserId(userId)
                .setAmount(-price)
                .setType(StoneRecordTypeEnum.BUY_CAPACITY.getType())
                .setBalance(lastBalance - price));
        int addDays = days - overDays % days;
        LocalDateTime newExpireTime;
        if (user.getExpireTime() != null && overDays == 0) {
            newExpireTime = user.getExpireTime().plusDays(addDays);
        } else {
            newExpireTime = LocalDateTime.of(LocalDate.now().plusDays(addDays), LocalTime.MIN);
        }
        memberUserMapper.updateById(new MemberUserDO()
                .setId(userId)
                .setStorageId(planId)
                .setExpireTime(newExpireTime)
                .setOverDays(0)
                .setDebtAmount(0));
        return Boolean.TRUE;
    }

    @Override
    public Integer autoRenewExpireStorage() {
        LocalDateTime now = LocalDateTime.now();
        List<MemberUserDO> expireUsers = memberUserMapper.selectList(new LambdaQueryWrapperX<MemberUserDO>()
                .isNull(MemberUserDO::getExpireTime)
                .or()
                .lt(MemberUserDO::getExpireTime, now)
                .ge(MemberUserDO::getOverDays, 0)
        );

        int successCount = 0;
        int failCount = 0;

        for (MemberUserDO user : expireUsers) {
            try {
                getSelf().processUserStorageRenew(user, now);
                successCount++;
            } catch (Exception e) {
                log.error("[autoRenewExpireStorage][处理用户 {} 自动续费失败]", user.getId(), e);
                failCount++;
            }
        }

        log.info("[autoRenewExpireStorage][处理完成][过期用户总数: {}, 成功: {}, 失败: {}]",
                expireUsers.size(), successCount, failCount);
        return successCount;
    }

    @Transactional(rollbackFor = Exception.class)
    public void processUserStorageRenew(MemberUserDO user, LocalDateTime now) {
        Long userId = user.getId();
        Integer realAmount = collectionService.getRealAmountByUserId(userId);
        Long planId = getStoragePlanIdByRealAmount(realAmount);

        if (planId == null || planId <= 0) {
            return;
        }

        StoragePlanDO plan = storagePlanMapper.selectById(planId);
        if (plan == null) {
            return;
        }

        Integer monthlyPrice = ObjectUtil.defaultIfNull(plan.getMonthlyPrice(), 0);
        if (monthlyPrice <= 0) {
            return;
        }

        Integer currentOverDays = ObjectUtil.defaultIfNull(user.getOverDays(), 0);
        // 小于7天，逾期天数加1，不做其他操作
        if (currentOverDays <7 || currentOverDays >= 30) {
            memberUserMapper.updateById(new MemberUserDO()
                    .setId(userId)
                    .setOverDays(currentOverDays + 1));
            return;
        }
        // 先查询
        EnergyStoneDO energyStone = energyStoneMapper.selectOne(new LambdaQueryWrapper<EnergyStoneDO>()
                        .eq(EnergyStoneDO::getUserId, userId));
        // 不够就兑换
        if (energyStone == null || energyStone.getAmount() < monthlyPrice) {
            stoneExchangeService.batchExchangeByCategory(userId, monthlyPrice);
            // 再查
            energyStone = energyStoneMapper.selectOne(new LambdaQueryWrapper<EnergyStoneDO>()
                    .eq(EnergyStoneDO::getUserId, userId));
        }

        // 还是不够，逾期天数加1
        if (energyStone == null || energyStone.getAmount() < monthlyPrice) {
            memberUserMapper.updateById(new MemberUserDO()
                    .setId(userId)
                    .setOverDays(currentOverDays + 1));
            return;
        }

        energyStoneMapper.updateById(new EnergyStoneDO()
                .setId(energyStone.getId())
                .setAmount(energyStone.getAmount() - monthlyPrice));
        StoneRecordDO lastRecord = stoneRecordMapper.selectLatestByUserId(userId);
        Integer lastBalance = lastRecord != null ? lastRecord.getBalance() : 0;
        stoneRecordMapper.insert(new StoneRecordDO()
                .setUserId(userId)
                .setAmount(-monthlyPrice)
                .setType(StoneRecordTypeEnum.BUY_CAPACITY.getType())
                .setBalance(lastBalance - monthlyPrice));

        LocalDateTime newExpireTime = LocalDateTime.of(LocalDate.now().plusDays(30-currentOverDays), LocalTime.MIN);
        memberUserMapper.updateById(new MemberUserDO()
                .setId(userId)
                .setStorageId(planId)
                .setExpireTime(newExpireTime)
                .setOverDays(0));
    }

    private StoragePlanServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}