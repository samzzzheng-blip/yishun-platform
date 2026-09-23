package com.techtron.onebook.module.app.controller.app.collection;

import cn.hutool.core.util.ObjectUtil;
import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.collection.vo.CollectionSaveReqVO;
import com.techtron.onebook.module.app.controller.app.collection.vo.AppCollectionCreateRespVO;
import com.techtron.onebook.module.app.controller.app.collection.vo.AppCollectionListReqVO;
import com.techtron.onebook.module.app.controller.app.collection.vo.AppCollectionRespVO;
import com.techtron.onebook.module.app.controller.app.collection.vo.AppStorageCapacityRespVO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.storageplan.StoragePlanDO;
import com.techtron.onebook.module.app.service.collection.CollectionService;
import com.techtron.onebook.module.app.service.storageplan.StoragePlanService;
import com.techtron.onebook.module.member.dal.dataobject.user.MemberUserDO;
import com.techtron.onebook.module.member.dal.mysql.user.MemberUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 藏品登记")
@RestController
@RequestMapping("/app/collection")
@Validated
public class AppCollectionController {

    @Resource
    private com.techtron.onebook.module.app.service.inbound.InboundParcelService inboundParcels;
    @Resource
    private com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper collectionMapper;

    private void requireEditable(Long id) {
        CollectionDO c = collectionMapper.selectByIdForUpdate(id);
        if (c == null || !java.util.Objects.equals(c.getUserId(), getLoginUserId())
                || !java.util.List.of(0, 2).contains(c.getStatus())) {
            throw new com.techtron.onebook.framework.common.exception.ServiceException(2_001_020_003, "仅可修改自己的登记中藏品");
        }
        inboundParcels.assertEditable(id);
    }

    private void requireDeletable(Long id) {
        CollectionDO c = collectionMapper.selectByIdForUpdate(id);
        if (!com.techtron.onebook.module.app.service.collection.RegistrationDeletePolicy.allowed(c, getLoginUserId())) {
            throw new com.techtron.onebook.framework.common.exception.ServiceException(2_001_020_003,
                    "仅可删除自己的未交付登记或未关联包裹的驳回记录");
        }
        inboundParcels.assertUnlinkedForDelete(id);
    }

    @Resource
    private CollectionService collectionService;

    @Resource
    private MemberUserMapper memberUserMapper;

    @Resource
    private StoragePlanService storagePlanService;

    @PostMapping("/create")
    @Operation(summary = "创建藏品登记")
    public CommonResult<AppCollectionCreateRespVO> createCollection(@Valid @RequestBody CollectionSaveReqVO createReqVO) {
        Long userId = getLoginUserId();
        createReqVO.setUserId(userId);
        createReqVO.setStatus(0);

        MemberUserDO user = memberUserMapper.selectById(userId);
        if (user != null) {
            Integer overDays = ObjectUtil.defaultIfNull(user.getOverDays(), 0);
            Long storageId = ObjectUtil.defaultIfNull(user.getStorageId(), 0L);

            if (overDays > 7) {
                Integer realAmount = collectionService.getRealAmountByUserId(userId);
                Long planId = storagePlanService.getStoragePlanIdByRealAmount(realAmount + 1);
                int monthlyPrice = 0;
                int yearlyPrice = 0;
                if (planId != null && planId > 0) {
                    var plan = storagePlanService.getStoragePlan(planId);
                    if (plan != null) {
                        int months = (overDays / 30) + 1;
                        int years = (overDays / 365) + 1;
                        monthlyPrice = months * ObjectUtil.defaultIfNull(plan.getMonthlyPrice(), 0);
                        yearlyPrice = years * ObjectUtil.defaultIfNull(plan.getYearlyPrice(), 0);
                    }
                }
                return success(AppCollectionCreateRespVO.needUpgrade(planId, monthlyPrice, yearlyPrice, storageId, user.getExpireTime()));
            }
        }

        Long collectionId = collectionService.createCollection(createReqVO);
        assert user != null;
        return success(AppCollectionCreateRespVO.success(collectionId, user.getExpireTime()));
    }

    @PutMapping("/update")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @Operation(summary = "更新藏品登记")
    public CommonResult<Boolean> updateCollection(@Valid @RequestBody CollectionSaveReqVO updateReqVO) {
        requireEditable(updateReqVO.getId());
        updateReqVO.setUserId(getLoginUserId());
        updateReqVO.setStatus(0);
        collectionService.updateCollection(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @Operation(summary = "删除藏品登记")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteCollection(@RequestParam("id") Long id) {
        requireDeletable(id);
        collectionService.deleteCollection(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除藏品登记")
    public CommonResult<Boolean> deleteCollectionList(@RequestParam("ids") List<Long> ids) {
        ids.stream().sorted().forEach(this::requireDeletable);
        collectionService.deleteCollectionListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得藏品登记")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppCollectionRespVO> getCollection(@RequestParam("id") Long id) {
        CollectionDO collection = collectionService.getCollection(id);
        return success(BeanUtils.toBean(collection, AppCollectionRespVO.class));
    }


    @GetMapping("/list")
    @Operation(summary = "获得商品列表")
    public CommonResult<List<AppCollectionRespVO>> getProductCategoryList(@Valid AppCollectionListReqVO listReqVO) {
        listReqVO.setUserId(getLoginUserId());
        if (java.util.Objects.equals(listReqVO.getStatus(), 1)) {
            return success(warehouseView.list(getLoginUserId()).stream()
                .filter(item -> listReqVO.getCategoryId() == null || java.util.Objects.equals(item.getCategoryId(), listReqVO.getCategoryId()))
                .filter(item -> listReqVO.getCollectionName() == null || listReqVO.getCollectionName().isBlank()
                    || item.getName() != null && item.getName().contains(listReqVO.getCollectionName()))
                .toList());
        }
        List<CollectionDO> list = collectionService.getRealCollectionList(listReqVO);
        return success(BeanUtils.toBean(list, AppCollectionRespVO.class));
    }

    @Resource
    private com.techtron.onebook.module.app.service.collection.WarehouseViewService warehouseView;

    @GetMapping("/storage-capacity")
    @Operation(summary = "获得用户当前空间容量及藏品数量")
    public CommonResult<AppStorageCapacityRespVO> getStorageCapacity() {
        Long userId = getLoginUserId();
        Integer realAmount = ObjectUtil.defaultIfNull(collectionService.getRealAmountByUserId(userId), 0);

        MemberUserDO user = memberUserMapper.selectById(userId);
        Long storageId = ObjectUtil.defaultIfNull(user != null ? user.getStorageId() : null, 0L);

        Integer capacity = 0;
        if (storageId != null && storageId > 0) {
            StoragePlanDO plan = storagePlanService.getStoragePlan(storageId);
            if (plan != null) {
                capacity = ObjectUtil.defaultIfNull(plan.getMaxCount(), 0);
            }
        }

        AppStorageCapacityRespVO resp = new AppStorageCapacityRespVO();
        resp.setStorageId(storageId);
        resp.setCapacity(capacity);
        resp.setRealAmount(realAmount);
        assert user != null;
        resp.setExpireTime(user.getExpireTime());
        return success(resp);
    }


}
