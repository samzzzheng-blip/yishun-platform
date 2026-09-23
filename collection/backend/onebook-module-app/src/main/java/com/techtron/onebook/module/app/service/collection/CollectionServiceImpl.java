package com.techtron.onebook.module.app.service.collection;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.module.app.controller.admin.category.vo.CollectionCategorySaveReqVO;
import com.techtron.onebook.module.app.controller.admin.collection.vo.*;
import com.techtron.onebook.module.app.controller.app.collection.vo.AppCollectionListReqVO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionRecordDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionRecordMapper;
import com.techtron.onebook.module.app.service.category.CollectionCategoryService;
import com.techtron.onebook.module.member.dal.dataobject.user.MemberUserDO;
import com.techtron.onebook.module.member.dal.mysql.user.MemberUserMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.COLLECTION_NOT_EXISTS;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.USER_NOT_EXISTS;

/**
 * 藏品登记 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class CollectionServiceImpl implements CollectionService {

    @Resource
    private com.techtron.onebook.module.app.service.inbound.InboundParcelService inboundParcels;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private CollectionRecordMapper collectionRecordMapper;

    @Resource
    private MemberUserMapper memberUserMapper;
    @Autowired
    private CollectionCategoryService collectionCategoryService;

    @Override
    public Long createCollection(CollectionSaveReqVO createReqVO) {
        // 插入
        CollectionDO collection = BeanUtils.toBean(createReqVO, CollectionDO.class);
        collection.setRealStock(createReqVO.getStock());
        collectionMapper.insert(collection);

        // 返回
        return collection.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCollection(CollectionAdminSaveReqVO createReqVO) {
        MemberUserDO memberUserDO = memberUserMapper.selectByMobile(createReqVO.getUser());
        if (memberUserDO == null) {
            throw exception(USER_NOT_EXISTS);
        }
        if (createReqVO.getCustomCategory() != null && !createReqVO.getCustomCategory().isEmpty()) {
            long categoryId = collectionCategoryService.createCollectionCategory(
                    new CollectionCategorySaveReqVO().setName(createReqVO.getCustomCategory())
                            .setUserId(memberUserDO.getId())
            );
            createReqVO.setCategoryId(categoryId);
            createReqVO.setCategoryName(createReqVO.getCustomCategory());
        }
        // 插入
        CollectionDO collection = BeanUtils.toBean(createReqVO, CollectionDO.class);
        collection.setRealStock(createReqVO.getStock());
        collection.setUserId(memberUserDO.getId());
        collection.setStatus(1);
        collectionMapper.insert(collection);

        // 新增变更记录
        saveCollectionRecord(memberUserDO.getId(), collection.getCategoryId(), collection.getStock(), 2);

        // 返回
        return collection.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCollection(CollectionSaveReqVO updateReqVO) {
        collectionMapper.selectByIdForUpdate(updateReqVO.getId());
        inboundParcels.assertEditable(updateReqVO.getId());
        // 校验存在
        validateCollectionExists(updateReqVO.getId());
        // 更新
        CollectionDO updateObj = BeanUtils.toBean(updateReqVO, CollectionDO.class);
        updateObj.setRealStock(updateReqVO.getStock());
        collectionMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditCollection(CollectionUpdateReqVO updateReqVO) {
        inboundParcels.lockForAudit(updateReqVO.getId());
        // 校验存在
        CollectionDO collection = collectionMapper.selectByIdForUpdate(updateReqVO.getId());
        if (collection == null) {
            throw exception(COLLECTION_NOT_EXISTS);
        }
        if (java.util.Objects.equals(collection.getStatus(), updateReqVO.getStatus())) return;
        if (!java.util.Objects.equals(collection.getStatus(), 0)
                || updateReqVO.getStatus() == null || !java.util.List.of(1, 2).contains(updateReqVO.getStatus())) {
            throw new com.techtron.onebook.framework.common.exception.ServiceException(2_001_020_002, "仅可审核待审核藏品");
        }
        inboundParcels.beforeAudit(collection.getId(), updateReqVO.getStatus());
        // 审核不改变登记数量，库存变更记录与审核在同一事务提交。
        CollectionDO updateObj = new CollectionDO().setId(collection.getId()).setStatus(updateReqVO.getStatus());
        collectionMapper.updateById(updateObj);
        // 审核通过时新增变更记录
        if (updateReqVO.getStatus() != null && updateReqVO.getStatus() == 1) {
            saveCollectionRecord(collection.getUserId(), collection.getCategoryId(),
                    collection.getStock(), 1);
        }
        inboundParcels.afterAudit(collection.getId(),
                com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId(), updateReqVO.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCollection(Long id) {
        collectionMapper.selectByIdForUpdate(id);
        inboundParcels.assertEditable(id);
        // 校验存在
        CollectionDO collection = collectionMapper.selectById(id);
        if (collection == null) {
            throw exception(COLLECTION_NOT_EXISTS);
        }
        // 新增变更记录
        saveCollectionRecord(collection.getUserId(), collection.getCategoryId(), collection.getStock(), 3);
        // 删除
        collectionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
        public void deleteCollectionListByIds(List<Long> ids) {
        ids.stream().sorted().forEach(id -> {
            collectionMapper.selectByIdForUpdate(id);
            inboundParcels.assertEditable(id);
        });
        // 删除
        collectionMapper.deleteByIds(ids);
        }


    private void validateCollectionExists(Long id) {
        if (collectionMapper.selectById(id) == null) {
            throw exception(COLLECTION_NOT_EXISTS);
        }
    }

    private void saveCollectionRecord(Long userId, Long categoryId, Integer stock, Integer type) {
        int s = stock != null ? stock : 0;
        int amount;
        if (type == 3 || type == 4 || type == 6 || type == 7) {
            amount = -s;
        } else {
            amount = s;
        }
        CollectionRecordDO record = CollectionRecordDO.builder()
                .userId(userId)
                .categoryId(categoryId)
                .amount(amount)
                .type(type)
                .build();
        collectionRecordMapper.insert(record);
    }

    @Override
    public CollectionDO getCollection(Long id) {
        return collectionMapper.selectById(id);
    }

    @Override
    public PageResult<CollectionRespVO> getCollectionPage(CollectionPageReqVO pageReqVO) {
        return collectionMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CollectionDO>  getCollectionList(AppCollectionListReqVO listReqVO) {
        return collectionMapper.selectList(new LambdaQueryWrapperX<CollectionDO>()
                .eq(CollectionDO::getUserId, listReqVO.getUserId())
                .eqIfPresent(CollectionDO::getCategoryId, listReqVO.getCategoryId())
                .eqIfPresent(CollectionDO::getStatus, listReqVO.getStatus())
                .neIfPresent(CollectionDO::getStatus, listReqVO.getNoEqStatus())
                .inIfPresent(CollectionDO::getId, listReqVO.getCollectionIds())
                .gt(CollectionDO::getStock, 0)
        );
    }

    public List<CollectionDO>  getRealCollectionList(AppCollectionListReqVO listReqVO) {
        return collectionMapper.selectList(new LambdaQueryWrapperX<CollectionDO>()
                .eq(CollectionDO::getUserId, listReqVO.getUserId())
                .eqIfPresent(CollectionDO::getCategoryId, listReqVO.getCategoryId())
                .likeIfPresent(CollectionDO::getName, listReqVO.getCollectionName())
                .eqIfPresent(CollectionDO::getStatus, listReqVO.getStatus())
                .neIfPresent(CollectionDO::getStatus, listReqVO.getNoEqStatus())
                .gt(CollectionDO::getStock, 0)
                .gt(CollectionDO::getRealStock, 0)
        );
    }

    @Override
    public Integer getRealAmountByUserId(Long userId) {
        return collectionMapper.selectRealAmountByUserId(userId);
    }

    @Override
    public PageResult<CollectionRecordRespVO> getCollectionRecordPage(CollectionRecordPageReqVO pageReqVO) {
        return collectionRecordMapper.selectPage(pageReqVO);
    }

}
