package com.kiss.yishun.service;

import com.kiss.yishun.dao.*;
import com.kiss.yishun.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.math.BigDecimal;

@Service
public class GradingWorkflowService {
    @Autowired private GradingJobDao jobs;
    @Autowired private GradingEventDao events;
    @Autowired private RateDao rates;
    @Autowired private GradingPhotoStore photos;
    @Autowired private GradingTemplateService templates;
    @Autowired private GradingBatchSequenceDao sequences;
    @Autowired private GradingBatchDayDao days;
    @javax.persistence.PersistenceContext private javax.persistence.EntityManager em;
    @Transactional
    public GradingBatchSequence newBatch(String request,String actor) {
        requireActor(actor);
        if(request==null||!request.matches("[a-fA-F0-9-]{36}"))throw new IllegalArgumentException("新建批次请求无效");
        GradingBatchSequence prior=sequences.findByRequestId(request).orElse(null);
        if(prior!=null){if(!actor.equals(prior.getCreatedBy()))throw new IllegalArgumentException("请求已使用");return prior;}
        java.time.LocalDate date=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));
        String key=date+"|"+actor;
        GradingBatchDay day=days.lockDay(key).orElse(null);
        if(day==null){day=new GradingBatchDay();day.setDayActor(key);days.saveAndFlush(day);}
        String name;
        do {day.setLastNumber(Math.addExact(day.getLastNumber(),1));name=text(date.getMonthValue()+"."+date.getDayOfMonth()+actor+day.getLastNumber(),100);}while(sequences.existsById(name));
        days.saveAndFlush(day);
        GradingBatchSequence s=new GradingBatchSequence();s.setBatch(name);s.setCreatedBy(actor);s.setCreatedAt(System.currentTimeMillis());s.setRequestId(request);return sequences.saveAndFlush(s);
    }
    public List<String> batches(String actor) {
        requireActor(actor);Set<String> result=new LinkedHashSet<>();
        for(GradingBatchSequence s:sequences.findByCreatedByOrderByCreatedAtDesc(actor))result.add(s.getBatch());
        for(GradingJob j:jobs.findAll(filter("","","",actor),Sort.by(Sort.Direction.DESC,"updatedAt")))if(j.getBatch()!=null&&!j.getBatch().isEmpty())result.add(j.getBatch());
        return new ArrayList<>(result);
    }
    public GradingJob nextInBatch(Long id,String actor) {
        GradingJob current=get(id,actor);
        if(!actor.equals(current.getCreatedBy()))throw new IllegalArgumentException("只能处理自己的批次");
        if(current.getBatchNumber()==null||current.getBatch()==null||current.getBatch().isEmpty())return null;
        Page<GradingJob> next=jobs.findAll((root,query,cb)->cb.and(
            cb.equal(root.get("createdBy"),actor),cb.equal(root.get("batch"),current.getBatch()),
            cb.equal(root.get("stage"),"COLLECTING"),cb.isNull(root.get("rateId")),cb.isNull(root.get("deletedAt")),
            cb.greaterThan(root.get("batchNumber"),current.getBatchNumber())
        ),PageRequest.of(0,1,Sort.by(Sort.Direction.ASC,"batchNumber")));
        return next.hasContent()?get(next.getContent().get(0).getId(),actor):null;
    }
    private long nextBatchNumber(String batch) {
        GradingBatchSequence s=sequences.lockBatch(batch).orElse(null);
        if(s==null) {s=new GradingBatchSequence();s.setBatch(batch);sequences.saveAndFlush(s);}
        s.setLastNumber(Math.addExact(s.getLastNumber(),1));sequences.saveAndFlush(s);return s.getLastNumber();
    }
    public static final List<String> STAGES=Arrays.asList("COLLECTING","PUBLISHING","DONE");
    public Page<GradingJob> list(String stage,String keyword,int page,String actor) {
        return list(stage,keyword,"",page,actor);
    }
    public Page<GradingJob> list(String stage,String keyword,String batch,int page,String actor) {
        return jobs.findAll(filter(stage,keyword,batch,actor),PageRequest.of(Math.max(0,page-1),20,Sort.Direction.DESC,"updatedAt","id"));
    }
    private org.springframework.data.jpa.domain.Specification<GradingJob> filter(String stage,String keyword,String batch,String actor) {
        requireActor(actor);
        return (root,q,cb)->{
            List<Predicate> p=new ArrayList<>();
            p.add(cb.isNull(root.get("deletedAt")));
            p.add(cb.notEqual(root.get("stage"),"TEMPORARY"));
            if(batch!=null&&!batch.isEmpty())p.add(cb.equal(root.get("batch"),batch));
            p.add(cb.or(cb.equal(root.get("createdBy"),actor),cb.isNotNull(root.get("rateId"))));
            if(stage!=null && !stage.isEmpty()) {
                if("UNFINISHED".equals(stage)) p.add(cb.isNull(root.get("rateId")));
                else if("DONE".equals(stage)) p.add(cb.isNotNull(root.get("rateId")));
                else {
                    if(!STAGES.contains(stage)) throw new IllegalArgumentException("进度无效");
                    p.add(cb.isNull(root.get("rateId")));
                    p.add("COLLECTING".equals(stage)?cb.notEqual(root.get("stage"),"PUBLISHING"):cb.equal(root.get("stage"),stage));
                }
            }
            if(keyword!=null && !keyword.trim().isEmpty()) {
                String pattern="%"+keyword.trim().replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";
                p.add(cb.or(cb.like(root.get("jobNumber"),pattern,'\\'),cb.like(root.get("batch"),pattern,'\\'),
                    cb.like(root.get("cardName"),pattern,'\\'),cb.like(root.get("certNumber"),pattern,'\\')));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
    public GradingJob get(Long id,String actor) {
        GradingJob j=jobs.findById(id).orElseThrow(()->new IllegalArgumentException("档案不存在或无权访问"));
        accessible(j,actor);
        if("TEMPORARY".equals(j.getStage()))throw new IllegalArgumentException("该商品已临时完成，请到临时区域查看或清除");
        if(j.getRateId()!=null)j.setStage("DONE");
        else if(!"PUBLISHING".equals(j.getStage()))j.setStage("COLLECTING");
        return templates.decorate(j);
    }
    public List<GradingEvent> history(Long id,String actor) { get(id,actor); return events.findByJobIdOrderByCreatedAtDesc(id); }
    @lombok.Data public static class ManageRequest {
        private Long id;
        private String stage,keyword,batch,action,targetBatch,token;
    }
    @lombok.Data public static class ManagePreview {
        private int count;
        private String token;
    }
    private List<GradingJob> selected(ManageRequest r,String actor) {
        if(r.getId()!=null){GradingJob j=get(r.getId(),actor);return new ArrayList<>(Collections.singletonList(j));}
        Page<GradingJob> rows=jobs.findAll(filter(r.getStage(),r.getKeyword(),r.getBatch(),actor),PageRequest.of(0,5000,Sort.Direction.ASC,"id"));
        if(rows.getTotalElements()>5000)throw new IllegalArgumentException("单次最多操作5000张，请缩小批次或状态筛选范围");
        if(rows.isEmpty())throw new IllegalArgumentException("没有符合条件的档案，请刷新筛选结果");
        return rows.getContent();
    }
    private ManagePreview managePlan(ManageRequest r,String actor,List<GradingJob> rows) {
        if(!Arrays.asList("DELETE","MOVE").contains(r.getAction()))throw new IllegalArgumentException("操作无效");
        if("MOVE".equals(r.getAction())&&(r.getTargetBatch()==null||r.getTargetBatch().isEmpty()||!batches(actor).contains(r.getTargetBatch())))throw new IllegalArgumentException("请选择可用的目标批次");
        try {
            List<Object> stamp=new ArrayList<>();stamp.add(actor);stamp.add(r.getAction());stamp.add(r.getTargetBatch());
            for(GradingJob j:rows){accessible(j,actor);if("TEMPORARY".equals(j.getStage()))throw new IllegalArgumentException("请到临时区域清除商品");stamp.add(Arrays.asList(j.getId(),j.getVersion(),j.getBatch(),j.getStage()));}
            byte[] bytes=java.security.MessageDigest.getInstance("SHA-256").digest(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(stamp));
            StringBuilder token=new StringBuilder();for(byte b:bytes)token.append(String.format("%02x",b&255));
            ManagePreview p=new ManagePreview();p.setCount(rows.size());p.setToken(token.toString());return p;
        }catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new IllegalStateException(e);}
    }
    public ManagePreview managePreview(ManageRequest r,String actor) {return managePlan(r,actor,selected(r,actor));}
    @Transactional
    public ManagePreview manageApply(ManageRequest r,String actor) {
        // Lock the destination counter before jobs, matching the publish/create lock order.
        if("MOVE".equals(r.getAction()))sequences.lockBatch(text(r.getTargetBatch(),100)).orElseThrow(()->new IllegalArgumentException("目标批次不存在"));
        List<GradingJob> rows=new ArrayList<>(selected(r,actor));rows.sort(Comparator.comparing(GradingJob::getId));
        for(GradingJob j:rows)em.refresh(j,javax.persistence.LockModeType.PESSIMISTIC_WRITE);
        ManagePreview p=managePlan(r,actor,rows);
        if(r.getToken()==null||!r.getToken().equals(p.getToken()))throw new IllegalArgumentException("筛选结果或档案已变化，请重新预览后确认");
        for(GradingJob j:rows) {
            if("DELETE".equals(r.getAction()))j.setDeletedAt(System.currentTimeMillis());
            else if(!Objects.equals(j.getBatch(),r.getTargetBatch())) {
                j.setBatchNumber(nextBatchNumber(r.getTargetBatch()));j.setBatch(r.getTargetBatch());
                if(j.getRateId()==null){j.setStage("COLLECTING");j.setTemplateSchema(null);}
            }
            persist(j,actor,r.getAction());
        }
        return p;
    }
    private void requireActor(String actor) {
        if(actor==null || actor.trim().isEmpty()) throw new IllegalArgumentException("请登录后操作");
    }
    private void accessible(GradingJob j,String actor) {
        requireActor(actor);
        if(j.getDeletedAt()!=null)throw new IllegalArgumentException("档案已从评级流程删除");
        if(j.getRateId()==null && !actor.equals(j.getCreatedBy()))
            throw new IllegalArgumentException("档案不存在或无权访问");
    }
    @Transactional
    public GradingJob create(GradingJob input,String actor) {
        requireActor(actor);
        // Stable client request ID makes retrying a mobile submission idempotent.
        String number=text(input.getJobNumber(),48);
        if(!number.matches("G-[a-fA-F0-9-]{36}")) throw new IllegalArgumentException("建档请求编号无效，请重新打开收卡表单");
        Optional<GradingJob> existing=jobs.findByJobNumber(number);
        if(existing.isPresent()) {
            if(!actor.equals(existing.get().getCreatedBy())) throw new IllegalArgumentException("请求编号已使用");
            accessible(existing.get(),actor);
            return existing.get();
        }
        GradingJob j=new GradingJob();
        j.setJobNumber(number); j.setStage("COLLECTING");
        j.setBatch(text(input.getBatch(),100)); j.setFrontPhoto(photo(input.getFrontPhoto(),actor));
        j.setBackPhoto(photo(input.getBackPhoto(),actor));
        String cert=text(input.getCertNumber(),18);
        if(!cert.isEmpty() && !cert.matches("[0-9]{1,18}")) throw new IllegalArgumentException("编号需为1至18位数字");
        j.setCertNumber(cert);
        j.setBatchNumber(nextBatchNumber(j.getBatch()));
        j.setCreatedBy(actor); j.setCreatedAt(System.currentTimeMillis()); j.setUpdatedAt(j.getCreatedAt());
        jobs.saveAndFlush(j); event(j,actor,"CREATE"); return templates.decorate(j);
    }
    @Transactional
    public GradingJob save(Long id,GradingJob input,String actor) {
        GradingJob j=locked(id,input.getVersion(),actor);
        if(j.getRateId()==null) {
            templates.saveValues(j,input);
            String batch=text(input.getBatch(),100);
            if(!Objects.equals(batch,j.getBatch()))j.setBatchNumber(nextBatchNumber(batch));
            j.setBatch(batch); j.setCardName(text(input.getCardName(),100));
            j.setSeries(text(input.getSeries(),100)); j.setCardYear(text(input.getCardYear(),20));
            j.setLanguage(text(input.getLanguage(),40)); j.setCardNumber(text(input.getCardNumber(),100));
            j.setCertNumber(text(input.getCertNumber(),18));
            j.setFrontPhoto(photo(input.getFrontPhoto(),actor)); j.setBackPhoto(photo(input.getBackPhoto(),actor));
            j.setSurface(grade(input.getSurface())); j.setCenter(grade(input.getCenter()));
            j.setEdge(grade(input.getEdge())); j.setCorner(grade(input.getCorner())); j.setScore(grade(input.getScore()));
            j.setNotes(text(input.getNotes(),500));
            j.setStage("COLLECTING");
        } else throw new IllegalArgumentException("已完成记录请到评级管理中维护");
        return templates.decorate(persist(j,actor,"SAVE"));
    }
    public static void validateForPublish(GradingJob j) {
        required(j.getCardName(),"卡片名称");
        for(String value:Arrays.asList(j.getSurface(),j.getCenter(),j.getEdge(),j.getCorner(),j.getScore())) {
            required(value,"完整评分");grade(value);
        }
    }
    @Transactional
    public GradingJob saveBatchNumber(Long id,Long version,String number,String actor) {
        String value=text(number,15);
        if(!value.matches("[1-9][0-9]{0,14}"))throw new IllegalArgumentException("批次内编号须为最多15位的正整数，不含前导零");
        long n=Long.parseLong(value);
        GradingJob j=jobs.findById(id).orElseThrow(()->new IllegalArgumentException("档案不存在"));accessible(j,actor);
        String batch=j.getBatch()==null?"":j.getBatch();
        GradingBatchSequence counter=sequences.lockBatch(batch).orElse(null);
        if(counter==null){counter=new GradingBatchSequence();counter.setBatch(batch);sequences.saveAndFlush(counter);}
        em.refresh(j,javax.persistence.LockModeType.PESSIMISTIC_WRITE);accessible(j,actor);
        if(!Objects.equals(batch,j.getBatch()==null?"":j.getBatch())||version==null||!Objects.equals(version,j.getVersion()))throw new IllegalArgumentException("档案已变化，请刷新后重试");
        if(j.getRateId()!=null||"TEMPORARY".equals(j.getStage()))throw new IllegalArgumentException("已完成商品不能修改批次内编号");
        if(jobs.existsByBatchAndBatchNumberAndIdNot(j.getBatch(),n,id))throw new IllegalArgumentException("批次内编号 "+n+" 已占用（含保留档案），请更换编号");
        Long previousMax=jobs.maxBatchNumber(j.getBatch());
        j.setBatchNumber(n);counter.setLastNumber(Math.max(Math.max(counter.getLastNumber(),n),previousMax==null?0:previousMax));sequences.saveAndFlush(counter);
        return templates.decorate(persist(j,actor,"SAVE_BATCH_NUMBER"));
    }
    @Transactional
    public GradingJob saveSummary(Long id,Long version,String summary,String expectedSchema,String actor) {
        GradingJob j=locked(id,version,actor);
        if(j.getRateId()!=null||!"PUBLISHING".equals(j.getStage()))throw new IllegalArgumentException("仅待发布档案可修改信息汇总");
        if(!Objects.equals(templates.schema(j),expectedSchema==null?"":expectedSchema))throw new IllegalArgumentException("模板已变化，请刷新后重新核对汇总");
        if(summary!=null&&summary.length()>5000)throw new IllegalArgumentException("信息汇总不能超过5000字");
        // Null explicitly restores automatic generation; an empty string is a deliberate empty summary.
        j.setLabelText(summary);
        j.setTemplateSchema(templates.schema(j));
        return templates.decorate(persist(j,actor,"SAVE_SUMMARY"));
    }
    @Transactional
    public GradingJob advance(Long id,Long version,String action,String certNumber,String actor) {
        GradingJob j=locked(id,version,actor);
        if(j.getRateId()!=null)throw new IllegalArgumentException("该档案已发布完成");
        if("REOPEN".equals(action)) {
            if(!"PUBLISHING".equals(j.getStage()))throw new IllegalArgumentException("该阶段不能退回整理");
            j.setStage("COLLECTING");
        } else if("NEXT".equals(action)) {
            if("PUBLISHING".equals(j.getStage()))throw new IllegalArgumentException("请设置批次起始编号并整批发布");
            validateForPublish(j);
            j.setGradedBy(actor);j.setStage("PUBLISHING");
        } else throw new IllegalArgumentException("操作无效");
        return persist(j,actor,action);
    }
    private Rate rate(GradingJob j) {
        Rate r=j.getRateId()==null?null:rates.findRateById(j.getRateId());
        if(r==null || !Objects.equals(r.getCertNumber(),j.getCertNumber())) throw new IllegalArgumentException("正式评级记录已删除或编号变更，请先核对原记录");
        return r;
    }
    private GradingJob locked(Long id,Long version,String actor) {
        GradingJob j=jobs.lockById(id).orElseThrow(()->new IllegalArgumentException("档案不存在"));
        accessible(j,actor);
        if("TEMPORARY".equals(j.getStage()))throw new IllegalArgumentException("临时商品已完成，不能继续修改");
        if(version==null || !Objects.equals(j.getVersion(),version)) throw new IllegalArgumentException("档案已被其他同事修改，请刷新后重试");
        return j;
    }
    private GradingJob persist(GradingJob j,String actor,String action) {
        j.setUpdatedAt(System.currentTimeMillis()); jobs.saveAndFlush(j); event(j,actor,action); return j;
    }
    private void event(GradingJob j,String actor,String action) {
        GradingEvent e=new GradingEvent(); e.setJobId(j.getId()); e.setActor(actor); e.setAction(action);
        e.setStage(j.getStage()); e.setCreatedAt(System.currentTimeMillis()); events.save(e);
    }
    public static String text(String v,int max) {
        String s=v==null?"":v.trim();
        if(s.length()>max) throw new IllegalArgumentException("字段长度不能超过"+max+"个字符");
        return s;
    }
    private static void required(String v,String name) { if(v==null || v.isEmpty()) throw new IllegalArgumentException("请填写"+name); }
    private static String grade(String v) {
        String s=text(v,10); if(s.isEmpty()) return s;
        try {
            BigDecimal n=new BigDecimal(s);
            if(n.compareTo(BigDecimal.ZERO)<0 || n.compareTo(BigDecimal.TEN)>0 || n.multiply(new BigDecimal("2")).stripTrailingZeros().scale()>0)
                throw new NumberFormatException();
        } catch(NumberFormatException e) { throw new IllegalArgumentException("评分应为0至10之间的数字，以0.5分递增"); }
        return s;
    }
    private String photo(String v,String actor) {
        String s=text(v,200);
        if(s.startsWith(GradingPhotoStore.PREFIX)) {photos.check(s,actor);return s;}
        if(!s.isEmpty() && !s.matches("/upload/precious/workflow/[a-f0-9-]{36}\\.(jpg|png)"))
            throw new IllegalArgumentException("请通过收卡工作台上传照片");
        return s;
    }
}
