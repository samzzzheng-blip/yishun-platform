package com.techtron.onebook.module.app.job;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.framework.quartz.core.handler.JobHandler;
import com.techtron.onebook.module.app.dal.dataobject.energystone.EnergyStoneDO;
import com.techtron.onebook.module.app.dal.dataobject.storageplan.StoragePlanDO;
import com.techtron.onebook.module.app.dal.mysql.energystone.EnergyStoneMapper;
import com.techtron.onebook.module.app.dal.mysql.storageplan.StoragePlanMapper;
import com.techtron.onebook.module.app.enums.NotifySceneEnum;
import com.techtron.onebook.module.app.service.collection.CollectionService;
import com.techtron.onebook.module.app.service.storageplan.StoragePlanService;
import com.techtron.onebook.module.member.dal.dataobject.user.MemberUserDO;
import com.techtron.onebook.module.member.dal.mysql.user.MemberUserMapper;
import com.techtron.onebook.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StorageExpireRemindJob implements JobHandler {

    @Resource
    private MemberUserMapper memberUserMapper;

    @Resource
    private StoragePlanMapper storagePlanMapper;

    @Resource
    private EnergyStoneMapper energyStoneMapper;

    @Resource
    private CollectionService collectionService;

    @Resource
    private StoragePlanService storagePlanService;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public String execute(String param) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTimeLimit = now.plusDays(3);

        List<MemberUserDO> users = memberUserMapper.selectList(new LambdaQueryWrapperX<MemberUserDO>()
                .isNotNull(MemberUserDO::getExpireTime)
                .le(MemberUserDO::getExpireTime, expireTimeLimit)
                .gt(MemberUserDO::getExpireTime, now));

        int notifyCount = 0;
        for (MemberUserDO user : users) {
            try {
                if (shouldNotify(user)) {
                    sendNotify(user);
                    notifyCount++;
                }
            } catch (Exception e) {
                log.error("[execute][发送存储套餐到期提醒给用户 {} 失败]", user.getId(), e);
            }
        }

        log.info("[execute][定时执行存储套餐到期提醒，共发送 {} 条通知]", notifyCount);
        return String.format("定时执行存储套餐到期提醒，共发送 %s 条通知", notifyCount);
    }

    private boolean shouldNotify(MemberUserDO user) {
        Long userId = user.getId();
        Long storageId = user.getStorageId();

        if (storageId == null || storageId <= 0) {
            return false;
        }

        StoragePlanDO plan = storagePlanMapper.selectById(storageId);
        if (plan == null) {
            return false;
        }

        Integer monthlyPrice = ObjectUtil.defaultIfNull(plan.getMonthlyPrice(), 0);
        if (monthlyPrice <= 0) {
            return false;
        }

        EnergyStoneDO energyStone = energyStoneMapper.selectOne(new LambdaQueryWrapper<EnergyStoneDO>()
                .eq(EnergyStoneDO::getUserId, userId));

        if (energyStone == null || energyStone.getAmount() < monthlyPrice) {
            return true;
        }

        return false;
    }

    private void sendNotify(MemberUserDO user) {
        Long userId = user.getId();

        Map<String, Object> msgMap = new HashMap<>(1);

        long daysUntilExpire = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), user.getExpireTime());
        msgMap.put("daysUntilExpire", (int) daysUntilExpire);

        NotifySendSingleToUserReqDTO message = new NotifySendSingleToUserReqDTO()
                .setUserId(userId)
                .setTemplateCode(NotifySceneEnum.STORAGE_EXPIRE_NOTIFY.getTemplateCode())
                .setTemplateParams(msgMap);

        applicationContext.publishEvent(message);
    }
}