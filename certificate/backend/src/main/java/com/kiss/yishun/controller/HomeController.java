package com.kiss.yishun.controller;

import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.entity.Cartoon;
import com.kiss.yishun.entity.Precious;
import com.kiss.yishun.entity.Rate;
import com.kiss.yishun.service.CartoonService;
import com.kiss.yishun.service.PreciousService;
import com.kiss.yishun.service.RateService;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(value = "前台模块")
@RestController
@RequestMapping("api/base")
public class HomeController {

    @Autowired
    private PreciousService preciousService;
    @Autowired
    private CartoonService cartoonService;
    @Autowired
    private RateService rateService;
    /**
     * 证书查询
     * @param paramMap
     *
     * @return
     */
    @ApiOperation("证书查询接口")
    @RequestMapping(value = "/queryCert", method = RequestMethod.POST)
    @ResponseBody
    public Result queryCert(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
        String certNumber = paramMap.get("certNumber");
        if (StrUtils.isEmpty(certNumber)) {
            return ResultGenerator.genFailureResult("查询码不能为空");
        }
        Rate rate = rateService.findRateByCertNumber(certNumber);
        if (rate == null) {
            Cartoon cartoon = cartoonService.findCartoonByCertNumber(certNumber);
            if (cartoon == null) {
                Precious precious = preciousService.findPreciousByCertNumber(certNumber, 1);
                if (precious != null) {
                    precious.setOperator(null);
                }
                return ResultGenerator.genSuccessResult(precious);
            }
            if (cartoon != null) {
                cartoon.setOperator(null);
            }
            return ResultGenerator.genSuccessResult(cartoon);
        }
        if (rate != null) {
            rate.setOperator(null);
        }
        return ResultGenerator.genSuccessResult(rate);

    }


}
