package com.techtron.onebook.module.app.service.collectiontransfer;

import com.techtron.onebook.module.app.controller.app.collectiontransfer.vo.CollectionTransferSaveReqVO;
import com.techtron.onebook.module.app.controller.app.collectiontransfer.vo.TrasnferUserRespVO;
import jakarta.validation.Valid;

/**
 * 转移记录 Service 接口
 *
 * @author 超级管理员
 */
public interface CollectionTransferService {

    /**
     * 创建转移记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTransfer(@Valid CollectionTransferSaveReqVO createReqVO);

    TrasnferUserRespVO searchUser(@Valid String uid);

}