package com.kiss.yishun.controller.admin;

import com.alibaba.fastjson.JSON;
import com.kiss.yishun.config.UploadConfig;
import io.swagger.annotations.Api;
import org.apache.shiro.web.util.WebUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@Api("上传模块")
@PropertySource("classpath:upload.properties")
@RequestMapping("api/usr")
public class UploaderController {

    @Autowired
    UploadConfig uploadConfig;

    @ResponseBody
    @RequestMapping("/uploadImg")
    public void uploadImg(@RequestParam(value="file") CommonsMultipartFile file, HttpServletRequest request,
                          HttpServletResponse response){
        Map<String, Object> map = new HashMap<>();
        File targetFile=null;
        //返回存储路径
        String url="";
        //获取文件名加后缀
        String fileName=file.getOriginalFilename();
        if(fileName!=null&&fileName!=""){
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
            fileName=System.currentTimeMillis()+"_"+new Random().nextInt(1000)+fileF;
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
                // 视频保存缩略图
                if (file.getContentType().startsWith("video/")) {
                    String saveFileName = fileName.substring(0,fileName.lastIndexOf("."))+"_video.jpg";
                    String saveFilePath = targetFile.getPath().substring(0,targetFile.getPath().lastIndexOf("."))+"_video.jpg";
                    fetchFrame(targetFile.getPath(),saveFilePath);
                    map.put("videoPicUrl", returnUrl+fileAdd+"/"+saveFileName);
                }
                map.put("url", url);
                map.put("name", fileName);
                map.put("status","done");
                responseResult(response,map);
            } catch (Exception e) {
                e.printStackTrace();
                map.clear();
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
}
