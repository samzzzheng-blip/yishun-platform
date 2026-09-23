package com.kiss.yishun.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.config.UploadConfig;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.Cartoon;
import com.kiss.yishun.entity.User;
import com.kiss.yishun.service.CartoonService;
import com.kiss.yishun.service.OperLogService;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@Api("卡通管理模块")
@PropertySource("classpath:upload.properties")
@RequestMapping("api/usr")
public class CartoonController {

	@Autowired
	CartoonService cartoonService;

	@Autowired
	UploadConfig uploadConfig;

	@Autowired
	OperLogService operLogService;

	@Autowired
	UserService userService;

	/**
	 * 分页查询
	 * @param page
	 * @param keywords
	 * @return
	 */
	@ApiOperation("分页查询")
	@RequestMapping(value = "/queryCartoonList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryCartoonList(@ApiParam(value = "page",required = true) @RequestParam("page") String page,
									@ApiParam(value = "pageSize",required = true) @RequestParam("pageSize") String pageSize,
									@ApiParam(value = "关键字") @RequestParam(value = "keywords",required = false) String keywords) {
		keywords = StrUtils.isEmpty(keywords)? "":keywords;
		Page<Cartoon> pp = cartoonService.findCartoonPageByKeywords(PageRequestUtils.getPageRequest(page,pageSize),keywords);
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
	@ApiOperation("新增卡通")
	@RequestMapping(value = "/addCartoon", method = {RequestMethod.POST})
	@ResponseBody
	public Result addPrecious(@ApiParam("cartoon") @RequestBody Cartoon pdto) throws Exception {
		if (StrUtils.isEmpty(pdto.getCertNumber())) {
			return ResultGenerator.genFailureResult("证书编号不能为空");
		}
		if (StrUtils.isEmpty(pdto.getRoleName())) {
			return ResultGenerator.genFailureResult("角色名称不能为空");
		}
		if (StrUtils.isEmpty(pdto.getImgUrl())) {
			return ResultGenerator.genFailureResult("图片不能为空");
		}
		if (StrUtils.isEmpty(pdto.getCartoonName())) {
			return ResultGenerator.genFailureResult("动漫名称不能为空");
		}

		if (StrUtils.isEmpty(pdto.getItemType())) {
			return ResultGenerator.genFailureResult("类别不能为空");
		}
		if (StrUtils.isEmpty(pdto.getAuthor())) {
			return ResultGenerator.genFailureResult("原作者不能为空");
		}
		if (StrUtils.isEmpty(pdto.getCompany())) {
			return ResultGenerator.genFailureResult("制作公司不能为空");
		}
		if (StrUtils.isEmpty(pdto.getScore())) {
			return ResultGenerator.genFailureResult("评级不能为空");
		}

		boolean exist = Integer.valueOf(1).equals(cartoonService.existSameCertNumber(pdto.getCertNumber()));
		if (!exist) {
			pdto.setCreatedate(System.currentTimeMillis());
			pdto.setUpdatedate(System.currentTimeMillis());
			// 移动tmp中的图片或视频到对应文件夹永久保存
			pdto = dealCartoonSave(pdto);
			String token = (String) SecurityUtils.getSubject().getPrincipal();
			String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
			User user = userService.findByUsername(userName);
			if (user.getRemark() != null && !user.getRemark().isEmpty()) {
				pdto.setOperator(userName + "-" + user.getRemark());
			} else {
				pdto.setOperator(userName);
			}
			cartoonService.addCartoon(pdto);
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
	private Cartoon dealCartoonSave(Cartoon pdto) {
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
	@ApiOperation("更新卡通")
	@RequestMapping(value = "/updateCartoon", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateCartoon(@ApiParam("cartoon") @RequestBody Cartoon pdto) {
		if (StrUtils.isEmpty(pdto.getCertNumber())) {
			return ResultGenerator.genFailureResult("证书编号不能为空");
		}
		if (StrUtils.isEmpty(pdto.getRoleName())) {
			return ResultGenerator.genFailureResult("角色名称不能为空");
		}
		if (StrUtils.isEmpty(pdto.getImgUrl())) {
			return ResultGenerator.genFailureResult("图片不能为空");
		}
		if (StrUtils.isEmpty(pdto.getCartoonName())) {
			return ResultGenerator.genFailureResult("动漫名称不能为空");
		}

		if (StrUtils.isEmpty(pdto.getItemType())) {
			return ResultGenerator.genFailureResult("类别不能为空");
		}
		if (StrUtils.isEmpty(pdto.getAuthor())) {
			return ResultGenerator.genFailureResult("原作者不能为空");
		}
		if (StrUtils.isEmpty(pdto.getCompany())) {
			return ResultGenerator.genFailureResult("制作公司不能为空");
		}
		if (StrUtils.isEmpty(pdto.getScore())) {
			return ResultGenerator.genFailureResult("评级不能为空");
		}
		long id = pdto.getId();
		boolean exist = Integer.valueOf(1).equals(cartoonService.existsCertNumberUpdate(pdto.getCertNumber(), id));
		if (exist) {
			return ResultGenerator.genFailureResult("已有其他相同编号");
		}
		Cartoon cartoon = cartoonService.findCartoonById(id);
		if (cartoon!=null) {
			// 标记需要删除的源文件
			Map<String,String> deleteMap = new HashMap<>();
			boolean deleteOldDir = !pdto.getCertNumber().equals(cartoon.getCertNumber());
			String oldCartoonFolder = cartoon.getCertNumber();
			if (!deleteOldDir) {
				if (!StrUtils.isEmpty(cartoon.getImgUrl()) && !cartoon.getImgUrl().equals(pdto.getImgUrl())) {
					deleteMap.put("imgUrl",cartoon.getImgUrl());
				}
				if (!StrUtils.isEmpty(cartoon.getEvidenceImg()) && !cartoon.getEvidenceImg().equals(pdto.getEvidenceImg())) {
					deleteMap.put("evidenceImg",cartoon.getEvidenceImg());
				}
				if (!StrUtils.isEmpty(cartoon.getEvidenceVideo()) && !cartoon.getEvidenceVideo().equals(pdto.getEvidenceVideo())) {
					deleteMap.put("evidenceVideo",cartoon.getEvidenceVideo());
					deleteMap.put("evidenceVideoImgUrl",cartoon.getEvidenceVideoImgUrl());
				}
			}
			cartoon.setCertNumber(pdto.getCertNumber());
			cartoon.setRoleName(pdto.getRoleName());
			cartoon.setImgUrl(pdto.getImgUrl());
			cartoon.setCartoonName(pdto.getCartoonName());
			cartoon.setItemType(pdto.getItemType());
			cartoon.setAuthor(pdto.getAuthor());
			cartoon.setCompany(pdto.getCompany());
			cartoon.setEvidenceImg(pdto.getEvidenceImg());
			cartoon.setEvidenceVideo(pdto.getEvidenceVideo());
			cartoon.setEvidenceVideoImgUrl(pdto.getEvidenceVideoImgUrl());
			cartoon.setScore(pdto.getScore());
			cartoon.setRemark(pdto.getRemark() == null?"":pdto.getRemark());

			cartoon = dealCartoonSave(cartoon);
			cartoon.setUpdatedate(System.currentTimeMillis());
			String token = (String) SecurityUtils.getSubject().getPrincipal();
			String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
			User user = userService.findByUsername(userName);
			if (user.getRemark() != null && !user.getRemark().isEmpty()) {
				cartoon.setOperator(userName + "-" + user.getRemark());
			} else {
				cartoon.setOperator(userName);
			}
			cartoonService.updateCartoon(cartoon);
			// 删除不用的资源
			if (deleteOldDir) {
				deleteResource(oldCartoonFolder);
			} else {
				for (Map.Entry<String,String> entry: deleteMap.entrySet()) {
					if ("evidenceVideoImgUrl".equals(entry.getKey())) {
						deleteOldResource(oldCartoonFolder,entry.getValue(),"evidenceVideo");
					}
					deleteOldResource(oldCartoonFolder,entry.getValue(),entry.getKey());
				}
			}
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("找不到该证书");
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
	 * 删除对应卡通目录
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
	@ApiOperation("批量删除卡通")
	@RequestMapping(value = "/deleteCartoon", method = {RequestMethod.GET})
	@ResponseBody
	public Result deleteCartoon(@ApiParam(value = "编号",required = true,example = "1,2,3") @RequestParam("id") String id, HttpServletRequest request) {
		String[] ids = id.split(",");
		for (int i=0;i<ids.length;i++) {
			Cartoon cartoon = cartoonService.findCartoonById(Long.parseLong(ids[i]));
			if (cartoon!=null) {
				cartoonService.delCartoon(Long.parseLong(ids[i]));
				// 删除对应资源
				deleteResource(cartoon.getCertNumber());
			}
		}
		operLogService.addOperLog("deleteCartoon:"+id, IpUtils.getIp(request));
		return ResultGenerator.genSuccessResult();
	}

	@GetMapping("/exportCartoon")
	public Result exportCartoon(@RequestParam("startTime") long startTime, @RequestParam("endTime") long endTime, HttpServletRequest request, HttpServletResponse response) {
		// 获取用户信息列表
		List<Cartoon> cartoonList = cartoonService.findAllByUpdatedateBetween(startTime, endTime);
		// 导出文件
		if (cartoonList.isEmpty()) {
			return ResultGenerator.genFailureResult("查询空");
		}
		String preciousDir = uploadConfig.getDiskPreciousDir();
		String zipDir = uploadConfig.getDiskZipDir();
		String returnZipDir = uploadConfig.getReturnZipDir();
		File zipDirFile = new File(zipDir);
		if (!zipDirFile.exists()) {
			zipDirFile.mkdirs();
		}
		String fileName = "cartoon_"+ new SimpleDateFormat("yyyyMMdd").format(new Date());
		File targetZipFile = new File(zipDirFile, fileName+".zip");
		if (targetZipFile.exists()) {
			targetZipFile.delete();
		}
		// 使用 EasyExcel 写入数据到指定文件夹
		// clazz 参数用于指定 Excel 文件的结构类
		// sheetName 参数用于指定 Excel 文件中的工作表名称
		File excelFile = new File(zipDirFile,fileName+".xlsx");
		EasyExcel.write(excelFile.getPath(), Cartoon.class)
				.sheet("cartoon")
				.doWrite(cartoonList);
		// 添加打入zip的文件路径列表
		List<String> zipList = new ArrayList<>();
		zipList.add(excelFile.getPath());
		// 图片资源太大先不打包
//		cartoonList.forEach(cartoon -> {
//			File sourceFile = new File(preciousDir, cartoon.getCertNumber());
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
