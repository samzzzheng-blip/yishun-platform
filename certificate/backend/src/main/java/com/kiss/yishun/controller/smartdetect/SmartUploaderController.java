package com.kiss.yishun.controller.smartdetect;

import com.alibaba.fastjson.JSON;
import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.config.SmartUploadConfig;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.SmartImageRecord;
import com.kiss.yishun.entity.SmartUser;
import com.kiss.yishun.service.SmartImageRecordService;
import com.kiss.yishun.service.SmartUserService;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@Api("Smart上传模块")
@PropertySource("classpath:smartupload.properties")
@RequestMapping("api/smart/usr")
public class SmartUploaderController {

    @Autowired
    SmartUploadConfig uploadConfig;

    @Resource
    SmartUserService userService;

    @Resource
    SmartImageRecordService imageRecordService;

    @ResponseBody
    @RequestMapping("/uploadCardImg")
    public void uploadCardImg(@RequestParam(value="file") CommonsMultipartFile file, HttpServletRequest request,
                          HttpServletResponse response){
        // 参数正面-1，反面-2获取
        String type = request.getParameter("type");
        Map<String, Object> map = new HashMap<>();
        if (!"1".equals(type) && !"2".equals(type)) {
            map.put("status","error");
            map.put("name", "上传错误");
            map.put("response","请上传正确的类型");
            responseResult(response,map);
            return;
        }
        File targetFile=null;
        //返回存储路径
        String url="";
        //获取文件名加后缀
        String fileName= file.getOriginalFilename();
        if(!fileName.isEmpty()){
            //存储路径
            String returnUrl = request.getScheme() + "://" + uploadConfig.getServerName() + ":" + uploadConfig.getPort()
                    + request.getContextPath() + uploadConfig.getReturnTmpDir();
            if ("80".equals(uploadConfig.getPort())) {
                returnUrl = request.getScheme() + "://"  + uploadConfig.getServerName()
                        + request.getContextPath() + uploadConfig.getReturnTmpDir();
            }
            //文件存储位置
            String path = uploadConfig.getDiskTmpDir();
            //文件后缀
            String fileF = fileName.substring(fileName.lastIndexOf("."));
            //新的文件名
            fileName = System.currentTimeMillis() + "_" + new Random().nextInt(1000) + fileF;
            //先判断文件是否存在
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String fileAdd = sdf.format(new Date());
            //获取文件夹路径
            File file1 =new File(path+"/"+fileAdd);
            //如果文件夹不存在则创建
            if(!file1 .exists()){
                file1 .mkdirs();
            }
            //将图片存入文件夹
            targetFile = new File(file1, fileName);
            try {
                //将上传的文件写到服务器上指定的文件。
                file.transferTo(targetFile);
                url=returnUrl+fileAdd+"/"+fileName;
                // 必须是图片
                if (!file.getContentType().startsWith("image/")) {
                    map.put("status","error");
                    map.put("name", "上传错误");
                    map.put("response","请上传图片");
                    responseResult(response,map);
                    return;
                }
                map.put("url", url);
                map.put("name", fileName);
                map.put("status","done");
                // 用户信息
                String token = SecurityUtils.getSubject().getPrincipal().toString();
                Long userId = JwtUtil.getSmartUserId(token, SecurityConstant.ACCOUNT);
                SmartUser user = userService.findById(userId);
                // 保存到图片历史记录
                SmartImageRecord imageRecord = new SmartImageRecord();
                imageRecord.setUrl(url);
                imageRecord.setName(fileName);
                long now = System.currentTimeMillis();
                imageRecord.setCreatedate(now);
                imageRecord.setUpdatedate(now);
                imageRecord.setType(Integer.parseInt(type));
                imageRecord.setUserId(user.getId());
                imageRecordService.addRecord(imageRecord);
                responseResult(response,map);
            } catch (Exception e) {
                map.put("status","error");
                map.put("name", "上传错误");
                map.put("response","系统异常，图片上传失败");
                responseResult(response,map);
            }
        }

    }

    @ResponseBody
    @RequestMapping("/uploadImg")
    public void uploadImg(@RequestParam(value="file") CommonsMultipartFile file, HttpServletRequest request,
                              HttpServletResponse response){
        File targetFile=null;
        //返回存储路径
        String url="";
        //获取文件名加后缀
        String fileName= file.getOriginalFilename();
        if(!fileName.isEmpty()){
            //存储路径
            String returnUrl = request.getScheme() + "://" + uploadConfig.getServerName() + ":" + uploadConfig.getPort()
                    + request.getContextPath() + uploadConfig.getReturnTmpDir();
            if ("80".equals(uploadConfig.getPort())) {
                returnUrl = request.getScheme() + "://"  + uploadConfig.getServerName()
                        + request.getContextPath() + uploadConfig.getReturnTmpDir();
            }
            //文件存储位置
            String path = uploadConfig.getDiskTmpDir();
            //文件后缀
            String fileF = fileName.substring(fileName.lastIndexOf("."));
            //新的文件名
            fileName = System.currentTimeMillis() + "_" + new Random().nextInt(1000) + fileF;
            //先判断文件是否存在
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String fileAdd = sdf.format(new Date());
            //获取文件夹路径
            File file1 =new File(path+"/"+fileAdd);
            //如果文件夹不存在则创建
            if(!file1 .exists()){
                file1 .mkdirs();
            }
            //将图片存入文件夹
            targetFile = new File(file1, fileName);
            Map<String, Object> map = new HashMap<>();
            try {
                //将上传的文件写到服务器上指定的文件。
                file.transferTo(targetFile);
                url=returnUrl+fileAdd+"/"+fileName;
                // 必须是图片
                if (!file.getContentType().startsWith("image/")) {
                    map.put("status","error");
                    map.put("name", "上传错误");
                    map.put("response","请上传图片");
                    responseResult(response,map);
                    return;
                }
                map.put("url", url);
                map.put("name", fileName);
                map.put("status","done");
                responseResult(response,map);
            } catch (Exception e) {
                map.put("status","error");
                map.put("name", "上传错误");
                map.put("response","系统异常，图片上传失败");
                responseResult(response,map);
            }
        }

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

    //参数：视频路径和缩略图保存路径
    public static void fetchFrame(String videofile, String framefile) {
        File targetFile = new File(framefile);
        FFmpegFrameGrabber ff = new FFmpegFrameGrabber(videofile);
        try {
            ff.start();
            int length = ff.getLengthInFrames();
            int i = 0;
            Frame f = null;
            while (i < length) {
                // 去掉前5帧，避免出现全黑的图片，依自己情况而定
                f = ff.grabImage();
                if ((i > 5) && (f.image != null)) {
                    break;
                }
                i++;
            }
            ImageIO.write(FrameToBufferedImage(f), "jpg", targetFile);
            //ff.flush();
            ff.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static BufferedImage FrameToBufferedImage(Frame frame) {
        //创建BufferedImage对象
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bufferedImage = converter.getBufferedImage(frame);
        return bufferedImage;
    }

    @ApiOperation("获取图片上传记录")
    @RequestMapping(value = "/getImageRecords", method = {RequestMethod.POST})
    @ResponseBody
    public Result getImageRecords(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
        String type = paramMap.get("type");
        if (!"1".equals(type) && !"2".equals(type)) {
            return ResultGenerator.genFailureResult("请上传正确的类型");
        }
        String token = (String) SecurityUtils.getSubject().getPrincipal();
        Long userId = JwtUtil.getSmartUserId(token, SecurityConstant.ACCOUNT);
        SmartUser smartUser = userService.findById(userId);
        // 取最近30条
        List<SmartImageRecord> list = imageRecordService.findLastList(Integer.parseInt(type), smartUser.getId(), PageRequest.of(0, 30));
        return ResultGenerator.genSuccessResult(list);
    }

    @ApiOperation("搜索图片上传记录")
    @RequestMapping(value = "/searchImageRecords", method = {RequestMethod.POST})
    @ResponseBody
    public Result searchImages(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
        String type = paramMap.get("type");
        if (!"1".equals(type) && !"2".equals(type)) {
            return ResultGenerator.genFailureResult("请上传正确的类型");
        }
        String key = paramMap.get("key");
        if (key.isEmpty()) {
            return ResultGenerator.genFailureResult("关键字不能空");
        }
        String token = (String) SecurityUtils.getSubject().getPrincipal();
        Long userId = JwtUtil.getSmartUserId(token, SecurityConstant.ACCOUNT);
        SmartUser smartUser = userService.findUserById(userId);
        List<SmartImageRecord> list = imageRecordService.findByFileName(Integer.parseInt(type), key, smartUser.getId(), PageRequest.of(0, 30));
        return ResultGenerator.genSuccessResult(list);
    }

    @ApiOperation("搜索图片上传记录")
    @RequestMapping(value = "/deleteHistoryImageRecord", method = {RequestMethod.POST})
    @ResponseBody
    public Result deleteHistoryImageRecord(@ApiParam(value = "paramMap",required = true) @RequestBody Map<String,String> paramMap) {
        String id = paramMap.get("id");
        if (StrUtils.isEmpty(id)) {
            return ResultGenerator.genFailureResult("参数不正确");
        }

        String token = (String) SecurityUtils.getSubject().getPrincipal();
        Long userId = JwtUtil.getSmartUserId(token, SecurityConstant.ACCOUNT);
        SmartUser smartUser = userService.findUserById(userId);
        boolean suc = imageRecordService.deleteImageRecord(smartUser.getId(), Long.parseLong(id));
        return suc?ResultGenerator.genSuccessResult():ResultGenerator.genFailureResult("删除失败");
    }

}
