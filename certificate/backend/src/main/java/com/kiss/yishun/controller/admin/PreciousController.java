package com.kiss.yishun.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.config.UploadConfig;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.Precious;
import com.kiss.yishun.entity.User;
import com.kiss.yishun.entity.vo.ExcelPreciousVo;
import com.kiss.yishun.service.OperLogService;
import com.kiss.yishun.service.PreciousService;
import com.kiss.yishun.service.UserService;
import com.kiss.yishun.utils.FileUtils;
import com.kiss.yishun.utils.IpUtils;
import com.kiss.yishun.utils.PageRequestUtils;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@Api("宝贝管理模块")
@PropertySource("classpath:upload.properties")
@RequestMapping("api/usr")
public class PreciousController {

	@Autowired
	PreciousService preciousService;

	@Autowired
	UploadConfig uploadConfig;

	@Autowired
	com.kiss.yishun.service.PreciousImportService preciousImportService;

	@Autowired
	UserService userService;

	@Autowired
	OperLogService operLogService;

	/**
	 * 分页查询
	 * @param page
	 * @param keywords
	 * @return
	 */
	@ApiOperation("分页查询")
	@RequestMapping(value = "/queryPreciousList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryPreciousList(@ApiParam(value = "page",required = true) @RequestParam("page") String page,
									@ApiParam(value = "pageSize",required = true) @RequestParam("pageSize") String pageSize,
									@ApiParam(value = "关键字") @RequestParam(value = "keywords",required = false) String keywords,
									@ApiParam(value = "状态") @RequestParam(value = "status",required = false) Integer status) {
		keywords = StrUtils.isEmpty(keywords)? "":keywords;
		status = status == null? -1:status;
		Page<Precious> pp = preciousService.findPreciousPageByKeywords(PageRequestUtils.getPageRequest(page, pageSize),keywords, status);
		Map<String,Object> result = new HashMap<>();
		result.put("list",pp.getContent());
		result.put("page",page);
		result.put("totalPage",pp.getTotalPages());
		result.put("totalCount",pp.getTotalElements());
		return ResultGenerator.genSuccessResult(result);
	}

	/**
	 * 新增
	 * @param pdto
	 * @return
	 */
	@ApiOperation("新增宝贝")
	@RequestMapping(value = "/addPrecious", method = {RequestMethod.POST})
	@ResponseBody
	public Result addPrecious(@ApiParam("precious") @RequestBody Precious pdto) throws Exception {
		if (StrUtils.isEmpty(pdto.getCertNumber())) {
			return ResultGenerator.genFailureResult("证书编号不能为空");
		}
		if (StrUtils.isEmpty(pdto.getSigner())) {
			return ResultGenerator.genFailureResult("签名者不能为空");
		}
		if (StrUtils.isEmpty(pdto.getItemType())) {
			return ResultGenerator.genFailureResult("类别不能为空");
		}
		if (StrUtils.isEmpty(pdto.getImgUrl())) {
			return ResultGenerator.genFailureResult("图片不能为空");
		}
//		if (StrUtils.isEmpty(pdto.getPublishCity())) {
//			return ResultGenerator.genFailureResult("活动地址不能为空");
//		}
//		if (StrUtils.isEmpty(pdto.getPublishTime())) {
//			return ResultGenerator.genFailureResult("活动时间不能为空");
//		}
//		if (StrUtils.isEmpty(pdto.getPublishActivity())) {
//			return ResultGenerator.genFailureResult("活动内容不能为空");
//		}
		boolean exist = Integer.valueOf(1).equals(preciousService.existSameCertNumber(pdto.getCertNumber()));
		if (!exist) {
			pdto.setCreatedate(System.currentTimeMillis());
			pdto.setUpdatedate(System.currentTimeMillis());
			pdto.setStatus(1);
			// 移动tmp中的图片或视频到对应文件夹永久保存
			pdto = dealPreciousSave(pdto);
			String token = (String) SecurityUtils.getSubject().getPrincipal();
			String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
			User user = userService.findByUsername(userName);
			if (user.getRemark() != null && !user.getRemark().isEmpty()) {
				pdto.setOperator(userName + "-" + user.getRemark());
			} else {
				pdto.setOperator(userName);
			}
			preciousService.addPrecious(pdto);
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("已有相同证书编号");
		}
	}

	/**
	 * 文件移动和路径处理
	 * @param pdto
	 * @return
	 */
	private Precious dealPreciousSave(Precious pdto) {
		if (!StrUtils.isEmpty(pdto.getImgUrl())) {
			moveResource(pdto.getCertNumber(),pdto.getImgUrl(),"img");
			pdto.setImgUrl(getSavePath(pdto.getImgUrl(),pdto.getCertNumber(),"img"));
		}
		if (!StrUtils.isEmpty(pdto.getEvidenceImg())) {
			moveResource(pdto.getCertNumber(),pdto.getEvidenceImg(),"evidenceImg");
			pdto.setEvidenceImg(getSavePath(pdto.getEvidenceImg(),pdto.getCertNumber(),"evidenceImg"));
		}
		if (!StrUtils.isEmpty(pdto.getEvidenceVideoImgUrl())) {
			moveResource(pdto.getCertNumber(),pdto.getEvidenceVideoImgUrl(),"evidenceVideo");
			moveResource(pdto.getCertNumber(),pdto.getEvidenceVideo(),"evidenceVideo");
			pdto.setEvidenceVideoImgUrl(getSavePath(pdto.getEvidenceVideoImgUrl(),pdto.getCertNumber(),"evidenceVideo"));
			pdto.setEvidenceVideo(getSavePath(pdto.getEvidenceVideo(),pdto.getCertNumber(),"evidenceVideo"));
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
	@ApiOperation("更新宝贝")
	@RequestMapping(value = "/updatePrecious", method = {RequestMethod.POST})
	@ResponseBody
	public Result updatePrecious(@ApiParam("precious") @RequestBody Precious pdto) {
		if (StrUtils.isEmpty(pdto.getCertNumber())) {
			return ResultGenerator.genFailureResult("证书编号不能为空");
		}
		if (StrUtils.isEmpty(pdto.getImgUrl())) {
			return ResultGenerator.genFailureResult("图片不能为空");
		}
		if (StrUtils.isEmpty(pdto.getSigner())) {
			return ResultGenerator.genFailureResult("签名人不能为空");
		}
		if (StrUtils.isEmpty(pdto.getItemType())) {
			return ResultGenerator.genFailureResult("载体不能为空");
		}
//		if (StrUtils.isEmpty(pdto.getPublishActivity())) {
//			return ResultGenerator.genFailureResult("活动内容不能为空");
//		}
//		if (StrUtils.isEmpty(pdto.getPublishCity())) {
//			return ResultGenerator.genFailureResult("活动地点不能为空");
//		}
//		if (StrUtils.isEmpty(pdto.getPublishTime())) {
//			return ResultGenerator.genFailureResult("活动时间不能为空");
//		}
		long id = pdto.getId();
		boolean exist = Integer.valueOf(1).equals(preciousService.existsCertNumberUpdate(pdto.getCertNumber(), id));
		if (exist) {
			return ResultGenerator.genFailureResult("已有其他相同编号");
		}
		Precious precious = preciousService.findPreciousById(id);
		if (precious!=null) {
			// 标记需要删除的源文件
			Map<String,String> deleteMap = new HashMap<>();
			boolean deleteOldDir = !pdto.getCertNumber().equals(precious.getCertNumber());
			String oldCertNumber = precious.getCertNumber();
			if (!deleteOldDir) {
				if (!StrUtils.isEmpty(precious.getImgUrl()) && !precious.getImgUrl().equals(pdto.getImgUrl())) {
					deleteMap.put("imgUrl",precious.getImgUrl());
				}
				if (!StrUtils.isEmpty(precious.getEvidenceImg()) && !precious.getEvidenceImg().equals(pdto.getEvidenceImg())) {
					deleteMap.put("evidenceImg",precious.getEvidenceImg());
				}
				if (!StrUtils.isEmpty(precious.getEvidenceVideo()) && !precious.getEvidenceVideo().equals(pdto.getEvidenceVideo())) {
					deleteMap.put("evidenceVideo",precious.getEvidenceVideo());
					deleteMap.put("evidenceVideoImgUrl",precious.getEvidenceVideoImgUrl());
				}
			}
			precious.setCertNumber(pdto.getCertNumber());
			precious.setImgUrl(pdto.getImgUrl());
			precious.setSigner(pdto.getSigner());
			precious.setItemType(pdto.getItemType());
			precious.setPublishActivity(pdto.getPublishActivity() == null ? "": pdto.getPublishActivity());
			precious.setPublishCity(pdto.getPublishCity() == null ? "": pdto.getPublishCity());
			precious.setPublishTime(pdto.getPublishTime() == null ? "": pdto.getPublishTime());
			precious.setPublishSign(pdto.getPublishSign() == null ? "": pdto.getPublishSign());
			precious.setEvidenceImg(pdto.getEvidenceImg());
			precious.setEvidenceVideo(pdto.getEvidenceVideo());
			precious.setEvidenceVideoImgUrl(pdto.getEvidenceVideoImgUrl());
			precious.setScore(pdto.getScore() == null ? "": pdto.getScore());
			precious.setRemark(pdto.getRemark() == null ? "":pdto.getRemark());

			precious = dealPreciousSave(precious);
			precious.setUpdatedate(System.currentTimeMillis());
			precious.setStatus(1);
			String token = (String) SecurityUtils.getSubject().getPrincipal();
			String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
			User user = userService.findByUsername(userName);
			if (user.getRemark() != null && !user.getRemark().isEmpty()) {
				precious.setOperator(userName + "-" + user.getRemark());
			} else {
				precious.setOperator(userName);
			}
			preciousService.updatePrecious(precious);

			// 删除不用的资源
			if (deleteOldDir) {
				deleteResource(oldCertNumber);
			} else {
				for (Map.Entry<String,String> entry: deleteMap.entrySet()) {
					if ("evidenceVideoImgUrl".equals(entry.getKey())) {
						deleteOldResource(oldCertNumber,entry.getValue(),"evidenceVideo");
					}
					deleteOldResource(oldCertNumber,entry.getValue(),entry.getKey());
				}
			}
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("找不到该宝贝");
		}
	}

	/**
	 * 删除旧资源
	 * @param certNo
	 * @param oldFilePath
	 * @param type
	 */
	private void deleteOldResource(String certNo,String oldFilePath,String type) {
		// Retain existing photos. Record edits must never physically delete media.
		// In particular, untrusted/legacy identifiers must not become filesystem paths.
	}

	/**
	 * 删除对应证书目录
	 * @param certNo
	 */
	private void deleteResource(String certNo) {
		// Incident safety policy: deleting a record does not delete its photo directory.
		// Empty identifiers previously resolved to the entire shared photo root.
		// Any future media cleanup requires a separate, audited recovery-safe workflow.
	}

	/**
	 * 批量删除
	 * @param id
	 * @return
	 */
	@ApiOperation("批量删除宝贝")
	@RequestMapping(value = "/deletePrecious", method = {RequestMethod.GET})
	@ResponseBody
	public Result deletePrecious(@ApiParam(value = "编号",required = true,example = "1,2,3") @RequestParam("id") String id, HttpServletRequest request) {
		String[] ids = id.split(",");
		for (int i=0;i<ids.length;i++) {
			Precious precious = preciousService.findPreciousById(Long.parseLong(ids[i]));
			if (precious!=null) {
				preciousService.delPrecious(Long.parseLong(ids[i]));
				// 删除对应资源
				deleteResource(precious.getCertNumber());
			}
		}
		operLogService.addOperLog("deletePrecious:"+id, IpUtils.getIp(request));
		return ResultGenerator.genSuccessResult();
	}

	/**
	 * 生成二维码下载
	 * @param certNo
	 * @param response
	 */
    @GetMapping("/downloadQrcode")
    @ApiOperation("二维码下载")
	public void downloadQrcode(@RequestParam("certNo") String certNo, HttpServletResponse response) {
		Map resultMap = new HashMap<String, Object>(5);
		try {
            String fileName = "qrcode_"+certNo+".png";
            String qrCodeContent = "https://www.yishunqianming.com/home?certNum=" + certNo;
            //生成二维码流
            BitMatrix bitMatrix = createQrcode(qrCodeContent,300,400);
            //设置请求头
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            OutputStream outputStream = response.getOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "png", outputStream);
            outputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("msg", "服务器异常");
		}

	}

    /**
     * 生成二维码流
     * @param content 二维码内容
     * @param width 二维码宽度
     * @param height 二维码高度
     * @return
     * @throws WriterException
     */
    public static BitMatrix createQrcode(String content,int width,int height)throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        // 指定编码格式
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 指定纠错级别(L--7%,M--15%,Q--25%,H--30%)
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 编码内容,编码类型(这里指定为二维码),生成图片宽度,生成图片高度,设置参数
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        return bitMatrix;
    }

	@RequestMapping("/uploadPreciousExcel")
	@ResponseBody
	public Result readExcel(@RequestParam(value="file") CommonsMultipartFile file,
            @RequestParam(value="attachments", required=false) org.springframework.web.multipart.MultipartFile[] attachments,
            @RequestParam(value="attachmentRefs", defaultValue="[]") String attachmentRefs, HttpServletRequest request,
							HttpServletResponse response) {
		try {
			String token = (String) SecurityUtils.getSubject().getPrincipal();
			String account = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
			User user = userService.findByUsername(account);
			String operator = account;
			if (user != null && !StrUtils.isEmpty(user.getRemark())) operator += "-" + user.getRemark();
            List<String> refs = JSON.parseArray(attachmentRefs, String.class);
            Map<String, org.springframework.web.multipart.MultipartFile> mapped = new HashMap<>();
            int count = attachments == null ? 0 : attachments.length;
            if (refs == null || refs.size() != count) throw new IllegalArgumentException("附件清单不匹配");
            for (int i = 0; i < count; i++) {
                if (refs.get(i) == null || mapped.put(refs.get(i), attachments[i]) != null) throw new IllegalArgumentException("附件引用重复");
            }
			return ResultGenerator.genSuccessResult(preciousImportService.importFile(file, operator, mapped));
		} catch (IllegalArgumentException e) {
			return ResultGenerator.genFailureResult(e.getMessage());
		} catch (Exception e) {
			org.slf4j.LoggerFactory.getLogger(getClass()).error("Precious Excel import failed", e);
			return ResultGenerator.genFailureResult("导入失败，请检查 Excel 格式和内容");
		}
	}

    @PostMapping("/previewPreciousExcel")
    public Result previewExcel(@RequestParam("file") CommonsMultipartFile file) {
        try { return ResultGenerator.genSuccessResult(preciousImportService.attachmentReferences(file)); }
        catch (IllegalArgumentException e) { return ResultGenerator.genFailureResult(e.getMessage()); }
        catch (Exception e) { return ResultGenerator.genFailureResult("无法读取 Excel，请检查格式"); }
    }

	private void responseResult(HttpServletResponse resp, Map<String,Object> map) {
		try {
			HttpServletResponse response = WebUtils.toHttp(resp);
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json; charset=utf-8");
			PrintWriter out;
			out = response.getWriter();
			out.write(JSON.toJSONString(map));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@GetMapping("/exportPrecious")
	public Result exportPrecious(@RequestParam("startTime") long startTime, @RequestParam("endTime") long endTime,HttpServletRequest request,HttpServletResponse response) {
		// 获取用户信息列表
		List<Precious> preciousList = preciousService.findAllByUpdatedateBetween(startTime, endTime);
		// 导出文件
		if (preciousList.isEmpty()) {
			return ResultGenerator.genFailureResult("查询空");
		}
		String preciousDir = uploadConfig.getDiskPreciousDir();
		String zipDir = uploadConfig.getDiskZipDir();
		String returnZipDir = uploadConfig.getReturnZipDir();
		File zipDirFile = new File(zipDir);
		if (!zipDirFile.exists()) {
			zipDirFile.mkdirs();
		}
		String fileName = "precious_"+ new SimpleDateFormat("yyyyMMdd").format(new Date());
		File targetZipFile = new File(zipDirFile, fileName+".zip");
		if (targetZipFile.exists()) {
			targetZipFile.delete();
		}
		// 使用 EasyExcel 写入数据到指定文件夹
		// clazz 参数用于指定 Excel 文件的结构类
		// sheetName 参数用于指定 Excel 文件中的工作表名称
		File excelFile = new File(zipDirFile,fileName+".xlsx");
		EasyExcel.write(excelFile.getPath(), Precious.class)
				.sheet("precious")
				.doWrite(preciousList);
		// 添加打入zip的文件路径列表
		List<String> zipList = new ArrayList<>();
		zipList.add(excelFile.getPath());
		// 图片资源太大先不打包
//		preciousList.forEach(precious -> {
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
