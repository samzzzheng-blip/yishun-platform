package com.kiss.yishun.controller.admin;
import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.common.*;
import com.kiss.yishun.entity.*;
import com.kiss.yishun.service.*;
import com.kiss.yishun.config.UploadConfig;
import lombok.Data;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/usr")
public class GradingWorkflowController {
    @Autowired private GradingWorkflowService workflow;
    @Autowired private UserService users;
    @Autowired private MenuService menus;
    @Autowired private UploadConfig upload;
    @Autowired private GradingPhotoStore photos;
    @Autowired private GradingTemplateService templates;
    @Autowired private GradingBatchPublishService batchPublish;
    @Autowired private GradingTemporaryService temporary;
    @Autowired private GradingTemporaryImportService temporaryImport;
    @PostMapping("/gradingTemporaryImport") public Result temporaryImport(@RequestParam("file") MultipartFile file){return result(()->temporaryImport.importFile(file,actor(true)));}
    @GetMapping("/gradingTemporary") public Result temporaryList(@RequestParam(defaultValue="") String from,@RequestParam(defaultValue="") String to,@RequestParam(defaultValue="1") int page){return result(()->temporary.list(from,to,page,actor(false)));}
    @PostMapping("/gradingTemporaryPreview") public Result temporaryPreview(@RequestBody GradingTemporaryService.Range body){return result(()->temporary.preview(body,actor(4)));}
    @PostMapping("/gradingTemporaryPurge") public Result temporaryPurge(@RequestBody GradingTemporaryService.Range body){return result(()->temporary.purge(body,actor(4)));}
    @Data public static class BatchPublish {private Long id;private String startNumber;private String token;}
    @PostMapping("/gradingBatchPreview")
    public Result batchPreview(@RequestBody BatchPublish body) {return result(()->batchPublish.preview(body.getId(),body.getStartNumber(),actor(true)));}
    @PostMapping("/gradingBatchPublish")
    public Result publishBatch(@RequestBody BatchPublish body) {return result(()->batchPublish.publish(body.getId(),body.getStartNumber(),body.getToken(),actor(true)));}
    @GetMapping("/gradingTemplates")
    public Result templates() {return result(()->{actor(false);java.util.Map<String,Object> data=new java.util.HashMap<>();data.put("canManage",true);data.put("list",templates.list());return data;});}
    @PostMapping("/gradingTemplateSave")
    public Result saveTemplate(@RequestBody GradingLabelTemplate body) {return result(()->templates.save(body,actor(false)));}
    @Data public static class TemplateChoice {private Long id;private Long version;private Long templateId;private String expectedSchema;private String scope;}
    @PostMapping("/gradingTemplateSelect")
    public Result selectTemplate(@RequestBody TemplateChoice body) {return result(()->{
        String name=actor(true);workflow.get(body.getId(),name);
        templates.select(body.getId(),body.getVersion(),body.getTemplateId(),body.getExpectedSchema(),body.getScope(),name);
        return workflow.get(body.getId(),name);
    });}
    private String actor(boolean write) {
        return actor(write?2:1);
    }
    private String actor(int operation) {
        String token=(String)SecurityUtils.getSubject().getPrincipal();
        String name=JwtUtil.getAccount(token,SecurityConstant.ACCOUNT);
        User u=users.findByUsername(name);
        if(u==null || u.getDisabled()!=0 || u.getRole()==null) throw new IllegalArgumentException("请使用已启用的评级员工账号登录");
        boolean allowed=false;
        for(Permission p:u.getRole().getPermissionList()) {
            long op=p.getOperation()==null?0:p.getOperation().getId();
            if((operation==2?(op==2||op==3):op==operation) && includesRating(p.getMenu(),new java.util.HashSet<Long>())) allowed=true;
        }
        if(!allowed) throw new IllegalArgumentException("当前账号没有评级"+(operation==4?"删除":operation==2?"编辑":"查看")+"权限");
        return name;
    }
    // Match the original menu permission inheritance: a granted parent includes its descendants.
    // Never walk upward from a granted sibling or infer rights from the account's role name.
    private boolean includesRating(Menu menu,java.util.Set<Long> visited) {
        if(menu==null)return false;
        if("rateManage".equals(menu.getPath())||"/rateManage".equals(menu.getPath()))return true;
        if(menu.getId()<=0||!visited.add(menu.getId()))return false;
        java.util.List<Menu> children=menus.findMenuChildList(menu.getId());
        if(children!=null)for(Menu child:children)if(includesRating(child,visited))return true;
        return false;
    }
    private interface Task { Object run() throws Exception; }
    private Result result(Task task) {
        try { return ResultGenerator.genSuccessResult(task.run()); }
        catch(IllegalArgumentException e) { return ResultGenerator.genFailureResult(e.getMessage()); }
        catch(org.springframework.dao.DataIntegrityViolationException e) { return ResultGenerator.genFailureResult("请求或编号已存在，请刷新核对"); }
        catch(org.springframework.orm.ObjectOptimisticLockingFailureException e) { return ResultGenerator.genFailureResult("档案已更新，请刷新后重试"); }
        catch(Exception e) { e.printStackTrace(); return ResultGenerator.genFailureResult("保存或读取失败，请重试；未成功的操作不会推进进度"); }
    }
    @GetMapping("/gradingJobs")
    public Result list(@RequestParam(defaultValue="") String stage,@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="") String batch,@RequestParam(defaultValue="1") int page) {
        return result(()->workflow.list(stage,keyword,batch,page,actor(false)));
    }
    public Result list(String stage,String keyword,int page){return list(stage,keyword,"",page);}
    @GetMapping("/gradingBatches") public Result batches(){return result(()->workflow.batches(actor(false)));}
    @Data public static class NewBatch {private String requestId;}
    @PostMapping("/gradingNewBatch") public Result newBatch(@RequestBody NewBatch body){return result(()->{String name=actor(true);return retryNumberAllocation(()->workflow.newBatch(body.getRequestId(),name));});}
    @PostMapping("/gradingManagePreview") public Result managePreview(@RequestBody GradingWorkflowService.ManageRequest body){return result(()->workflow.managePreview(body,actor("DELETE".equals(body.getAction())?4:2)));}
    @PostMapping("/gradingManageApply") public Result manageApply(@RequestBody GradingWorkflowService.ManageRequest body){return result(()->workflow.manageApply(body,actor("DELETE".equals(body.getAction())?4:2)));}
    @GetMapping("/gradingJob")
    public Result detail(@RequestParam Long id) { return result(()->workflow.get(id,actor(false))); }
    @GetMapping("/gradingNextInBatch")
    public Result nextInBatch(@RequestParam Long id) {return result(()->java.util.Collections.singletonMap("next",workflow.nextInBatch(id,actor(true))));}
    @GetMapping("/gradingHistory")
    public Result history(@RequestParam Long id) { return result(()->workflow.history(id,actor(false))); }
    @PostMapping("/gradingCreate")
    public Result create(@RequestBody GradingJob body) { return result(()->{String name=actor(true);return retryNumberAllocation(()->workflow.create(body,name));}); }
    @PostMapping("/gradingSave")
    public Result save(@RequestBody GradingJob body) { return result(()->{String name=actor(true);return retryNumberAllocation(()->workflow.save(body.getId(),body,name));}); }
    @Data public static class SummaryEdit {private Long id,version;private String labelText,expectedSchema;}
    @PostMapping("/gradingSummarySave")
    public Result saveSummary(@RequestBody SummaryEdit body){return result(()->workflow.saveSummary(body.getId(),body.getVersion(),body.getLabelText(),body.getExpectedSchema(),actor(true)));}
    @Data public static class BatchNumberEdit {private Long id,version;private String batchNumber;}
    @PostMapping("/gradingBatchNumberSave")
    public Result saveBatchNumber(@RequestBody BatchNumberEdit body){return result(()->workflow.saveBatchNumber(body.getId(),body.getVersion(),body.getBatchNumber(),actor(true)));}
    // Each attempt enters a new service transaction; concurrent first cards may create the same counter.
    private Object retryNumberAllocation(Task task) throws Exception {
        for(int attempt=0;;attempt++)try{return task.run();}
        catch(org.springframework.dao.DataIntegrityViolationException|org.springframework.dao.TransientDataAccessException e){if(attempt>=4)throw e;}
    }
    @Data public static class Advance {
        private Long id;
        private Long version;
        private String action;
        private String certNumber;
    }
    @PostMapping("/gradingAdvance")
    public Result advance(@RequestBody Advance body) {
        return result(()->workflow.advance(body.getId(),body.getVersion(),body.getAction(),body.getCertNumber(),actor(true)));
    }
    @PostMapping("/gradingPhoto")
    public Result photo(@RequestParam("file") MultipartFile file) {
        return result(()->{
            String owner=actor(true);
            if(file.isEmpty() || file.getSize()>12*1024*1024) throw new IllegalArgumentException("照片需小于12MB");
            byte[] bytes=file.getBytes();
            boolean png=bytes.length>8 && bytes[0]==(byte)137 && bytes[1]==80 && bytes[2]==78 && bytes[3]==71;
            boolean jpg=bytes.length>3 && bytes[0]==(byte)255 && bytes[1]==(byte)216 && bytes[2]==(byte)255;
            if(!png&&!jpg) throw new IllegalArgumentException("请上传JPG或PNG照片；HEIC请先转换");
            try(javax.imageio.stream.ImageInputStream in=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                java.util.Iterator<javax.imageio.ImageReader> readers=ImageIO.getImageReaders(in);
                if(!readers.hasNext()) throw new IllegalArgumentException("无法读取照片");
                javax.imageio.ImageReader reader=readers.next();
                try { reader.setInput(in); if((long)reader.getWidth(0)*reader.getHeight(0)>50000000) throw new IllegalArgumentException("照片尺寸过大，请压缩后上传"); }
                finally { reader.dispose(); }
            }
            return photos.store(bytes,png,owner);
        });
    }
    @GetMapping("/gradingPhotoFile/{name:.+}")
    public org.springframework.http.ResponseEntity<byte[]> readPhoto(@PathVariable String name) {
        try {
            byte[] bytes=photos.read(GradingPhotoStore.PREFIX+name,actor(false));
            return org.springframework.http.ResponseEntity.ok().header("Cache-Control","no-store")
                .header("X-Content-Type-Options","nosniff")
                .contentType(name.endsWith(".png")?org.springframework.http.MediaType.IMAGE_PNG:org.springframework.http.MediaType.IMAGE_JPEG).body(bytes);
        } catch(Exception e) {return org.springframework.http.ResponseEntity.status(404).build();}
    }
}
