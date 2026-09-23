package com.techtron.onebook.module.app.controller.app.stonerecord;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.app.stonerecord.vo.AppStoneRecordPageReqVO;
import com.techtron.onebook.module.app.controller.app.stonerecord.vo.AppStoneRecordRespVO;
import com.techtron.onebook.module.app.dal.mysql.stonerecord.StoneRecordMapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@RestController
@RequestMapping("/app/stone-record")
@Validated
public class AppStoneRecordController {
    @Resource
    private StoneRecordMapper stoneRecordMapper;

    @GetMapping("/page")
    public CommonResult<PageResult<AppStoneRecordRespVO>> page(@Valid AppStoneRecordPageReqVO req) {
        // 身份只取登录态，不接受客户端指定用户；响应不包含手机号等个人信息。
        return success(BeanUtils.toBean(stoneRecordMapper.selectUserPage(getLoginUserId(), req),
                AppStoneRecordRespVO.class));
    }
}
