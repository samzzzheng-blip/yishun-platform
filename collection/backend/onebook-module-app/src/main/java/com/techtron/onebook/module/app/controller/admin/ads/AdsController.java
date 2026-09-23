package com.techtron.onebook.module.app.controller.admin.ads;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.ads.vo.AdsPageReqVO;
import com.techtron.onebook.module.app.controller.admin.ads.vo.AdsRespVO;
import com.techtron.onebook.module.app.controller.admin.ads.vo.AdsSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.ads.AdsDO;
import com.techtron.onebook.module.app.service.ads.AdsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.techtron.onebook.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 广告")
@RestController
@RequestMapping("/app/ads")
@Validated
public class AdsController {

    @Resource
    private AdsService adsService;

    @PostMapping("/create")
    @Operation(summary = "创建广告")
    @PreAuthorize("@ss.hasPermission('app:ads:create')")
    public CommonResult<Long> createAds(@Valid @RequestBody AdsSaveReqVO createReqVO) {
        return success(adsService.createAds(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新广告")
    @PreAuthorize("@ss.hasPermission('app:ads:update')")
    public CommonResult<Boolean> updateAds(@Valid @RequestBody AdsSaveReqVO updateReqVO) {
        adsService.updateAds(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除广告")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('app:ads:delete')")
    public CommonResult<Boolean> deleteAds(@RequestParam("id") Long id) {
        adsService.deleteAds(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除广告")
                @PreAuthorize("@ss.hasPermission('app:ads:delete')")
    public CommonResult<Boolean> deleteAdsList(@RequestParam("ids") List<Long> ids) {
        adsService.deleteAdsListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得广告")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:ads:query')")
    public CommonResult<AdsRespVO> getAds(@RequestParam("id") Long id) {
        AdsDO ads = adsService.getAds(id);
        return success(BeanUtils.toBean(ads, AdsRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得广告分页")
    @PreAuthorize("@ss.hasPermission('app:ads:query')")
    public CommonResult<PageResult<AdsRespVO>> getAdsPage(@Valid AdsPageReqVO pageReqVO) {
        PageResult<AdsDO> pageResult = adsService.getAdsPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AdsRespVO.class));
    }

}