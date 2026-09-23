package com.techtron.onebook.module.app.service.stonerecord;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.stonerecord.vo.StoneRecordPageReqVO;
import com.techtron.onebook.module.app.controller.admin.stonerecord.vo.StoneRecordRespVO;
import com.techtron.onebook.module.app.dal.dataobject.stonerecord.StoneRecordDO;
import com.techtron.onebook.module.app.dal.mysql.stonerecord.StoneRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Service
@Validated
public class StoneRecordServiceImpl implements StoneRecordService {

    @Resource
    private StoneRecordMapper stoneRecordMapper;

    @Override
    public void createStoneRecord(Integer amount, Integer type) {
        Long userId = getLoginUserId();
        StoneRecordDO lastRecord = stoneRecordMapper.selectLatestByUserId(userId);
        Integer lastBalance = lastRecord != null ? lastRecord.getBalance() : 0;
        StoneRecordDO stoneRecord = StoneRecordDO.builder()
                .userId(userId)
                .amount(amount)
                .type(type)
                .balance(lastBalance + amount)
                .build();
        stoneRecordMapper.insert(stoneRecord);
    }

    @Override
    public PageResult<StoneRecordRespVO> getStoneRecordPage(StoneRecordPageReqVO pageReqVO) {
        return stoneRecordMapper.selectPage(pageReqVO);
    }

}
