package com.kiss.yishun.controller.admin;

import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.PokemonCard;
import com.kiss.yishun.service.SmartCardService;
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
@Api("宝可梦卡牌管理模块")
@RequestMapping("api/admin-smart")
public class SmartCardManageController {

	@Autowired
	SmartCardService smartCardService;

	/**
	 * 分页查询
	 * @param page
	 * @return
	 */
	@ApiOperation("分页查询")
	@RequestMapping(value = "/queryCardList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryCardList(@ApiParam(value = "page",required = true) @RequestParam("page") String page,
								 @ApiParam(value = "pageSize",required = true) @RequestParam("pageSize") String pageSize,
								@ApiParam(value = "alias") @RequestParam(value = "alias",required = false) String alias,
								@ApiParam(value = "code") @RequestParam(value = "code",required = false) String code) {
		Page<PokemonCard> pp = null;
		pp = smartCardService.findAllCard(alias, code, PageRequestUtils.getPageRequest(page, pageSize));
		Map<String,Object> result = new HashMap<>();
		result.put("list",pp.getContent());
		result.put("page",page);
		result.put("totalPage",pp.getTotalPages());
		result.put("totalCount",pp.getTotalElements());
		return ResultGenerator.genSuccessResult(result);
	}

	/**
	 * 更新
	 * @param card
	 *
	 * @return
	 */
	@ApiOperation("更新card")
	@RequestMapping(value = "/updateCard", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateCard(@ApiParam("card") @RequestBody PokemonCard card) {
		if (card.getId() <= 0) {
			return ResultGenerator.genFailureResult("卡牌id不能为空");
		}
		PokemonCard updateCard = smartCardService.findCardById(card.getId());
		if (updateCard == null) {
			return ResultGenerator.genFailureResult("卡牌信息不存在");
		}
		boolean checkExist = false;
		if (!StrUtils.isEmpty(card.getAlias())) {
			// 转小写
			card.setAlias(card.getAlias().toLowerCase());
			if (!card.getAlias().equals(updateCard.getAlias())) {
				checkExist = true;
			}
			updateCard.setAlias(card.getAlias());
		}
		if (!StrUtils.isEmpty(card.getCode())) {
			if (!card.getCode().equals(updateCard.getCode())) {
				checkExist = true;
			}
			updateCard.setCode(card.getCode());
		}
		if (!StrUtils.isEmpty(card.getName())) {
			updateCard.setName(card.getName());
		}
		if (!StrUtils.isEmpty(card.getAttribute())) {
			updateCard.setAttribute(card.getAttribute());
		}
		if (!StrUtils.isEmpty(card.getRarity())) {
			updateCard.setRarity(card.getRarity());
		}
		// 是否更改了alias和code，是的话校验是否有重复冲突
		if (checkExist) {
			PokemonCard existCard = smartCardService.findCardByAliasAndCode(updateCard.getAlias(), updateCard.getCode());
			if (existCard != null) {
				return ResultGenerator.genFailureResult("已存在相同卡牌编号，不能重复");
			}
		}
		updateCard.setUpdatedate(System.currentTimeMillis());
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		updateCard.setOperator(userName);
		smartCardService.updateCard(updateCard);
		return ResultGenerator.genSuccessResult();
	}

	/**
	 * 新增
	 * @param card
	 *
	 * @return
	 */
	@ApiOperation("新增card")
	@RequestMapping(value = "/addCard", method = {RequestMethod.POST})
	@ResponseBody
	public Result addCard(@ApiParam("card") @RequestBody PokemonCard card) {
		if (StrUtils.isEmpty(card.getAlias())) {
			return ResultGenerator.genFailureResult("快捷编号不能为空");
		}
		if (StrUtils.isEmpty(card.getCode())) {
			return ResultGenerator.genFailureResult("卡牌编号不能为空");
		}
		if (StrUtils.isEmpty(card.getName())) {
			return ResultGenerator.genFailureResult("宝可梦名称不能为空");
		}
		if (StrUtils.isEmpty(card.getAttribute())) {
			return ResultGenerator.genFailureResult("宝可梦属性不能为空");
		}
		if (StrUtils.isEmpty(card.getRarity())) {
			return ResultGenerator.genFailureResult("稀有度不能为空");
		}
		// 转小写
		card.setAlias(card.getAlias().toLowerCase());
		PokemonCard existCard = smartCardService.findCardByAliasAndCode(card.getAlias(), card.getCode());
		if (existCard != null) {
			return ResultGenerator.genFailureResult("已存在相同卡牌编号，不能重复");
		}
		card.setCreatedate(System.currentTimeMillis());
		card.setUpdatedate(System.currentTimeMillis());
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		card.setOperator(userName);
		smartCardService.addCard(card);
		return ResultGenerator.genSuccessResult();
	}
}
