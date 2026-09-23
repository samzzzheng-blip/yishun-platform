package com.kiss.yishun.controller.smartdetect;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.cache.JedisClient;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.constant.SmartConstant;
import com.kiss.yishun.entity.*;
import com.kiss.yishun.entity.enums.SmartAuditEnum;
import com.kiss.yishun.entity.vo.DetectCardResultVo;
import com.kiss.yishun.entity.vo.SmartCardDetailRespVo;
import com.kiss.yishun.service.*;
import com.kiss.yishun.utils.ImageBase64Util;
import com.kiss.yishun.utils.PageRequestUtils;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Api("Smart检测卡片模块")
@RequestMapping("api/smart/usr")
@RestController
@Log4j2
public class SmartDetectController {

	@Autowired
	private JedisClient redisClient;

	@Resource
	SmartUserService userService;

	@Resource
	SmartScoreService scoreService;

	@Resource
    SmartBrandService smartBrandService;

	@Resource
	SmartCardService smartCardService;

	@Resource
	SmartAuditService auditService;

	@Resource
	SmartConfigService configService;

	private SmartCardDetailRespVo parseDetectDetail(SmartScore smartScore, PokemonBrand pokemonBrand, PokemonCard pokemonCard) {
		SmartCardDetailRespVo respVo = new SmartCardDetailRespVo();
		respVo.setId(smartScore.getId());
		respVo.setEdge(smartScore.getEdge());
		respVo.setCorner(smartScore.getCorner());
		respVo.setCenter(smartScore.getCenter());
		respVo.setSurface(smartScore.getSurface());
		respVo.setScore(smartScore.getScore());
		respVo.setFrontUrl(smartScore.getFrontUrl());
		respVo.setBackUrl(smartScore.getBackUrl());
		respVo.setCertNumber(smartScore.getCertNumber());
		if (pokemonBrand != null) {
			SmartCardDetailRespVo.PokemonVo pokemonVo = new SmartCardDetailRespVo.PokemonVo();
			pokemonVo.setCardType("宝可梦");
			pokemonVo.setBrand(pokemonBrand.getBrand());
			pokemonVo.setTitle(pokemonBrand.getTitle());
			pokemonVo.setRarity(pokemonCard.getRarity());
			pokemonVo.setName(pokemonCard.getName());
			respVo.setCardInfo(pokemonVo);
			respVo.setStatus(1);
		} else {
			respVo.setStatus(0);
		}

		return respVo;
	}

	@ApiOperation("检测卡片")
	@RequestMapping(value = "/detectCard", method = {RequestMethod.POST})
	@ResponseBody
	public DeferredResult<Result> detectCard(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
		String frontUrl = paramMap.get("frontUrl");
		String backUrl = paramMap.get("backUrl");
		// 支持高并发写法，不占用nginx线程
		DeferredResult<Result> result = new DeferredResult<>(300000L, "超时"); // 5分钟超时
		// 超时处理
		result.onTimeout(() -> {
			deferredCallResult(result, ResultGenerator.genFailureResult("请求超时"));
		});
		// 异常处理
		result.onError((Throwable t) -> {
			deferredCallResult(result, ResultGenerator.genFailureResult(t.getMessage()));
		});
		deferredDetectCardRequest(result, frontUrl, backUrl);
		return result;
	}

	private void deferredDetectCardRequest(DeferredResult<Result> result, String frontUrl, String backUrl) {
		if (StrUtils.isEmpty(frontUrl)) {
			deferredCallResult(result, ResultGenerator.genFailureResult("正面图片不能为空"));
			return;
		}
		if (StrUtils.isEmpty(backUrl)) {
			deferredCallResult(result, ResultGenerator.genFailureResult("背面图片不能为空"));
			return;
		}
		frontUrl = frontUrl.replace("http://", "https://");
		backUrl = backUrl.replace("http://", "https://");
		String systemSet = configService.findValueByKey(SmartConstant.CONFIG_KEY_AI_SYSTEM_SET);
		String question = configService.findValueByKey(SmartConstant.CONFIG_KEY_AI_QUESTION);
		String aiAskUrl = configService.findValueByKey(SmartConstant.CONFIG_KEY_AI_ASK_URL);
		String apiKey = configService.findValueByKey(SmartConstant.CONFIG_KEY_AI_API_KEY);
		String model = configService.findValueByKey(SmartConstant.CONFIG_KEY_AI_MODEL);
		String absoluteUploadDir = configService.findValueByKey(SmartConstant.CONFIG_KEY_AI_UPLOAD_TMP_DIR);
		// 组装JSON
		// ---- 构建 system 消息 ----
		Map<String, Object> messageSystem = new HashMap<>();
		messageSystem.put("role", "system");
		messageSystem.put("content", systemSet);

		// ---- 构建 user 消息 ----
		Map<String, Object> contentText = new HashMap<>();
		contentText.put("type", "text");
		contentText.put("text", question);

		Map<String, Object> image1 = new HashMap<>();
		image1.put("type", "image_url");
		Map<String, String> imageUrl1 = new HashMap<>();
//		imageUrl1.put("url", frontUrl);
		// 使用base64完整渲染
        String frontBase64 = null;
        try {
            frontBase64 = ImageBase64Util.localTmpImageUrlToBase64(frontUrl, absoluteUploadDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
		if (frontBase64 == null) {
			imageUrl1.put("url", frontUrl);
		} else {
			imageUrl1.put("url", frontBase64);
		}
		image1.put("image_url", imageUrl1);

		Map<String, Object> image2 = new HashMap<>();
		image2.put("type", "image_url");
		Map<String, String> imageUrl2 = new HashMap<>();
//		imageUrl2.put("url", backUrl);
		// 使用base64完整渲染
		String backBase64 = null;
		try {
			backBase64 = ImageBase64Util.localTmpImageUrlToBase64(backUrl, absoluteUploadDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (backBase64 != null) {
			imageUrl2.put("url", backBase64);
		} else {
			imageUrl2.put("url", backUrl);
		}
		image2.put("image_url", imageUrl2);

		List<Map<String, Object>> contentList = new ArrayList<>();
		contentList.add(contentText);
		contentList.add(image1);
		contentList.add(image2);

		Map<String, Object> messageUser = new HashMap<>();
		messageUser.put("role", "user");
		messageUser.put("content", contentList);

		// ---- 构建请求 body ----
		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put("model", model);
		List<Map<String, Object>> messages = new ArrayList<>();
		messages.add(messageSystem);
		messages.add(messageUser);
		bodyMap.put("messages", messages);

		// ---- 转换为 JSON ----
		ObjectMapper mapper = new ObjectMapper();
        String body = null;
        try {
            body = mapper.writeValueAsString(bodyMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
			deferredCallResult(result, ResultGenerator.genFailureResult("参数异常:"+e.getMessage()));
			return;
		}
        log.info("=== Requeset body ===");
		if (frontBase64 != null) {
			log.info("frontBase64：" + frontBase64.substring(0,30) + "...");
		} else {
			log.info("frontUrl：" + frontUrl);
		}
		if (backBase64 != null) {
			log.info("backBase64：" + backBase64.substring(0,30) + "...");
		} else {
			log.info("backUrl：" + backUrl);
		}
		log.info(String.format("url:%s\nsystemSet:%s\nquestion:%s\nmodel:%s", aiAskUrl, systemSet, question, model));
		OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.connectTimeout(5, TimeUnit.MINUTES)
				.readTimeout(5, TimeUnit.MINUTES)
				.writeTimeout(5, TimeUnit.MINUTES)
				.callTimeout(5, TimeUnit.MINUTES)
				.build();
		Request request = new Request.Builder()
				.url(aiAskUrl)
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + apiKey)
				.post(okhttp3.RequestBody.create(MediaType.parse("application/json;charset=utf-8"), body))
				.build();
		String finalFrontUrl = frontUrl;
		String finalBackUrl = backUrl;
		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				deferredCallResult(result, ResultGenerator.genFailureResult("查询失败:"+e.getMessage()));
			}

			@Override
			public void onResponse(Call call, Response response) {
				deferredDetectCardResponse(result, finalFrontUrl, finalBackUrl, response);
			}
		});
	}

	private void deferredDetectCardResponse(DeferredResult<Result> result, String frontUrl, String backUrl, Response response) {
		okhttp3.ResponseBody responseBody = null;
		try {
			responseBody = response.body();
			if (response.isSuccessful() && responseBody != null) {
				String responseBodyStr = responseBody.string();
				log.info("=== Raw Response ===");
				log.info(responseBodyStr);
				// 用 Jackson 解析 JSON
				ObjectMapper resultmapper = new ObjectMapper();
				Map<String, Object> jsonMap = resultmapper.readValue(responseBodyStr, Map.class);
				// 取出 choices[0].message.content
				List<Map<String, Object>> choices = (List<Map<String, Object>>) jsonMap.get("choices");
				if (choices != null && !choices.isEmpty()) {
					Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
					if (message != null && message.get("content") != null) {
						String answer = message.get("content").toString();
						if (!StrUtils.isEmpty(answer) && !"null".equals(answer)) {
							log.info("---answer---"+answer);
							DetectCardResultVo cardResultVo = JSONObject.parseObject(answer, DetectCardResultVo.class);
							if (cardResultVo == null) {
								deferredCallResult(result, ResultGenerator.genFailureResult("查询失败: 卡牌解析失败"));
							} else if (cardResultVo.getResultCode() == -1 || StrUtils.isEmpty(cardResultVo.getBrand())) {
								log.info("无效卡牌, 原因：");
								if (!StrUtils.isEmpty(cardResultVo.getMsg())) {
									log.info(cardResultVo.getMsg());
								} else if (StrUtils.isEmpty(cardResultVo.getBrand())) {
									log.info("识别不到快捷编码");
								}
								deferredCallResult(result, ResultGenerator.genFailureResult("查询失败: 无效卡牌"));
							} else {
								deferredDetectValidCardResponse(result, frontUrl, backUrl, cardResultVo);
							}
						} else {
							deferredCallResult(result, ResultGenerator.genFailureResult("查询失败: 无效卡牌"));
						}
					} else {
						deferredCallResult(result, ResultGenerator.genFailureResult("查询失败: 未找到content"));
					}
				} else {
					deferredCallResult(result, ResultGenerator.genFailureResult("查询失败:未找到choices"));
				}
			} else {
				deferredCallResult(result, ResultGenerator.genFailureResult("查询失败:"+response.message()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			deferredCallResult(result, ResultGenerator.genFailureResult("查询异常:"+e.getMessage()));
		} finally {
			if (responseBody != null) {
				responseBody.close();
			}
		}
	}

	private void deferredDetectValidCardResponse(DeferredResult<Result> result, String frontUrl, String backUrl, DetectCardResultVo cardResultVo) {
		if (!StrUtils.isEmpty(cardResultVo.getBrand())) {
			// 转小写
			cardResultVo.setBrand(cardResultVo.getBrand().toLowerCase());
		}
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		Long userId = JwtUtil.getSmartUserId(token, SecurityConstant.ACCOUNT);
		SmartUser user = userService.findById(userId);
		// 是否待审核标记
		boolean needAudit = false;
		// 查询pokemon数据库
		PokemonBrand pokemonBrand = smartBrandService.findBrandByAlias(cardResultVo.getBrand());
		if (pokemonBrand == null) {
			needAudit = true;
		}
		PokemonCard pokemonCard = null;
		if (!needAudit) {
			pokemonCard = smartCardService.findCardByAliasAndCode(pokemonBrand.getAlias(), cardResultVo.getCardNum());
			if (pokemonCard == null) {
				needAudit = true;
			}
		}
		SmartAudit audit = null;
		if (needAudit) {
			// 进入待审核状态
			audit = new SmartAudit();
			audit.setFrontUrl(frontUrl);
			audit.setBackUrl(backUrl);
			audit.setAlias(cardResultVo.getBrand());
			audit.setSerial(cardResultVo.getParentSerial());
			audit.setSubserial(cardResultVo.getChildSerial());
			audit.setStatus(SmartAuditEnum.WAIT.getCode());
			audit.setName(cardResultVo.getPokemonName());
			audit.setRarity(cardResultVo.getRarity());
			audit.setAttribute(cardResultVo.getPokemonAttribute());
			audit.setCode(cardResultVo.getCardNum());
			audit.setCenter(cardResultVo.getCenter());
			audit.setCorner(cardResultVo.getCorner());
			audit.setEdge(cardResultVo.getEdge());
			audit.setSurface(cardResultVo.getSurface());
			audit.setScore(cardResultVo.getTotalScore());
			audit.setCreatedate(System.currentTimeMillis());
			audit.setUpdatedate(System.currentTimeMillis());
			long auditId = auditService.addAudit(audit);
			audit.setId(auditId);
		}
		// 保存查询记录数据
		SmartScore smartScore = new SmartScore();
		smartScore.setFrontUrl(frontUrl);
		smartScore.setBackUrl(backUrl);
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long startTime = calendar.getTimeInMillis();
		calendar.set(Calendar.HOUR, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		long endTime = calendar.getTimeInMillis();
		long todayCount = scoreService.getTodayCount(startTime, endTime);
		StringBuilder certNumberSb = new StringBuilder();
		certNumberSb.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		if (todayCount >= 999) {
			certNumberSb.append((todayCount+1));
		} else {
			certNumberSb.append(String.valueOf((1000+todayCount+1)).substring(1));
		}
		String certNumber = certNumberSb.toString();
		smartScore.setCertNumber(certNumber);
//		smartScore.setUserId(userId);
		smartScore.setUser(user);
		smartScore.setCreatedate(System.currentTimeMillis());
		smartScore.setUpdatedate(System.currentTimeMillis());
		if (needAudit) {
			smartScore.setAuditId(audit.getId());
			smartScore.setStatus(SmartAuditEnum.WAIT.getCode());
		} else {
			smartScore.setStatus(SmartAuditEnum.PASS.getCode());
			smartScore.setScore(cardResultVo.getTotalScore());
			smartScore.setEdge(cardResultVo.getEdge());
			smartScore.setCorner(cardResultVo.getCorner());
			smartScore.setSurface(cardResultVo.getSurface());
			smartScore.setCenter(cardResultVo.getCenter());
			smartScore.setCardId(pokemonCard.getId());
			smartScore.setAlias(pokemonBrand.getAlias());
		}
		long scoreId = scoreService.addScore(smartScore);
		if (needAudit) {
			audit.setScoreId(scoreId);
			auditService.updateAudit(audit);
			deferredCallResult(result, ResultGenerator.genSuccessResult(parseDetectDetail(smartScore, null, null)));
		} else {
			deferredCallResult(result, ResultGenerator.genSuccessResult(parseDetectDetail(smartScore, pokemonBrand, pokemonCard)));
		}
	}

	private void deferredCallResult(DeferredResult<Result> result, Result response) {
		if (!result.isSetOrExpired()) {
			result.setResult(response);
		}
	}

	@ApiOperation("获取检测记录")
	@RequestMapping(value = "/getDetectRecords", method = {RequestMethod.POST})
	@ResponseBody
	public Result getDetectRecords(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
		String page = paramMap.get("page");
		String pageSize = paramMap.get("pageSize");
		if (page == null || pageSize == null) {
			return ResultGenerator.genFailureResult("参数缺失");
		}
		String certNumber = paramMap.get("certNumber");
		if (certNumber == null) {
			certNumber = "";
		}
		String startTime = paramMap.get("startTime");
		String endTime = paramMap.get("endTime");
		Calendar calendar = Calendar.getInstance();
		if (StrUtils.isEmpty(endTime)) {
			endTime = String.valueOf(calendar.getTimeInMillis());
		}
		if (StrUtils.isEmpty(startTime)) {
			calendar.set(1970, Calendar.JANUARY,1,0,0,0);
			startTime = String.valueOf(calendar.getTimeInMillis());
		}
		String token = SecurityUtils.getSubject().getPrincipal().toString();
		Long userId = JwtUtil.getSmartUserId(token, SecurityConstant.ACCOUNT);
		Page<SmartScore> pp = scoreService.getScoreRecord(userId, certNumber, Long.parseLong(startTime), Long.parseLong(endTime), PageRequestUtils.getPageRequest(String.valueOf(page), String.valueOf(pageSize)));
		Map<String,Object> result = new HashMap<>();
		result.put("list",pp.getContent());
		result.put("page",page);
		result.put("totalPage",pp.getTotalPages());
		result.put("totalCount",pp.getTotalElements());
		return ResultGenerator.genSuccessResult(result);
	}

	@ApiOperation("查询卡片详情")
	@RequestMapping(value = "/queryDetectDetail", method = {RequestMethod.POST})
	@ResponseBody
	public Result queryDetectDetail(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
		String id = paramMap.get("id");
		if (StrUtils.isEmpty(id)) {
			return ResultGenerator.genFailureResult("参数缺失");
		}
		SmartScore smartScore = scoreService.getScoreDetail(Long.parseLong(id));
		PokemonBrand pokemonBrand = smartBrandService.findBrandByAlias(smartScore.getAlias());
		PokemonCard pokemonCard = smartCardService.findCardById(smartScore.getCardId());
		return ResultGenerator.genSuccessResult(parseDetectDetail(smartScore, pokemonBrand, pokemonCard));
	}

}
