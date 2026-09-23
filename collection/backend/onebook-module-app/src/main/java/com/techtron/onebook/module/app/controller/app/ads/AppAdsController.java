package com.techtron.onebook.module.app.controller.app.ads;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.ads.vo.AdsRespVO;
import com.techtron.onebook.module.app.dal.dataobject.ads.AdsDO;
import com.techtron.onebook.module.app.service.ads.AdsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 广告")
@RestController
@RequestMapping("/app/ads")
@Validated
public class AppAdsController {

    @Resource
    private AdsService adsService;

    @GetMapping("/list")
    @Operation(summary = "获得商品分类列表")
    @PermitAll
    public CommonResult<List<AdsRespVO>> getAdsList() {
        List<AdsDO> list = adsService.getAdsList();
        return success(BeanUtils.toBean(list, AdsRespVO.class));
    }

}