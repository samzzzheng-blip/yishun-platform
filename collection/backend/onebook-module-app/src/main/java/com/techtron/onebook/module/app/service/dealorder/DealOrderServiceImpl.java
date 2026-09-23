package com.techtron.onebook.module.app.service.dealorder;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.dealorder.vo.DealOrderPageReqVO;
import com.techtron.onebook.module.app.controller.admin.dealorder.vo.DealOrderRespVO;
import com.techtron.onebook.module.app.controller.admin.dealorder.vo.DealOrderSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.dealorder.DealOrderDO;
import com.techtron.onebook.module.app.dal.mysql.dealorder.DealOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.DEAL_ORDER_NOT_EXISTS;

/**
 * 批量交易成交 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class DealOrderServiceImpl implements DealOrderService {

    @Resource
    private DealOrderMapper dealOrderMapper;

    @Override
    public Long createDealOrder(DealOrderSaveReqVO createReqVO) {
        // 插入
        DealOrderDO dealOrder = BeanUtils.toBean(createReqVO, DealOrderDO.class);
        dealOrderMapper.insert(dealOrder);

        // 返回
        return dealOrder.getId();
    }

    @Override
    public void updateDealOrder(DealOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateDealOrderExists(updateReqVO.getId());
        // 更新
        DealOrderDO updateObj = BeanUtils.toBean(updateReqVO, DealOrderDO.class);
        dealOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteDealOrder(Long id) {
        // 校验存在
        validateDealOrderExists(id);
        // 删除
        dealOrderMapper.deleteById(id);
    }

    @Override
        public void deleteDealOrderListByIds(List<Long> ids) {
        // 删除
        dealOrderMapper.deleteByIds(ids);
        }


    private void validateDealOrderExists(Long id) {
        if (dealOrderMapper.selectById(id) == null) {
            throw exception(DEAL_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public DealOrderDO getDealOrder(Long id) {
        return dealOrderMapper.selectById(id);
    }

    @Override
    public PageResult<DealOrderDO> getDealOrderPage(DealOrderPageReqVO pageReqVO) {
        return dealOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public List<DealOrderRespVO> getDealOrderList(Long categoryId) {
        List<DealOrderDO> list = dealOrderMapper.selectList(new LambdaQueryWrapper<DealOrderDO>()
                .eq(DealOrderDO::getCategoryId, categoryId)
                .orderByDesc(DealOrderDO::getCreateTime)
                .last("limit 30")
        );
        return BeanUtils.toBean(list, DealOrderRespVO.class);
    }

}