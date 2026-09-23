package com.kiss.yishun.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.config.UploadConfig;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.Rate;
import com.kiss.yishun.entity.User;
import com.kiss.yishun.entity.vo.RateImportResult;
import com.kiss.yishun.service.OperLogService;
import com.kiss.yishun.service.RateImportService;
import com.kiss.yishun.service.RateService;
import com.kiss.yishun.service.UserService;
import com.kiss.yishun.utils.FileUtils;
import com.kiss.yishun.utils.IpUtils;
import com.kiss.yishun.utils.PageRequestUtils;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@Api("评级管理模块")
@PropertySource("classpath:upload.properties")
@RequestMapping("api/usr")
public class RateController {

	@Autowired
	RateService rateService;

	@Autowired
	UploadConfig uploadConfig;

	@Autowired
	OperLogService operLogService;

	@Autowired
	UserService userService;

	@Autowired
	RateImportService rateImportService;

	/**
	 * 分页查询
	 * @param page
	 * @param keywords
	 * @return
	 */
	@ApiOperation("分页查询")
	@RequestMapping(value = "/queryRateList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryRateList(@ApiParam(value = "page",required = true) @RequestParam("page") String page,
									@ApiParam(value = "pageSize",required = true) @RequestParam("pageSize") String pageSize,
									@ApiParam(value = "关键字") @RequestParam(value = "keywords",required = false) String keywords,
									@ApiParam(value = "起始编号") @RequestParam(value = "startNumber",required = false) String startNumber,
									@ApiParam(value = "结束编号") @RequestParam(value = "endNumber",required = false) String endNumber) {
		keywords = StrUtils.isEmpty(keywords)? "":keywords;
		Long start;
		Long end;
		try {
			start = parseCertNumberBound(startNumber, "起始编号");
			end = parseCertNumberBound(endNumber, "结束编号");
			validateRange(start, end);
		} catch (IllegalArgumentException e) {
			return ResultGenerator.genFailureResult(e.getMessage());
		}
		Page<Rate> pp = rateService.findRatePageByFilters(PageRequestUtils.getPageRequest(page,pageSize),keywords,start,end);
		Map<String,Object> result = new HashMap<>();
		result.put("list",pp.getContent());
		result.put("page",page);
		result.put("totalPage",pp.getTotalPages());
		result.put("totalCount",pp.getTotalElements());
		return ResultGenerator.genSuccessResult(result);
	}

	private Long parseCertNumberBound(String value, String fieldName) {
		if (StrUtils.isEmpty(value)) {
			return null;
		}
		String trimmed = value.trim();
		if (!trimmed.matches("[0-9]+")) {
			throw new IllegalArgumentException(fieldName + "只能填写数字");
		}
		try {
			return Long.valueOf(trimmed);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(fieldName + "超出允许范围");
		}
	}

	private void validateRange(Long start, Long end) {
		if (start != null && end != null && start > end) {
			throw new IllegalArgumentException("起始编号不能大于结束编号");
		}
	}

	/**
	 * 新增
	 * @param pdto
	 * @return
	 */
	@ApiOperation("新增评级")
	@RequestMapping(value = "/addRate", method = {RequestMethod.POST})
	@ResponseBody
	public Result addRate(@ApiParam("rate") @RequestBody Rate pdto) throws Exception {
		if (StrUtils.isEmpty(pdto.getCertNumber())) {
			return ResultGenerator.genFailureResult("证书编号不能为空");
		}
		if (StrUtils.isEmpty(pdto.getImgUrl())) {
			return ResultGenerator.genFailureResult("图片不能为空");
		}
		if (StrUtils.isEmpty(pdto.getRateName())) {
			return ResultGenerator.genFailureResult("名称不能为空");
		}
		if (StrUtils.isEmpty(pdto.getSurface())) {
			return ResultGenerator.genFailureResult("表面不能为空");
		}
		if (StrUtils.isEmpty(pdto.getEdge())) {
			return ResultGenerator.genFailureResult("边缘不能为空");
		}
		if (StrUtils.isEmpty(pdto.getCorner())) {
			return ResultGenerator.genFailureResult("角落不能为空");
		}
		if (StrUtils.isEmpty(pdto.getScore())) {
			return ResultGenerator.genFailureResult("总评分不能为空");
		}

		boolean exist = Integer.valueOf(1).equals(rateService.existSameCertNumber(pdto.getCertNumber()));
		if (!exist) {
			pdto.setCreatedate(System.currentTimeMillis());
			pdto.setUpdatedate(System.currentTimeMillis());
			// 移动tmp中的图片或视频到对应文件夹永久保存
			pdto = dealRateSave(pdto);
			String token = (String) SecurityUtils.getSubject().getPrincipal();
			String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
			User user = userService.findByUsername(userName);
			if (user.getRemark() != null && !user.getRemark().isEmpty()) {
				pdto.setOperator(userName + "-" + user.getRemark());
			} else {
				pdto.setOperator(userName);
			}
			rateService.addRate(pdto);
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("已有相同编号");
		}
	}

	/**
	 * 文件移动和路径处理
	 * @param pdto
	 * @return
	 */
	private Rate dealRateSave(Rate pdto) {
		if (!StrUtils.isEmpty(pdto.getImgUrl())) {
			moveResource(pdto.getCertNumber(),pdto.getImgUrl(),"img");
			pdto.setImgUrl(getSavePath(pdto.getImgUrl(),pdto.getCertNumber(),"img"));
		}
		return pdto;
	}

	/**
	 * 获取对应证书目录的外部路径
	 * @param tmpPath
	 * @param certNo
	 * @param type
	 * @return
	 */
	private String getSavePath(String tmpPath,String certNo,String type) {
		String path = tmpPath;
		if (tmpPath.contains(uploadConfig.getReturnTmpDir())) {
			// /upload/tmp/2222/ccc.png -> /upload/precious/certno/img/ccc.png
			String fileName = tmpPath.substring(tmpPath.lastIndexOf("/")+1);
			path = tmpPath.substring(0,tmpPath.indexOf(uploadConfig.getReturnTmpDir()))
					+uploadConfig.getReturnPreciousDir()+certNo+"/"+type+"/"+fileName;
		}
		return path;
	}

	/**
	 * 获取tmp磁盘目录
	 * @param tmpPath
	 * @return
	 */
	private String getTmpDiskPath(String tmpPath) {
		String path = uploadConfig.getDiskTmpDir() +tmpPath.substring(tmpPath.indexOf(uploadConfig.getReturnTmpDir())+uploadConfig.getReturnTmpDir().length());
		return path;
	}

	/**
	 * tmp资源移动到对应证书目录
	 * @param certNo
	 * @param tmpPath
	 * @param type
	 */
	private void moveResource(String certNo,String tmpPath,String type) {
		if (!tmpPath.contains(uploadConfig.getReturnTmpDir())) {
			return;
		}
		String target = uploadConfig.getDiskPreciousDir() +  certNo;
		File targetDir = new File(target, type);
		if(!targetDir.exists()){
			targetDir.mkdirs();
		}
		String tmpDiskPath = getTmpDiskPath(tmpPath);
		FileUtils.cutGeneralFile(tmpDiskPath, targetDir.getPath());
	}

	/**
	 * 更新
	 * @param pdto
	 *
	 * @return
	 */
	@ApiOperation("更新评级")
	@RequestMapping(value = "/updateRate", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateRate(@ApiParam("rate") @RequestBody Rate pdto) {
		if (StrUtils.isEmpty(pdto.getCertNumber())) {
			return ResultGenerator.genFailureResult("编号不能为空");
		}
		if (StrUtils.isEmpty(pdto.getImgUrl())) {
			return ResultGenerator.genFailureResult("图片不能为空");
		}
		if (StrUtils.isEmpty(pdto.getRateName())) {
			return ResultGenerator.genFailureResult("名称不能为空");
		}
		if (StrUtils.isEmpty(pdto.getSurface())) {
			return ResultGenerator.genFailureResult("表面不能为空");
		}
		if (StrUtils.isEmpty(pdto.getEdge())) {
			return ResultGenerator.genFailureResult("边缘不能为空");
		}
		if (StrUtils.isEmpty(pdto.getCorner())) {
			return ResultGenerator.genFailureResult("角落不能为空");
		}
		if (StrUtils.isEmpty(pdto.getScore())) {
			return ResultGenerator.genFailureResult("总评分不能为空");
		}
		long id = pdto.getId();
		boolean exist = Integer.valueOf(1).equals(rateService.existsCertNumberUpdate(pdto.getCertNumber(), id));
		if (exist) {
			return ResultGenerator.genFailureResult("已有其他相同编号");
		}
		Rate rate = rateService.findRateById(id);
		if (rate!=null) {
			// 标记需要删除的源文件
			Map<String,String> deleteMap = new HashMap<>();
			boolean deleteOldDir = !pdto.getCertNumber().equals(rate.getCertNumber());
			String oldRateFolder = rate.getCertNumber();
			if (!deleteOldDir) {
				if (!StrUtils.isEmpty(rate.getImgUrl()) && !rate.getImgUrl().equals(pdto.getImgUrl())) {
					deleteMap.put("imgUrl",rate.getImgUrl());
				}
			}
			rate.setCertNumber(pdto.getCertNumber());
			rate.setRateName(pdto.getRateName());
			rate.setImgUrl(pdto.getImgUrl());
			rate.setSurface(pdto.getSurface());
			rate.setEdge(pdto.getEdge());
			rate.setCorner(pdto.getCorner());
			rate.setCenter(pdto.getCenter());
			rate.setScore(pdto.getScore());
			rate.setRemark(pdto.getRemark() == null?"":pdto.getRemark());
			rate = dealRateSave(rate);
			rate.setUpdatedate(System.currentTimeMillis());
			String token = (String) SecurityUtils.getSubject().getPrincipal();
			String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
			User user = userService.findByUsername(userName);
			if (user.getRemark() != null && !user.getRemark().isEmpty()) {
				rate.setOperator(userName + "-" + user.getRemark());
			} else {
				rate.setOperator(userName);
			}
			rateService.updateRate(rate);
			// 删除不用的资源
			if (deleteOldDir) {
				deleteResource(oldRateFolder);
			} else {
				for (Map.Entry<String,String> entry: deleteMap.entrySet()) {
					if ("evidenceVideoImgUrl".equals(entry.getKey())) {
						deleteOldResource(oldRateFolder,entry.getValue(),"evidenceVideo");
					}
					deleteOldResource(oldRateFolder,entry.getValue(),entry.getKey());
				}
			}
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("找不到该评级");
		}
	}

	/**
	 * 删除旧资源
	 * @param foldName
	 * @param oldFilePath
	 * @param type
	 */
	private void deleteOldResource(String foldName,String oldFilePath,String type) {
		// Retain existing photos. Record edits must never physically delete media.
		// In particular, untrusted/legacy identifiers must not become filesystem paths.
	}

	/**
	 * 删除对应评级目录
	 * @param resourceFolder
	 */
	private void deleteResource(String resourceFolder) {
		// Incident safety policy: deleting a record does not delete its photo directory.
		// Empty identifiers previously resolved to the entire shared photo root.
		// Any future media cleanup requires a separate, audited recovery-safe workflow.
	}

	/**
	 * 批量删除
	 * @param id
	 * @return
	 */
	@ApiOperation("批量删除评级")
	@RequestMapping(value = "/deleteRate", method = {RequestMethod.GET})
	@ResponseBody
	public Result deleteRate(@ApiParam(value = "编号",required = true,example = "1,2,3") @RequestParam("id") String id, HttpServletRequest request) {
		String[] ids = id.split(",");
		for (int i=0;i<ids.length;i++) {
			Rate rate = rateService.findRateById(Long.parseLong(ids[i]));
			if (rate!=null) {
				rateService.delRate(Long.parseLong(ids[i]));
				// 删除对应资源
				deleteResource(rate.getCertNumber());
			}
		}
		operLogService.addOperLog("deleteRate:"+id, IpUtils.getIp(request));
		return ResultGenerator.genSuccessResult();
	}

	@ApiOperation("查询编号区间内的评级编号")
	@GetMapping("/queryRateCertNumbers")
	@ResponseBody
	public Result queryRateCertNumbers(@RequestParam(value = "startNumber", required = false) String startNumber,
									   @RequestParam(value = "endNumber", required = false) String endNumber) {
		try {
			Long start = parseCertNumberBound(startNumber, "起始编号");
			Long end = parseCertNumberBound(endNumber, "结束编号");
			requireRange(start, end);
			validateRange(start, end);
			List<String> certNumbers = new ArrayList<>();
			for (Rate rate : rateService.findAllByCertNumberRange(start, end)) {
				certNumbers.add(rate.getCertNumber());
			}
			return ResultGenerator.genSuccessResult(certNumbers);
		} catch (IllegalArgumentException e) {
			return ResultGenerator.genFailureResult(e.getMessage());
		}
	}

	@ApiOperation("删除编号区间内的全部评级")
	@GetMapping("/deleteRateRange")
	@ResponseBody
	public Result deleteRateRange(@RequestParam(value = "startNumber", required = false) String startNumber,
								  @RequestParam(value = "endNumber", required = false) String endNumber,
								  HttpServletRequest request) {
		try {
			Long start = parseCertNumberBound(startNumber, "起始编号");
			Long end = parseCertNumberBound(endNumber, "结束编号");
			requireRange(start, end);
			validateRange(start, end);
			List<Rate> rates = rateService.findAllByCertNumberRange(start, end);
			for (Rate rate : rates) {
				rateService.delRate(rate.getId());
				deleteResource(rate.getCertNumber());
			}
			Map<String, Object> result = new HashMap<>();
			result.put("deleted", rates.size());
			operLogService.addOperLog("deleteRateRange:" + startNumber + "-" + endNumber + ",deleted=" + rates.size(), IpUtils.getIp(request));
			return ResultGenerator.genSuccessResult(result);
		} catch (IllegalArgumentException e) {
			return ResultGenerator.genFailureResult(e.getMessage());
		}
	}

	@ApiOperation("下载编号区间内的全部评级二维码")
	@GetMapping("/downloadRateQrcodes")
	public void downloadRateQrcodes(@RequestParam(value = "startNumber", required = false) String startNumber,
									@RequestParam(value = "endNumber", required = false) String endNumber,
									HttpServletRequest request,
									HttpServletResponse response) throws Exception {
		Long start;
		Long end;
		try {
			start = parseCertNumberBound(startNumber, "起始编号");
			end = parseCertNumberBound(endNumber, "结束编号");
			requireRange(start, end);
			validateRange(start, end);
		} catch (IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		List<Rate> rates = rateService.findAllByCertNumberRange(start, end);
		String folderName = "二维码-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
		response.setContentType("application/zip");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(folderName + ".zip", "UTF-8").replace("+", "%20"));
		try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
			for (Rate rate : rates) {
				String certNumber = rate.getCertNumber();
				zos.putNextEntry(new ZipEntry(folderName + "/qrcode_" + certNumber + ".png"));
				String content = "https://www.yishunqianming.com/home?certNum=" + certNumber;
				BitMatrix bitMatrix = PreciousController.createQrcode(content, 300, 400);
				MatrixToImageWriter.writeToStream(bitMatrix, "png", zos);
				zos.closeEntry();
			}
		}
		operLogService.addOperLog("downloadRateQrcodes:" + startNumber + "-" + endNumber + ",count=" + rates.size(), IpUtils.getIp(request));
	}

	private void requireRange(Long start, Long end) {
		if (start == null && end == null) {
			throw new IllegalArgumentException("请至少填写一个编号区间");
		}
	}

	@ApiOperation("批量导入评级")
	@PostMapping("/importRate")
	@ResponseBody
	public Result importRate(@RequestParam("file") CommonsMultipartFile file, HttpServletRequest request) {
		try {
			String token = (String) SecurityUtils.getSubject().getPrincipal();
			String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
			User user = userService.findByUsername(userName);
			String operator = userName;
			if (user != null && !StrUtils.isEmpty(user.getRemark())) {
				operator += "-" + user.getRemark();
			}
			RateImportResult result = rateImportService.importFile(file, operator);
			operLogService.addOperLog("importRate:total=" + result.getTotal() + ",imported=" + result.getImported(), IpUtils.getIp(request));
			return ResultGenerator.genSuccessResult(result);
		} catch (IllegalArgumentException e) {
			return ResultGenerator.genFailureResult(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ResultGenerator.genFailureResult("导入失败，请检查文件格式和内容");
		}
	}

	@GetMapping("/exportRate")
	public Result exportRate(@RequestParam("startTime") long startTime, @RequestParam("endTime") long endTime, HttpServletRequest request, HttpServletResponse response) {
		// 获取用户信息列表
		List<Rate> rateList = rateService.findAllByUpdatedateBetween(startTime, endTime);
		// 导出文件
		if (rateList.isEmpty()) {
			return ResultGenerator.genFailureResult("查询空");
		}
		String preciousDir = uploadConfig.getDiskPreciousDir();
		String zipDir = uploadConfig.getDiskZipDir();
		String returnZipDir = uploadConfig.getReturnZipDir();
		File zipDirFile = new File(zipDir);
		if (!zipDirFile.exists()) {
			zipDirFile.mkdirs();
		}
		String fileName = "rate_"+ new SimpleDateFormat("yyyyMMdd").format(new Date());
		File targetZipFile = new File(zipDirFile, fileName+".zip");
		if (targetZipFile.exists()) {
			targetZipFile.delete();
		}
		// 使用 EasyExcel 写入数据到指定文件夹
		// clazz 参数用于指定 Excel 文件的结构类
		// sheetName 参数用于指定 Excel 文件中的工作表名称
		File excelFile = new File(zipDirFile,fileName+".xlsx");
		EasyExcel.write(excelFile.getPath(), Rate.class)
				.sheet("rate")
				.doWrite(rateList);
		// 添加打入zip的文件路径列表
		List<String> zipList = new ArrayList<>();
		zipList.add(excelFile.getPath());
		// 图片资源太大先不打包
//		rateList.forEach(precious -> {
//			File sourceFile = new File(preciousDir, precious.getCertNumber());
//			if (sourceFile.exists()) {
//				zipList.add(sourceFile.getPath());
//			}
//		});
		// 遍历打入zip
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetZipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);
			for (int i = 0; i < zipList.size(); i++) {
				File fileToZip = new File(zipList.get(i));
				zipFile(fileToZip, fileToZip.getName(), zos);
			}
			zos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// 删除excel
		excelFile.delete();
		// 返回zip下载地址
		String portPart = "80".equals(uploadConfig.getPort())?"":":" + uploadConfig.getPort();
		String returnUrl = request.getScheme() + "://" + uploadConfig.getServerName() + portPart
				+ request.getContextPath() + returnZipDir + fileName + ".zip";
		returnUrl = returnUrl.replace("http:", "https:");
		return ResultGenerator.genSuccessResult(returnUrl);
	}

	private static void zipFile(File fileToZip, String fileName, ZipOutputStream zos) throws Exception {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("/")) {
				zos.putNextEntry(new ZipEntry(fileName));
				zos.closeEntry();
			} else {
				zos.putNextEntry(new ZipEntry(fileName + "/"));
				zos.closeEntry();
			}
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zos);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zos.putNextEntry(zipEntry);
		byte[] bytes = new byte[2048];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}
		zos.closeEntry();
	}
}
