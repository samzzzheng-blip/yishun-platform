package com.kiss.yishun.controller.smartdetect;

import com.kiss.yishun.auth.JwtToken;
import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.cache.JedisClient;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.config.SmsConfig;
import com.kiss.yishun.entity.enums.SmartPhoneCodeEnum;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.constant.SmartConstant;
import com.kiss.yishun.entity.SmartSms;
import com.kiss.yishun.entity.SmartUser;
import com.kiss.yishun.service.SmartSmsService;
import com.kiss.yishun.service.SmartUserService;
import com.kiss.yishun.utils.CheckUtils;
import com.kiss.yishun.utils.StrUtils;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Api(value = "Smart登录模块")
@RestController
@RequestMapping("api/smart/base")
@Log4j2
public class SmartLoginController {


    @Autowired
    private JedisClient redisClient;

    @Autowired
    private SmartUserService userService;

    @Autowired
    private SmsConfig smsConfig;

    @Autowired
    private SmartSmsService smsService;

    /**
     * 登录
     * @param paramMap
     *
     * @return
     */
    @ApiOperation("smart登录接口")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Result login(HttpServletRequest request,@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
        String phone = paramMap.get("phone");
        if (StrUtils.isEmpty(phone)) {
            return ResultGenerator.genFailureResult("手机号不能空");
        }
        String password = paramMap.get("password");
        if (StrUtils.isEmpty(password)) {
            return ResultGenerator.genFailureResult("密码不能空");
        }
        SmartUser user = userService.findByPhone(phone);
        if (user == null || user.getDisabled() != null && user.getDisabled() == 1) {
            return ResultGenerator.genFailureResult("用户不存在！");
        }
        String encryptPassword = user.getPwd();
        if (!password.equals(encryptPassword)) {
            return ResultGenerator.genFailureResult("密码不正确！");
        }
        long now = System.currentTimeMillis();
        user.setUpdatedate(now);
        userService.updateUser(user);
        String uuid = request.getRemoteAddr()+":"+ UUID.randomUUID();
        String userName = SmartConstant.SMART_USER+user.getId();
        // 生成token，存入redis
        String token = JwtUtil.sign(userName, now, uuid);
        redisClient.set(userName, token, 0);
        redisClient.expire(userName, SecurityConstant.TOKEN_EXPIRE_TIME/1000,0);
        redisClient.set(userName+SecurityConstant.REDIS_USR_EXPIRE,String.valueOf(now+SecurityConstant.TOKEN_EXPIRE_TIME),0);
        redisClient.set(userName+SecurityConstant.REDIS_LOGIN_UUID, uuid, 0);
        SecurityUtils.getSubject().login(new JwtToken(token));
        Map<String,String> map = new HashMap<>();
        map.put("phone",user.getPhone());
        map.put("token",token);
        map.put("head", user.getHead());
        map.put("nick", user.getNick());
        return ResultGenerator.genSuccessResult(map);
    }

    /**
     * 发送验证码
     * @param paramMap
     *
     * @return
     */
    @ApiOperation("发送验证码接口")
    @RequestMapping(value = "/sendCode", method = RequestMethod.POST)
    @ResponseBody
    public Result sendCode(HttpServletRequest request,@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
        String scene = paramMap.get("scene");
        if (StrUtils.isEmpty(scene)) {
            return ResultGenerator.genFailureResult("短信场景不能空");
        }
        String phone = paramMap.get("phone");
        if (StrUtils.isEmpty(phone)) {
            return ResultGenerator.genFailureResult("手机号不能空");
        }
        phone = phone.trim();
        if (!CheckUtils.isPhone(phone)) {
            return ResultGenerator.genFailureResult("手机号格式不正确");
        }

        if (!checkPhoneCodeTimes(phone, scene)) {
            return ResultGenerator.genFailureResult("发送太频繁，请稍候再试");
        }

        if ("1".equals(scene) || "2".equals(scene)) {
            // 系统是否已有该手机号
            SmartUser phoneUser = userService.findByPhone(phone);
            if (phoneUser != null) {
                return ResultGenerator.genFailureResult("手机号已存在");
            }
        }

        String mobileCode = null;
        // 调用发送短信接口
        SmartSms sms = null;
        if (smsConfig.getSmsOpen() == 1) {
            // 生成验证码
            Random random = new Random();
            StringBuilder codeSb = new StringBuilder();
            for (int i=0;i<4;i++) {
                codeSb.append(random.nextInt(10));
            }
            mobileCode = codeSb.toString();
            sms = this.sendTencentMessage(phone, scene, mobileCode);
        } else {
            // 测试环境不发送短信
            mobileCode = "9999";
            sms = new SmartSms();
            sms.setStatus(1);
            sms.setScene(scene);
            sms.setContent(mobileCode);
            sms.setRemark(getSmsRemark(scene));
        }
        log.info(phone+"注册发送验证码："+mobileCode);
        if (sms.getStatus() == 1) {
            // 成功
            recordPhoneCodeTimes(phone, scene);
            // 记录验证码到redis，保留3分钟
            String phoneCodeName = String.format(SmartConstant.SMART_PHONE_CODE_NAME, phone, scene);
            redisClient.set(phoneCodeName, mobileCode, 0);
            redisClient.expire(phoneCodeName, 180, 0);
            return ResultGenerator.genSuccessResult(true);
        } else {
            // 失败
            return ResultGenerator.genFailureResult(sms.getErr());
        }
    }

    private String getTencentTemplateId(String scene) {
        if ("1".equals(scene)) {
            return smsConfig.getTencentRegistTemplateId();
        } else if ("2".equals(scene)) {
            return smsConfig.getTencentModifyPhoneTemplateId();
        } else {
            return "";
        }
    }

    private String getSmsRemark(String scene) {
        if ("1".equals(scene)) {
            return "注册发送验证码";
        } else if ("2".equals(scene)) {
            return "修改手机号发送验证码";
        } else {
            return "";
        }
    }

    private SmartSms sendTencentMessage(String phone, String scene, String code) {
        SmartSms sms = new SmartSms();
        sms.setPhone(phone);
        sms.setContent(code);
        sms.setRemark(getSmsRemark(scene));
        sms.setStatus(2);
        sms.setScene(scene);
        try {
            // 发送
            Credential cred = new Credential(smsConfig.getTencentSecretId(), smsConfig.getTencentSecretkey());
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setReqMethod("POST");
            httpProfile.setConnTimeout(10);
            httpProfile.setWriteTimeout(10);
            httpProfile.setReadTimeout(10);
            httpProfile.setEndpoint(smsConfig.getApiUrl());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(cred, smsConfig.getTencentRegion(), clientProfile);
            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(smsConfig.getTencentAppId());
            req.setSignName(smsConfig.getTencentSignName());
            req.setTemplateId(getTencentTemplateId(scene));
            String[] phoneArr = {"+86"+phone};
            String[] templateParamArr = {code};
            req.setTemplateParamSet(templateParamArr);
            req.setPhoneNumberSet(phoneArr);
            SendSmsResponse res = client.SendSms(req);
            // 创建记录
            sms.setResponse(SendSmsResponse.toJsonString(res));
            if (res.getSendStatusSet() != null && res.getSendStatusSet().length > 0) {
                sms.setStatus("Ok".equals(res.getSendStatusSet()[0].getCode())?1:2);
                sms.setErr(res.getSendStatusSet()[0].getMessage());
            } else {
                sms.setErr("发送失败");
            }
        } catch (Exception e) {
            sms.setErr(e.getMessage());
        }
        sms.setCreatedate(System.currentTimeMillis());
        sms.setUpdatedate(System.currentTimeMillis());
        smsService.addRecord(sms);
        return sms;
    }

    private boolean checkPhoneCodeTimes(String phone, String scene) {
        String tryTime = redisClient.get(String.format(SmartConstant.SMART_PHONE_CODE_MAX_TRY_NAME,phone,scene), 0);
        if (!StrUtils.isEmpty(tryTime)) {
            int time = Integer.parseInt(tryTime);
            if (time >= SmartConstant.SMART_PHONE_CODE_MAX_TRY_TIMES) {
                return false;
            }
        }
        return true;
    }

    private void recordPhoneCodeTimes(String phone, String scene) {
        String name = String.format(SmartConstant.SMART_PHONE_CODE_MAX_TRY_NAME,phone,scene);
        String tryTime = redisClient.get(name, 0);
        int time = 0;
        if (!StrUtils.isEmpty(tryTime)) {
            time = Integer.parseInt(tryTime);
        }
        redisClient.set(name, String.valueOf(time+1), 0);
        if (time == 0) {
            redisClient.expire(name, SmartConstant.PHONE_CODE_MAX_TRY_EXPIRE_TIME, 0);
        }
    }

    /**
     * 注册
     * @param paramMap
     *
     * @return
     */
    @ApiOperation("注册接口")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public Result register(HttpServletRequest request,@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
        String phone = paramMap.get("phone");
        if (StrUtils.isEmpty(phone)) {
            return ResultGenerator.genFailureResult("手机号不能空");
        }
        String verifyCode = paramMap.get("verifyCode");
        if (StrUtils.isEmpty(verifyCode)) {
            return ResultGenerator.genFailureResult("验证码不能为空");
        }
        String phoneCodeName = String.format(SmartConstant.SMART_PHONE_CODE_NAME, phone, SmartPhoneCodeEnum.REGIST.getCode());
        String redisVerifyCode = redisClient.get(phoneCodeName, 0);
        if (StrUtils.isEmpty(redisVerifyCode)) {
            return ResultGenerator.genFailureResult("验证码已过期");
        }
        if (!verifyCode.equals(redisVerifyCode)) {
            recordPhoneCodeTimes(phone, SmartPhoneCodeEnum.REGIST.getCode());
            return ResultGenerator.genFailureResult("验证码不正确");
        }
        String password = paramMap.get("password");
        if (StrUtils.isEmpty(password)) {
            return ResultGenerator.genFailureResult("密码不能空");
        }
        if (password.length() < 6) {
            return ResultGenerator.genFailureResult("密码长度不能少于6位");
        }
        SmartUser user = userService.findByPhone(phone);
        if (user != null) {
            return ResultGenerator.genFailureResult("用户已存在！");
        }
        user = new SmartUser();
        user.setPhone(phone);
        user.setPwd(password);
        user.setDisabled(0);
        long curtime = System.currentTimeMillis();
        user.setCreatedate(curtime);
        user.setUpdatedate(curtime);
        Long userId = userService.addUser(user);
        long now = System.currentTimeMillis();
        String uuid = request.getRemoteAddr()+":"+ UUID.randomUUID();
        String userName = SmartConstant.SMART_USER+userId;
        // 生成token，存入redis
        String token = JwtUtil.sign(userName, now, uuid);
        redisClient.set(userName, token, 0);
        redisClient.expire(userName, SecurityConstant.TOKEN_EXPIRE_TIME/1000,0);
        redisClient.set(userName+SecurityConstant.REDIS_USR_EXPIRE,String.valueOf(now+SecurityConstant.TOKEN_EXPIRE_TIME),0);
        redisClient.set(userName+SecurityConstant.REDIS_LOGIN_UUID, uuid, 0);
        // 清除验证码限制
        redisClient.del(SmartConstant.SMART_PHONE_CODE_MAX_TRY_NAME);
        SecurityUtils.getSubject().login(new JwtToken(token));
        Map<String,String> map = new HashMap<>();
        map.put("phone",user.getPhone());
        map.put("token",token);
        map.put("nick", user.getNick());
        map.put("head", user.getHead());
        return ResultGenerator.genSuccessResult(map);
    }


}
