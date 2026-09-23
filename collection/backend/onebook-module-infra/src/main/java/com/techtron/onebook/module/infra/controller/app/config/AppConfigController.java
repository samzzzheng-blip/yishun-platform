package com.techtron.onebook.module.infra.controller.app.config;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.infra.controller.admin.config.vo.ConfigPageReqVO;
import com.techtron.onebook.module.infra.controller.admin.config.vo.ConfigRespVO;
import com.techtron.onebook.module.infra.convert.config.ConfigConvert;
import com.techtron.onebook.module.infra.dal.dataobject.config.ConfigDO;
import com.techtron.onebook.module.infra.service.config.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/infra/config")
public class AppConfigController {

    @Resource
    private ConfigService configService;

    @GetMapping("/page")
    @Operation(summary = "获取参数配置分页")
    public CommonResult<PageResult<ConfigRespVO>> getConfigPage(@Valid ConfigPageReqVO pageReqVO) {
        PageResult<ConfigDO> page = configService.getConfigPage(pageReqVO);
        return success(ConfigConvert.INSTANCE.convertPage(page));
    }
}
