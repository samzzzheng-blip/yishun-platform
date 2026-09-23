package com.kiss.yishun.controller.admin;

import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.constant.SmartConstant;
import com.kiss.yishun.entity.SmartConfig;
import com.kiss.yishun.service.SmartConfigService;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Api("ai管理模块")
@RequestMapping("api/admin-smart")
public class SmartConfigManageController {

	@Autowired
	SmartConfigService configService;

	/**
	 * 查询AI设置
	 * @return
	 */
	@ApiOperation("查询AI设置")
	@RequestMapping(value = "/queryAiSet", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryAiSet() {
		String aiSystemSet = configService.findValueByKey(SmartConstant.CONFIG_KEY_AI_SYSTEM_SET);
		String aiQuestion = configService.findValueByKey(SmartConstant.CONFIG_KEY_AI_QUESTION);
		String aiAskUrl = configService.findValueByKey(SmartConstant.CONFIG_KEY_AI_ASK_URL);
		Map<String,String> map = new HashMap<>();
		map.put("aiSystemSet", aiSystemSet);
		map.put("aiQuestion", aiQuestion);
		map.put("aiAskUrl", aiAskUrl);
		return ResultGenerator.genSuccessResult(map);
	}

	/**
	 * 更新AI设置
	 * @param params 参数map
	 *
	 * @return
	 */
	@ApiOperation("更新AI设置")
	@RequestMapping(value = "/updateAiSet", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateAiSet(@ApiParam(value = "params", required = true) @RequestBody Map<String,String> params) {
		String aiSystemSet = params.get("aiSystemSet");
		String aiQuestion = params.get("aiQuestion");
		if (StrUtils.isEmpty(aiQuestion)) {
			return ResultGenerator.genFailureResult("提问词不能为空");
		}
		String aiAskUrl = params.get("aiAskUrl");
		if (StrUtils.isEmpty(aiAskUrl)) {
			return ResultGenerator.genFailureResult("访问接口不能为空");
		}
		SmartConfig smartConfig1 = new SmartConfig();
		smartConfig1.setName(SmartConstant.CONFIG_KEY_AI_SYSTEM_SET);
		smartConfig1.setValue(aiSystemSet == null ? "": aiSystemSet);
		configService.updateConfig(smartConfig1);
		SmartConfig smartConfig2 = new SmartConfig();
		smartConfig2.setName(SmartConstant.CONFIG_KEY_AI_QUESTION);
		smartConfig2.setValue(aiQuestion);
		configService.updateConfig(smartConfig2);
		SmartConfig smartConfig3 = new SmartConfig();
		smartConfig3.setName(SmartConstant.CONFIG_KEY_AI_ASK_URL);
		smartConfig3.setValue(aiAskUrl);
		configService.updateConfig(smartConfig3);
		return ResultGenerator.genSuccessResult();
	}
}
