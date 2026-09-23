package com.kiss.yishun.service;

import com.kiss.yishun.dao.GradingJobDao;
import com.kiss.yishun.dao.GradingBatchSequenceDao;
import com.kiss.yishun.entity.GradingJob;
import com.kiss.yishun.entity.GradingBatchSequence;
import com.kiss.yishun.entity.vo.RateImportRow;
import com.kiss.yishun.entity.vo.RateImportResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/** Account-private, atomic import. No Rate, certificate allocation or audit tombstone. */
@Service
public class GradingTemporaryImportService {
    @Autowired private RateImportService parser;
    @Autowired private GradingJobDao jobs;
    @Autowired private GradingWorkflowService workflow;
    @Autowired private GradingBatchSequenceDao sequences;
    @Autowired private GradingPhotoStore photos;

    @Transactional(rollbackFor=Exception.class)
    public RateImportResult importFile(MultipartFile file,String actor) throws Exception {
        if(actor==null||actor.trim().isEmpty())throw new IllegalArgumentException("请先登录");
        List<RateImportRow> rows=parser.parseFile(file);
        MessageDigest digest=MessageDigest.getInstance("SHA-256");
        digest.update(actor.getBytes(StandardCharsets.UTF_8));digest.update((byte)0);
        byte[] hash=digest.digest(file.getBytes());
        String request=UUID.nameUUIDFromBytes(hash).toString();
        String prefix="TI-"+request.replace("-","")+"-";
        RateImportResult result=new RateImportResult();result.setTotal(rows.size());
        List<GradingJob> pending=new ArrayList<>();List<RateImportRow> source=new ArrayList<>();
        for(int i=0;i<rows.size();i++) {
            RateImportRow r=rows.get(i);String key=prefix+(i+1);
            if(jobs.findByJobNumber(key).isPresent()){result.addError(r.getSource()+"：此文件的该条记录已导入临时区");continue;}
            try {
                GradingJob j=new GradingJob();j.setJobNumber(key);j.setStage("TEMPORARY");
                j.setCardName(text(r.getRateName(),100,"名称"));j.setLabelText(text(r.getLabelText(),10000,"信息汇总"));
                j.setSurface(score(r.getSurface()));j.setCenter(score(r.getCenter()));j.setEdge(score(r.getEdge()));
                j.setCorner(score(r.getCorner()));j.setScore(score(r.getScore()));j.setNotes(text(r.getRemark(),1000,"备注"));
                j.setCertNumber("");j.setCreatedBy(actor);j.setGradedBy(actor);j.setConfirmedBy(actor);
                pending.add(j);source.add(r);
            }catch(IllegalArgumentException e){throw new IllegalArgumentException(r.getSource()+"："+e.getMessage()+"；本次未导入任何新记录");}
        }
        if(pending.isEmpty())return result;
        GradingBatchSequence batch=workflow.newBatch(request,actor);
        long now=System.currentTimeMillis();
        for(int i=0;i<pending.size();i++) {
            GradingJob j=pending.get(i);
            try {j.setFrontPhoto(photo(source.get(i),actor));}
            catch(IllegalArgumentException e){throw new IllegalArgumentException(source.get(i).getSource()+"："+e.getMessage()+"；本次未导入任何新记录");}
            batch.setLastNumber(Math.addExact(batch.getLastNumber(),1));
            j.setBatch(batch.getBatch());j.setBatchNumber(batch.getLastNumber());j.setCreatedAt(now);j.setUpdatedAt(now);
            jobs.save(j);
        }
        sequences.saveAndFlush(batch);jobs.flush();result.setImported(pending.size());return result;
    }
    private String text(String value,int limit,String field){String v=value==null?"":value.trim();if(v.length()>limit)throw new IllegalArgumentException(field+"超过"+limit+"个字符");return v;}
    private String score(String value){
        String v=text(value,10,"评分");if(v.isEmpty())return v;
        try {java.math.BigDecimal n=new java.math.BigDecimal(v);if(n.signum()<0||n.compareTo(java.math.BigDecimal.TEN)>0||n.remainder(new java.math.BigDecimal("0.5")).signum()!=0)throw new NumberFormatException();}
        catch(NumberFormatException e){throw new IllegalArgumentException("评分需为0至10、间隔0.5的数字，或留空");}return v;
    }
    private String photo(RateImportRow row,String actor) throws Exception {
        String value=text(row.getImgUrl(),14000000,"照片数据");byte[] bytes=row.getEmbeddedImage();
        if(value.startsWith("data:image/")){
            int comma=value.indexOf(',');if(comma<0||!value.substring(0,comma).contains(";base64"))throw new IllegalArgumentException("照片Base64格式不正确");
            try {bytes=Base64.getDecoder().decode(value.substring(comma+1));}catch(IllegalArgumentException e){throw new IllegalArgumentException("照片Base64格式不正确");}
        }
        if(bytes!=null){
            if(bytes.length==0||bytes.length>10*1024*1024)throw new IllegalArgumentException("照片为空或超过10MB");
            BufferedImage img=ImageIO.read(new ByteArrayInputStream(bytes));if(img==null)throw new IllegalArgumentException("照片无法识别，请使用PNG或JPEG图片");
            ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(img,"png",out);
            return photos.store(out.toByteArray(),true,actor);
        }
        if(value.isEmpty())return "";
        if(value.startsWith(GradingPhotoStore.PREFIX)){photos.check(value,actor);return text(value,200,"照片地址");}
        if(value.startsWith("https://")||value.startsWith("http://")||value.startsWith("/upload/"))return text(value,200,"照片地址");
        throw new IllegalArgumentException("照片需为网址、系统图片路径或嵌入图片；本地文件名不能直接上传照片");
    }
}
