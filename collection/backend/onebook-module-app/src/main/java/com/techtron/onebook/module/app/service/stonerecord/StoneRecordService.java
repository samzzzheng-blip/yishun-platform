package com.techtron.onebook.module.app.service.stonerecord;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.stonerecord.vo.StoneRecordPageReqVO;
import com.techtron.onebook.module.app.controller.admin.stonerecord.vo.StoneRecordRespVO;

public interface StoneRecordService {

    void createStoneRecord(Integer amount, Integer type);

    PageResult<StoneRecordRespVO> getStoneRecordPage(StoneRecordPageReqVO pageReqVO);

}
