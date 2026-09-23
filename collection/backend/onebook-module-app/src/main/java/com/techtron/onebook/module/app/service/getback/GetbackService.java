package com.techtron.onebook.module.app.service.getback;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackPageReqVO;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackRespVO;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackSaveReqVO;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackUpdateReqVO;
import com.techtron.onebook.module.app.dal.dataobject.getback.GetbackDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 取回 Service 接口
 *
 * @author 超级管理员
 */
public interface GetbackService {

    /**
     * 创建取回
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createGetback(@Valid GetbackSaveReqVO createReqVO);

    /**
     * 更新取回
     *
     * @param updateReqVO 更新信息
     */
    void updateGetback(@Valid GetbackUpdateReqVO updateReqVO);

    /**
     * 取消尚未发货的取回申请，并恢复关联藏品库存。
     *
     * @param id 取回申请编号
     */
    void cancelGetback(Long id);

    /**
     * 删除取回
     *
     * @param id 编号
     */
    void deleteGetback(Long id);

    /**
    * 批量删除取回
    *
    * @param ids 编号
    */
    void deleteGetbackListByIds(List<Long> ids);

    /**
     * 获得取回
     *
     * @param id 编号
     * @return 取回
     */
    GetbackDO getGetback(Long id);

    /**
     * 获得取回分页
     *
     * @param pageReqVO 分页查询
     * @return 取回分页
     */
    PageResult<GetbackRespVO> getGetbackPage(GetbackPageReqVO pageReqVO);

    GetbackDO getDeliver(Long collectionId, Long userId);

    List<GetbackRespVO> getGetbackListByUserId(Long userId);

}
