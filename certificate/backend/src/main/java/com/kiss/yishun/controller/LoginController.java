package com.kiss.yishun.controller;

import com.kiss.yishun.auth.JwtToken;
import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.cache.JedisClient;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.User;
import com.kiss.yishun.service.OperLogService;
import com.kiss.yishun.service.UserService;
import com.kiss.yishun.utils.IpUtils;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

@Api(value = "登录模块")
@RestController
@RequestMapping("api/base")
public class LoginController {


    @Autowired
    private JedisClient redisClient;

    @Autowired
    private UserService userService;

    @Autowired private com.kiss.yishun.auth.AdminSessions sessions;

    @Autowired
    private OperLogService operLogService;

    /**
     * 登录
     * @param paramMap
     *
     * @return
     */
    @ApiOperation("登录接口")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Result login(HttpServletRequest request,@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
        String username = paramMap.get("username");
        if (StrUtils.isEmpty(username)) {
            return ResultGenerator.genFailureResult("用户名不能空");
        }
        String password = paramMap.get("password");
        if (StrUtils.isEmpty(password)) {
            return ResultGenerator.genFailureResult("密码不能空");
        }
        User user = userService.findByUsername(username);
        if (user == null || user.getDisabled() == 1) {
            return ResultGenerator.genFailureResult("用户不存在！");
        }
        String encryptPassword = user.getPassword();
        if (!password.equals(encryptPassword)) {
            return ResultGenerator.genFailureResult("账号和密码不一致！");
        }
        long now = System.currentTimeMillis();
        String uuid = IpUtils.getIp(request) +":"+ UUID.randomUUID();
        // 生成token，存入redis
        String token = JwtUtil.sign(username, now, uuid);
        sessions.issue(token,user);
        SecurityUtils.getSubject().login(new JwtToken(token));
        Map<String,String> map = new HashMap<>();
        map.put("userName",user.getUsername());
        map.put("token",token);
        map.put("role",user.getRole().getCode());

        operLogService.addOperLog("login", IpUtils.getIp(request));
        return ResultGenerator.genSuccessResult(map);
    }


}
