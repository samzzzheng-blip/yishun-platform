package com.kiss.yishun.controller.smartdetect;

import com.kiss.yishun.auth.JwtToken;
import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.cache.JedisClient;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.constant.SmartConstant;
import com.kiss.yishun.entity.SmartUser;
import com.kiss.yishun.entity.enums.SmartPhoneCodeEnum;
import com.kiss.yishun.service.SmartUserService;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Api("Smart用户模块")
@RequestMapping("api/smart/usr")
@RestController
public class SmartUserController {

	@Autowired
	private JedisClient redisClient;

	@Resource
	SmartUserService userService;

	@ApiOperation("获取个人信息")
	@RequestMapping(value = "/getUserInfo", method = {RequestMethod.POST})
	@ResponseBody
	public Result getUserInfo() {
		String token = (String) SecurityUtils.getSubject().getPrincipal();
		Long userId = JwtUtil.getSmartUserId(token, SecurityConstant.ACCOUNT);
		SmartUser smartUser = userService.findById(userId);
		// 隐藏密码
		if (smartUser != null) {
			smartUser.setPwd("");
		}
		return ResultGenerator.genSuccessResult(smartUser);
	}

	@ApiOperation("修改密码")
	@RequestMapping(value = "/modifyPwd", method = {RequestMethod.POST})
	@ResponseBody
	public Result modifyPwd(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap,
							HttpServletResponse response) {
		String newpwd = paramMap.get("newpwd");
		String oldpwd = paramMap.get("oldpwd");
		if (StrUtils.isEmpty(newpwd)) {
			return ResultGenerator.genFailureResult("新密码不能为空");
		}
		if (newpwd.length() < 6) {
			return ResultGenerator.genFailureResult("新密码长度不能少于6位");
		}
		if (StrUtils.isEmpty(oldpwd)) {
			return ResultGenerator.genFailureResult("原密码不能为空");
		}
		if (oldpwd.length() < 6) {
			return ResultGenerator.genFailureResult("旧密码长度不能少于6位");
		}
		// 判断原密码是否正确
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		Long userId = JwtUtil.getSmartUserId(token, SecurityConstant.ACCOUNT);
		SmartUser user = userService.findById(userId);
		if (!oldpwd.equals(user.getPwd())) {
			return ResultGenerator.genFailureResult("原密码不正确");
		}
		// 更新密码
		user.setPwd(newpwd);
		userService.updateUser(user);
		return ResultGenerator.genSuccessResult();
	}

	@ApiOperation("修改头像")
	@RequestMapping(value = "/modifyHead", method = {RequestMethod.POST})
	@ResponseBody
	public Result modifyHead(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap,
								 HttpServletResponse response) {
		String head = paramMap.get("head");
		if (StrUtils.isEmpty(head)) {
			return ResultGenerator.genFailureResult("头像不能为空");
		}
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		Long userId = JwtUtil.getSmartUserId(token, SecurityConstant.ACCOUNT);
		SmartUser user = userService.findById(userId);

		// 更新头像
		user.setHead(head);
		userService.updateUser(user);

		return ResultGenerator.genSuccessResult();
	}

	@ApiOperation("修改昵称")
	@RequestMapping(value = "/modifyNick", method = {RequestMethod.POST})
	@ResponseBody
	public Result modifyNick(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap,
							 HttpServletResponse response) {
		String nick = paramMap.get("nick");
		if (StrUtils.isEmpty(nick)) {
			return ResultGenerator.genFailureResult("昵称不能为空");
		}
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		Long userId = JwtUtil.getSmartUserId(token, SecurityConstant.ACCOUNT);
		SmartUser user = userService.findById(userId);
		user.setNick(nick);
		userService.updateUser(user);

		return ResultGenerator.genSuccessResult();
	}

	@ApiOperation("修改手机号")
	@RequestMapping(value = "/modifyPhone", method = {RequestMethod.POST})
	@ResponseBody
	public Result modifyPhone(HttpServletRequest request, @ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
		String phone = paramMap.get("phone");
		if (StrUtils.isEmpty(phone)) {
			return ResultGenerator.genFailureResult("手机号不能为空");
		}
		String verifyCode = paramMap.get("verifyCode");
		if (StrUtils.isEmpty(verifyCode)) {
			return ResultGenerator.genFailureResult("验证码不能为空");
		}
		String phoneCodeName = String.format(SmartConstant.SMART_PHONE_CODE_NAME, phone, SmartPhoneCodeEnum.MODIFY_PHONE.getCode());
		String redisVerifyCode = redisClient.get(phoneCodeName, 0);
		if (redisVerifyCode == null) {
			return ResultGenerator.genFailureResult("验证码已失效");
		}
		if (!verifyCode.equals(redisVerifyCode)) {
			return ResultGenerator.genFailureResult("验证码不正确");
		}
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		if (userName == null) {
			throw new AuthenticationException("token已过期!");
		}
		// 系统是否已存在手机
		SmartUser targetUser = userService.findByPhone(phone);
		if (targetUser != null) {
			return ResultGenerator.genFailureResult("该手机号已注册");
		}
		Long userId = JwtUtil.getSmartUserId(token, SecurityConstant.ACCOUNT);
		SmartUser user = userService.findUserById(userId);
		// 更新手机号
		user.setPhone(phone);
		userService.updateUser(user);

		SecurityUtils.getSubject().login(new JwtToken(token));
		Map<String,String> map = new HashMap<>();
		map.put("phone", phone);
		return ResultGenerator.genSuccessResult(map);
	}

    @ApiOperation("退出登录")
    @RequestMapping(value = "/logout", method = {RequestMethod.GET})
	@ResponseBody
    public Result logout() {
    	String token = SecurityUtils.getSubject().getPrincipal().toString();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		redisClient.del(userName);
		redisClient.del(userName + SecurityConstant.REDIS_USR_EXPIRE);
		redisClient.del(userName + SecurityConstant.REDIS_LOGIN_UUID);
		SecurityUtils.getSubject().logout();
        return ResultGenerator.genSuccessResult();
    }


}
