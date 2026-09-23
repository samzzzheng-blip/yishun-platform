package com.kiss.yishun.controller.admin;

import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.PokemonBrand;
import com.kiss.yishun.service.SmartBrandService;
import com.kiss.yishun.utils.PageRequestUtils;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Api("宝可梦管理模块")
@RequestMapping("api/admin-smart")
public class SmartBrandManageController {

	@Autowired
	SmartBrandService smartBrandService;

	/**
	 * 分页查询
	 * @param page
	 * @return
	 */
	@ApiOperation("分页查询")
	@RequestMapping(value = "/queryBrandList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryBrandList(@ApiParam(value = "page",required = true) @RequestParam("page") String page,
									@ApiParam(value = "pageSize",required = true) @RequestParam("pageSize") String pageSize,
								 @ApiParam(value = "alias") @RequestParam(value = "alias",required = false) String alias) {
		Page<PokemonBrand> pp = smartBrandService.findAllBrand(alias, PageRequestUtils.getPageRequest(page, pageSize));
		Map<String,Object> result = new HashMap<>();
		result.put("list",pp.getContent());
		result.put("page",page);
		result.put("totalPage",pp.getTotalPages());
		result.put("totalCount",pp.getTotalElements());
		return ResultGenerator.genSuccessResult(result);
	}

	/**
	 * 更新
	 * @param brand
	 *
	 * @return
	 */
	@ApiOperation("更新brand")
	@RequestMapping(value = "/updateBrand", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateBrand(@ApiParam("brand") @RequestBody PokemonBrand brand) {
		if (brand.getId() <= 0) {
			return ResultGenerator.genFailureResult("系列id不能为空");
		}
		PokemonBrand updateBrand = smartBrandService.findBrandById(brand.getId());
		if (updateBrand == null) {
			return ResultGenerator.genFailureResult("系列信息不存在");
		}
		boolean checkExist = false;
		if (!StrUtils.isEmpty(brand.getAlias())) {
			// 转小写
			brand.setAlias(brand.getAlias().toLowerCase());
			if (!brand.getAlias().equals(updateBrand.getAlias())) {
				checkExist = true;
			}
			updateBrand.setAlias(brand.getAlias());
		}
		if (!StrUtils.isEmpty(brand.getBrand())) {
			updateBrand.setBrand(brand.getBrand());
		}
		if (!StrUtils.isEmpty(brand.getTitle())) {
			updateBrand.setTitle(brand.getTitle());
		}
		if (checkExist) {
			PokemonBrand existBrand = smartBrandService.findBrandByAlias(brand.getAlias());
			if (existBrand != null) {
				return ResultGenerator.genFailureResult("已存在相同系列编号，不能重复");
			}
		}
		updateBrand.setUpdatedate(System.currentTimeMillis());
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		updateBrand.setOperator(userName);
		smartBrandService.updateBrand(updateBrand);
		return ResultGenerator.genSuccessResult();
	}

	/**
	 * 新增
	 * @param brand
	 *
	 * @return
	 */
	@ApiOperation("新增brand")
	@RequestMapping(value = "/addBrand", method = {RequestMethod.POST})
	@ResponseBody
	public Result addBrand(@ApiParam("brand") @RequestBody PokemonBrand brand) {
		if (StrUtils.isEmpty(brand.getAlias())) {
			return ResultGenerator.genFailureResult("快捷编号不能为空");
		}
		if (StrUtils.isEmpty(brand.getBrand())) {
			return ResultGenerator.genFailureResult("总系列名称不能为空");
		}
		if (StrUtils.isEmpty(brand.getTitle())) {
			return ResultGenerator.genFailureResult("子系列名称不能为空");
		}
		// 转小写
		brand.setAlias(brand.getAlias().toLowerCase());
		PokemonBrand existBrand = smartBrandService.findBrandByAlias(brand.getAlias());
		if (existBrand != null) {
			return ResultGenerator.genFailureResult("已存在相同系列编号，不能重复");
		}
		brand.setCreatedate(System.currentTimeMillis());
		brand.setUpdatedate(System.currentTimeMillis());
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		brand.setOperator(userName);
		smartBrandService.addBrand(brand);
		return ResultGenerator.genSuccessResult();
	}

}
