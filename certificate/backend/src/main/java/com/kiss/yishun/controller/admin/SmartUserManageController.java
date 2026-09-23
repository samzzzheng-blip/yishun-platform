package com.kiss.yishun.controller.admin;

import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.entity.SmartUser;
import com.kiss.yishun.service.SmartUserService;
import com.kiss.yishun.utils.PageRequestUtils;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Api("宝可梦用户管理模块")
@RequestMapping("api/admin-smart")
public class SmartUserManageController {

	@Autowired
	SmartUserService userService;

	/**
	 * 分页查询
	 * @param page
	 * @return
	 */
	@ApiOperation("分页查询")
	@RequestMapping(value = "/queryUserList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryUserList(@ApiParam(value = "page",required = true) @RequestParam("page") String page,
									@ApiParam(value = "pageSize",required = true) @RequestParam("pageSize") String pageSize,
								@ApiParam(value = "phone") @RequestParam(value = "phone",required = false) String phone) {
		if (StrUtils.isEmpty(phone)) {
			phone = "";
		}
		Page<SmartUser> pp = userService.findAll(phone, PageRequestUtils.getPageRequest(page, pageSize));
		Map<String,Object> result = new HashMap<>();
		result.put("list",pp.getContent());
		result.put("page",page);
		result.put("totalPage",pp.getTotalPages());
		result.put("totalCount",pp.getTotalElements());
		return ResultGenerator.genSuccessResult(result);
	}

	/**
	 * 更新
	 * @param user
	 *
	 * @return
	 */
	@ApiOperation("更新用户")
	@RequestMapping(value = "/updateUser", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateUser(@ApiParam("user") @RequestBody SmartUser user) {
		if (user.getId() <= 0) {
			return ResultGenerator.genFailureResult("用户ID不能为空");
		}
		SmartUser oldUser = userService.findUserById(user.getId());
		if (oldUser == null) {
			return ResultGenerator.genFailureResult("用户信息不存在");
		}
		boolean checkExist = false;
		if (!StrUtils.isEmpty(user.getPhone())) {
			if (!user.getPhone().equals(oldUser.getPhone())) {
				checkExist = true;
			}
			oldUser.setPhone(user.getPhone());
		}
		if (!StrUtils.isEmpty(user.getPwd())) {
			oldUser.setPwd(user.getPwd());
		}
		if (user.getDisabled() != null) {
			oldUser.setDisabled(user.getDisabled());
		}
		if (checkExist) {
			SmartUser existUser = userService.findByPhone(user.getPhone());
			if (existUser != null) {
				return ResultGenerator.genFailureResult("已存在相同手机号，不能重复");
			}
		}
		userService.updateUser(oldUser);
		return ResultGenerator.genSuccessResult();
	}
}
