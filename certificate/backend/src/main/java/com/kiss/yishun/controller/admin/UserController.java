package com.kiss.yishun.controller.admin;

import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.cache.JedisClient;
import com.kiss.yishun.common.Constants;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.Role;
import com.kiss.yishun.entity.User;
import com.kiss.yishun.service.OperLogService;
import com.kiss.yishun.service.RoleService;
import com.kiss.yishun.service.UserService;
import com.kiss.yishun.utils.IpUtils;
import com.kiss.yishun.utils.PageRequestUtils;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api("用户模块")
@RequestMapping("api/usr")
@RestController
public class UserController {

	@Autowired
	private JedisClient redisClient;

	@Autowired private com.kiss.yishun.auth.AdminSessions sessions;

	@Resource
	UserService userService;

	@Resource
	RoleService roleService;

	@Resource
	OperLogService operLogService;

	@ApiOperation("修改密码")
	@RequestMapping(value = "/modifyPwd", method = {RequestMethod.POST})
	@ResponseBody
	public Result modifyPwd(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap,
							HttpServletRequest request) {
		String newpwd = paramMap.get("newpwd");
		String oldpwd = paramMap.get("oldpwd");
		String repwd = paramMap.get("repwd");
		if (StrUtils.isEmpty(oldpwd)) {
			return ResultGenerator.genFailureResult("旧密码不能为空");
		}
		if (StrUtils.isEmpty(newpwd)) {
			return ResultGenerator.genFailureResult("新密码不能为空");
		}
		if (StrUtils.isEmpty(repwd)) {
			return ResultGenerator.genFailureResult("确认密码不能为空");
		}
		if (!newpwd.equals(repwd)) {
			return ResultGenerator.genFailureResult("新密码不一致");
		}
		// 判断原密码是否正确
		// 解密获得userName
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		User user = userService.findByUsername(userName);
		if (!oldpwd.equals(user.getPassword())) {
			return ResultGenerator.genFailureResult("原密码不正确");
		}

		// 更新密码
		user.setPassword(newpwd);
//			user.setUpdatedate(System.currentTimeMillis());
		userService.updateUser(user);

		operLogService.addOperLog("modifyPwd", IpUtils.getIp(request));

		// 重新登录，清除登录信息
		SecurityUtils.getSubject().logout();
		redisClient.del(userName);

		return ResultGenerator.genFailureResult(Constants.UNLOGIN,"修改成功，请重新登录");
	}

    @ApiOperation("退出登录")
    @RequestMapping(value = "/logout", method = {RequestMethod.GET})
	@ResponseBody
    public Result logout(HttpServletRequest request) {
    	String token = SecurityUtils.getSubject().getPrincipal().toString();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		sessions.revoke(token,userName);
		SecurityUtils.getSubject().logout();
		return ResultGenerator.genSuccessResult();
    }

	@ApiOperation("分页查询")
	@RequestMapping(value = "/queryUserList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryUserList(@ApiParam(value = "page",required = true) @RequestParam("page") String page,
									@ApiParam(value = "pageSize",required = true) @RequestParam("pageSize") String pageSize,
									@ApiParam(value = "关键字") @RequestParam(value = "keywords",required = false) String keywords) {
		keywords = StrUtils.isEmpty(keywords)? "":keywords;
		String token = (String) SecurityUtils.getSubject().getPrincipal();
		String userName = JwtUtil.getAccount(token,SecurityConstant.ACCOUNT);
		User user = userService.findByUsername(userName);
		Page<User> pp = userService.findUserPageByKeywords(PageRequestUtils.getPageRequest(page),keywords,user.getRole().getLevel());
		Map<String,Object> result = new HashMap<>();
		List<User> list = pp.getContent();
		if (!list.isEmpty()) {
			for (User u: list) {
				u.setPassword(null);
			}
		}
		result.put("list",list);
		result.put("page",page);
		result.put("totalPage",pp.getTotalPages());
		result.put("totalCount",pp.getTotalElements());
		return ResultGenerator.genSuccessResult(result);
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("批量删除用户")
	@RequestMapping(value = "/deleteUser", method = {RequestMethod.GET})
	@ResponseBody
	public Result deleteUser(@ApiParam(value = "编号",required = true,example = "1,2,3") @RequestParam("id") String id, HttpServletRequest request) {
		String[] ids = id.split(",");
		for (int i=0;i<ids.length;i++) {
			User user = userService.findUserById(Long.parseLong(ids[i]));
			if (user!=null) {
				userService.deleteUser(Long.parseLong(ids[i]));
			}
		}
		operLogService.addOperLog("deleteUser:"+id, IpUtils.getIp(request));
		return ResultGenerator.genSuccessResult();
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("新增用户")
	@RequestMapping(value = "/addUser", method = {RequestMethod.POST})
	@ResponseBody
	public Result addUser(@ApiParam("user") @RequestBody User udto) throws Exception {
		if (StrUtils.isEmpty(udto.getUsername())) {
			return ResultGenerator.genFailureResult("用户名不能为空");
		}
		if (StrUtils.isEmpty(udto.getPassword())) {
			return ResultGenerator.genFailureResult("密码不能为空");
		}
		if (null == udto.getRole() || StrUtils.isEmpty(udto.getRole().getCode())) {
			return ResultGenerator.genFailureResult("角色不能为空");
		}
		Role role = roleService.findRoleByCode(udto.getRole().getCode());
		if (null == role) {
			return ResultGenerator.genFailureResult("角色不存在");
		}
		User user = userService.findByUsername(udto.getUsername());
		if (user == null) {
			udto.setRole(role);
			udto.setDisabled(0);
			userService.addUser(udto);
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("已有相同用户名");
		}
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("更新用户")
	@RequestMapping(value = "/updateUser", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateUser(@ApiParam("user") @RequestBody User udto) {
		if (null == udto.getRole() || StrUtils.isEmpty(udto.getRole().getCode())) {
			return ResultGenerator.genFailureResult("角色不能为空");
		}
		Role role = roleService.findRoleByCode(udto.getRole().getCode());
		if (null == role) {
			return ResultGenerator.genFailureResult("角色不存在");
		}
		long id = udto.getId();
		User user = userService.findUserById(id);
		if (user!=null) {
			user.setRole(role);
			user.setRemark(udto.getRemark());
			userService.updateUser(user);
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("找不到该用户");
		}
	}


	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("开启或禁用用户")
	@RequestMapping(value = "/updateUserStatus", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateUserStatus(@ApiParam("parmsMap") @RequestBody Map<String,Object> parmsMap) {
		int disabled = (int) parmsMap.get("disabled");
		long id = (int) parmsMap.get("id");
		User user = userService.findUserById(id);
		if (user!=null) {
			user.setDisabled(disabled);
			userService.updateUser(user);
			// 禁用要清除redis
			if (disabled == 1) {
				redisClient.del(user.getUsername());
				redisClient.del(user.getUsername()+SecurityConstant.REDIS_USR_EXPIRE);
				redisClient.del(user.getUsername()+SecurityConstant.REDIS_LOGIN_UUID);
			}
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("找不到该用户");
		}
	}

}
