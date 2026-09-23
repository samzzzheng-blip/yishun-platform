package com.techtron.onebook.module.app.service.collectiontransfer;

import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategorySaveReqVO;
import com.techtron.onebook.module.app.controller.app.collectiontransfer.vo.CollectionTransferSaveReqVO;
import com.techtron.onebook.module.app.controller.app.collectiontransfer.vo.TrasnferUserRespVO;
import com.techtron.onebook.module.app.convert.collectiontransfer.CollectionTransferConvert;
import com.techtron.onebook.module.app.dal.dataobject.category.CollectionCategoryDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.collectiontransfer.CollectionTransferDO;
import com.techtron.onebook.module.app.dal.dataobject.collectiontransfer.CollectionTransferItemDO;
import com.techtron.onebook.module.app.dal.mysql.category.CollectionCategoryMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.collectiontransfer.CollectionTransferItemMapper;
import com.techtron.onebook.module.app.dal.mysql.collectiontransfer.CollectionTransferMapper;
import com.techtron.onebook.module.app.enums.NotifySceneEnum;
import com.techtron.onebook.module.app.mq.producer.TransferNotifyProducer;
import com.techtron.onebook.module.app.service.category.CollectionCategoryService;
import com.techtron.onebook.module.member.api.user.MemberUserApi;
import com.techtron.onebook.module.member.api.user.dto.MemberUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.*;

/**
 * 转移记录 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class CollectionTransferServiceImpl implements CollectionTransferService {

    @Resource
    private CollectionTransferMapper transferMapper;
    @Autowired
    private MemberUserApi memberUserApi;
    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private CollectionCategoryMapper collectionCategoryMapper;
    @Autowired
    private CollectionCategoryService collectionCategoryService;

    @Resource
    private TransferNotifyProducer transferNotifyProducer;

    @Resource
    private CollectionTransferItemMapper collectionTransferItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransfer(CollectionTransferSaveReqVO createReqVO) {
        if (Objects.equals(createReqVO.getToUserId(), createReqVO.getUserId())) {
            throw exception(CANT_TRANSFER_SELF);
        }
        List<CollectionDO> list = collectionMapper.selectForUpdate(createReqVO.getCollectionIds(), createReqVO.getUserId());

        if (list == null || list.isEmpty()) {
            throw exception(TRANSFER_COLLECTION_NOT_EXISTS);
        }
        // 插入
        CollectionTransferDO transfer = BeanUtils.toBean(createReqVO, CollectionTransferDO.class);
        transferMapper.insert(transfer);
        long id = transfer.getId();
        List<CollectionTransferItemDO> collectionTransferItemList = CollectionTransferConvert.INSTANCE.convert(list);
        collectionTransferItemList.forEach(item -> item.setTransferId(id));
        collectionTransferItemMapper.insertBatch(collectionTransferItemList);

        // 增加
        long userId = createReqVO.getToUserId();
        Long categoryId = list.get(0).getCategoryId();
        CollectionCategoryDO collectionCategoryDO = collectionCategoryService.getCollectionCategory(categoryId);
        if (collectionCategoryDO.getUserId() != 0) {
            CollectionCategoryDO newCategory = new CollectionCategoryDO()
                    .setName(collectionCategoryDO.getName())
                    .setCopyId(collectionCategoryDO.getCopyId())
                    .setPicUrl(collectionCategoryDO.getPicUrl())
                    .setUserId(userId);
            categoryId = collectionCategoryService.createCollectionCategory(BeanUtils.toBean(newCategory, CollectionCategorySaveReqVO.class));
        }

        Long finalCategoryId = categoryId;
        List<CollectionDO> updateList = list.stream().map(collectionDO -> new CollectionDO()
                .setId(collectionDO.getId())
                .setCategoryId(finalCategoryId)
                .setUserId(userId)
        ).toList();
        collectionMapper.updateBatch(updateList);


        // 发消息
        MemberUserRespDTO toUser = memberUserApi.getUser(createReqVO.getToUserId());
        MemberUserRespDTO user = memberUserApi.getUser(createReqVO.getUserId());
        String toUserName = toUser.getNickname() + '（' + toUser.getMobile() + ')';
        String userName = user.getNickname() + '（' + user.getMobile() + ')';
        String names = list.stream().map(CollectionDO::getName).collect(Collectors.joining(","));
        transferNotifyProducer.sendNotifySendMessage(NotifySceneEnum.GET_TRANSFER_NOTIFY.getTemplateCode(), createReqVO.getToUserId(), userName, names);
        transferNotifyProducer.sendNotifySendMessage(NotifySceneEnum.TRANSFER_NOTIFY.getTemplateCode(), createReqVO.getUserId(), toUserName, names);

        // 返回
        return id;
    }

    @Override
    public TrasnferUserRespVO searchUser(String uid) {
        MemberUserRespDTO userInfo = memberUserApi.getUserByMobile(uid);
        if (Objects.equals(userInfo.getId(), getLoginUserId())) {
            throw exception(CANT_TRANSFER_SELF);
        }
        return BeanUtils.toBean(userInfo, TrasnferUserRespVO.class);
    }





}