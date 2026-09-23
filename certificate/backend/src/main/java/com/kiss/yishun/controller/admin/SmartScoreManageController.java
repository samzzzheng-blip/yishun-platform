package com.kiss.yishun.controller.admin;

import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.entity.SmartScore;
import com.kiss.yishun.service.SmartScoreService;
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
@Api("宝可梦Score管理模块")
@RequestMapping("api/admin-smart")
public class SmartScoreManageController {

	@Autowired
	SmartScoreService scoreService;

	/**
	 * 分页查询
	 * @param paramMap
	 * @return
	 */
	@ApiOperation("分页查询")
	@RequestMapping(value = "/queryAllScoreList", method = {RequestMethod.POST})
	@ResponseBody
	public Result queryAllScoreList(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
		String page = paramMap.get("page");
		String pageSize = paramMap.get("pageSize");
		if (page == null || pageSize == null) {
			return ResultGenerator.genFailureResult("参数缺失");
		}
		String phone = paramMap.get("phone");
		String certNumber = paramMap.get("certNumber");
		String startTime = paramMap.get("startTime");
		String endTime = paramMap.get("endTime");
		Page<SmartScore> pp = scoreService.getAllScoreList(phone, certNumber, startTime != null?Long.parseLong(startTime):0, endTime != null?Long.parseLong(endTime):0, PageRequestUtils.getPageRequest(page, pageSize));
		Map<String,Object> result = new HashMap<>();
		result.put("list",pp.getContent());
		result.put("page",page);
		result.put("totalPage",pp.getTotalPages());
		result.put("totalCount",pp.getTotalElements());
		return ResultGenerator.genSuccessResult(result);
	}

	/**
	 * 更新
	 * @param score
	 *
	 * @return
	 */
	@ApiOperation("更新score")
	@RequestMapping(value = "/updateScore", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateScore(@ApiParam("score") @RequestBody SmartScore score) {
		if (score.getId() <= 0) {
			return ResultGenerator.genFailureResult("记录ID不能为空");
		}
		SmartScore updateScore = scoreService.getScoreDetail(score.getId());
		if (updateScore == null) {
			return ResultGenerator.genFailureResult("记录不存在");
		}
		if (!StrUtils.isEmpty(score.getAlias())) {
			updateScore.setAlias(score.getAlias());
		}
		if (!StrUtils.isEmpty(score.getSurface())) {
			updateScore.setSurface(score.getSurface());
		}
		if (!StrUtils.isEmpty(score.getEdge())) {
			updateScore.setEdge(score.getEdge());
		}
		if (!StrUtils.isEmpty(score.getCenter())) {
			updateScore.setCenter(score.getCenter());
		}
		if (!StrUtils.isEmpty(score.getCorner())) {
			updateScore.setCorner(score.getCorner());
		}
		if (!StrUtils.isEmpty(score.getScore())) {
			updateScore.setScore(score.getScore());
		}
		scoreService.updateScore(updateScore);
		return ResultGenerator.genSuccessResult();
	}
}
