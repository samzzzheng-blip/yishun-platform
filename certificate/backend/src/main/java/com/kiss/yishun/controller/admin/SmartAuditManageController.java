package com.kiss.yishun.controller.admin;

import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.PokemonBrand;
import com.kiss.yishun.entity.PokemonCard;
import com.kiss.yishun.entity.SmartAudit;
import com.kiss.yishun.entity.SmartScore;
import com.kiss.yishun.entity.enums.SmartAuditEnum;
import com.kiss.yishun.service.SmartAuditService;
import com.kiss.yishun.service.SmartBrandService;
import com.kiss.yishun.service.SmartCardService;
import com.kiss.yishun.service.SmartScoreService;
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
@Api("宝可梦审核管理模块")
@RequestMapping("api/admin-smart")
public class SmartAuditManageController {

	@Autowired
	SmartBrandService smartBrandService;

	@Autowired
	SmartCardService smartCardService;

	@Autowired
	SmartAuditService auditService;

	@Autowired
	SmartScoreService scoreService;

	/**
	 * 分页查询
	 * @param page
	 * @return
	 */
	@ApiOperation("分页查询")
	@RequestMapping(value = "/queryAuditList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryAuditList(@ApiParam(value = "page",required = true) @RequestParam("page") String page,
									@ApiParam(value = "pageSize",required = true) @RequestParam("pageSize") String pageSize) {
		Page<SmartAudit> pp = auditService.getAuditRecord(PageRequestUtils.getPageRequest(page, pageSize));
		Map<String,Object> result = new HashMap<>();
		result.put("list",pp.getContent());
		result.put("page",page);
		result.put("totalPage",pp.getTotalPages());
		result.put("totalCount",pp.getTotalElements());
		return ResultGenerator.genSuccessResult(result);
	}

	/**
	 * 更新
	 * @param auditItem
	 *
	 * @return
	 */
	@ApiOperation("更新审核通过")
	@RequestMapping(value = "/updateAuditPass", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateAuditPass(@ApiParam("audit") @RequestBody SmartAudit auditItem) {
		if (auditItem.getScoreId() == null) {
			return ResultGenerator.genFailureResult("记录ID不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getFrontUrl())) {
			return ResultGenerator.genFailureResult("正面图片不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getBackUrl())) {
			return ResultGenerator.genFailureResult("反面图片不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getAlias())) {
			return ResultGenerator.genFailureResult("快捷编码不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getSerial())) {
			return ResultGenerator.genFailureResult("系列名称不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getSubserial())) {
			return ResultGenerator.genFailureResult("子系列名称不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getName())) {
			return ResultGenerator.genFailureResult("宝可梦名称不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getRarity())) {
			return ResultGenerator.genFailureResult("宝可梦稀有度不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getAttribute())) {
			return ResultGenerator.genFailureResult("卡牌属性不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getCode())) {
			return ResultGenerator.genFailureResult("卡牌编号不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getCenter())) {
			return ResultGenerator.genFailureResult("中心评分不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getCorner())) {
			return ResultGenerator.genFailureResult("边缘评分不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getEdge())) {
			return ResultGenerator.genFailureResult("角落评分不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getSurface())) {
			return ResultGenerator.genFailureResult("表面评分不能为空");
		}
		if (StrUtils.isEmpty(auditItem.getScore())) {
			return ResultGenerator.genFailureResult("总评分不能为空");
		}
		PokemonBrand brand = smartBrandService.findBrandByAlias(auditItem.getAlias());
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		if (brand == null) {
			// 插入brand
			brand = new PokemonBrand();
			brand.setBrand(auditItem.getSerial());
			brand.setTitle(auditItem.getSubserial());
			brand.setAlias(auditItem.getAlias());
			brand.setCreatedate(System.currentTimeMillis());
			brand.setUpdatedate(System.currentTimeMillis());
			brand.setOperator(userName);
			long brandId = smartBrandService.addBrand(brand);
			brand.setId(brandId);
		}
		PokemonCard card = smartCardService.findCardByAliasAndCode(brand.getAlias(), auditItem.getCode());
		if (card == null) {
			// 插入card
			card = new PokemonCard();
			card.setOperator(userName);
			card.setName(auditItem.getName());
			card.setCode(auditItem.getCode());
			card.setRarity(auditItem.getRarity());
			card.setAttribute(auditItem.getAttribute());
			card.setAlias(brand.getAlias());
			card.setCreatedate(System.currentTimeMillis());
			card.setUpdatedate(System.currentTimeMillis());
			long cardId = smartCardService.addCard(card);
			card.setId(cardId);
		}
		// 更新audit记录
		SmartAudit audit = auditService.findAuditById(auditItem.getId());
		audit.setStatus(SmartAuditEnum.PASS.getCode());
		audit.setUpdatedate(System.currentTimeMillis());
		auditService.updateAudit(audit);
		// 更新score记录
		SmartScore score = scoreService.getScoreDetail(auditItem.getScoreId());
		score.setStatus(SmartAuditEnum.PASS.getCode());
		score.setAlias(brand.getAlias());
		score.setCardId(card.getId());
		score.setCenter(auditItem.getCenter());
		score.setCorner(auditItem.getCorner());
		score.setEdge(auditItem.getEdge());
		score.setSurface(auditItem.getSurface());
		score.setScore(auditItem.getScore());
		score.setUpdatedate(System.currentTimeMillis());
		scoreService.updateScore(score);
		return ResultGenerator.genSuccessResult();
	}


	/**
	 * 批量拒绝
	 * @param id
	 * @return
	 */
	@ApiOperation("批量拒绝审核")
	@RequestMapping(value = "/rejectAudits", method = {RequestMethod.GET})
	@ResponseBody
	public Result rejectAudits(@ApiParam(value = "编号",required = true,example = "1,2,3") @RequestParam("id") String id) {
		String[] ids = id.split(",");
		for (int i=0;i<ids.length;i++) {
			SmartAudit audit = auditService.findAuditById(Long.parseLong(ids[i]));
			if (audit != null) {
				auditService.rejectAudit(Long.parseLong(ids[i]));
				SmartScore scoreItem = scoreService.getScoreDetail(audit.getScoreId());
				if (scoreItem != null) {
					scoreItem.setStatus(SmartAuditEnum.REJECT.getCode());
					scoreService.updateScore(scoreItem);
				}
			}
		}
		return ResultGenerator.genSuccessResult();
	}
}
