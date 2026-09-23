package com.kiss.yishun.service;

import com.kiss.yishun.dao.*;
import com.kiss.yishun.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.*;
import java.util.*;
import java.time.*;

/** Temporary completion creates no Rate. Purging writes no tombstone or audit record. */
@Service
public class GradingTemporaryService {
    @Autowired private GradingJobDao jobs;
    @Autowired private GradingBatchSequenceDao sequences;
    @Autowired private GradingBatchTemplateDao bindings;
    @PersistenceContext private EntityManager em;
    @lombok.Data public static class Range {private String from,to,token;}
    @lombok.Data public static class Preview {private long count;private String token,from,to;}
    private Specification<GradingJob> filter(String from,String to,String actor,boolean required){
        if(actor==null||actor.trim().isEmpty())throw new IllegalArgumentException("请先登录");
        final long lower=boundary(from,false,required),upper=boundary(to,true,required);
        if(lower>=upper)throw new IllegalArgumentException("开始日期不能晚于结束日期");
        return (root,q,cb)->cb.and(cb.equal(root.get("stage"),"TEMPORARY"),cb.isNull(root.get("rateId")),cb.isNull(root.get("deletedAt")),cb.equal(root.get("createdBy"),actor),cb.ge(root.get("updatedAt"),lower),cb.lt(root.get("updatedAt"),upper));
    }
    private long boundary(String date,boolean end,boolean required){
        if(date==null||date.trim().isEmpty()){if(required)throw new IllegalArgumentException("清除前必须选择开始和结束日期");return end?Long.MAX_VALUE:0;}
        try {LocalDate d=LocalDate.parse(date);return (end?d.plusDays(1):d).atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();}
        catch(Exception e){throw new IllegalArgumentException("日期格式应为 YYYY-MM-DD");}
    }
    public Page<GradingJob> list(String from,String to,int page,String actor){return jobs.findAll(filter(from,to,actor,false),PageRequest.of(Math.max(0,page-1),20,Sort.by(Sort.Direction.DESC,"updatedAt","id")));}
    private List<GradingJob> selected(Range r,String actor){
        Page<GradingJob> page=jobs.findAll(filter(r.getFrom(),r.getTo(),actor,true),PageRequest.of(0,5000,Sort.by("id")));
        if(page.getTotalElements()>5000)throw new IllegalArgumentException("单次最多清除5000件，请缩小时间范围");
        return new ArrayList<>(page.getContent());
    }
    private Preview plan(Range r,String actor,List<GradingJob> rows){
        if(rows.isEmpty())throw new IllegalArgumentException("该时间范围内没有临时商品");
        try {
            List<Object> stamp=new ArrayList<>();stamp.add(actor);stamp.add(r.getFrom());stamp.add(r.getTo());
            for(GradingJob j:rows){if(!"TEMPORARY".equals(j.getStage())||j.getRateId()!=null||!actor.equals(j.getCreatedBy()))throw new IllegalArgumentException("商品状态已变化，请重新筛选");stamp.add(Arrays.asList(j.getId(),j.getVersion(),j.getUpdatedAt()));}
            byte[] bytes=java.security.MessageDigest.getInstance("SHA-256").digest(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(stamp));StringBuilder token=new StringBuilder();for(byte b:bytes)token.append(String.format("%02x",b&255));
            Preview p=new Preview();p.setCount(rows.size());p.setToken(token.toString());p.setFrom(r.getFrom());p.setTo(r.getTo());return p;
        }catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new IllegalStateException(e);}
    }
    public Preview preview(Range r,String actor){return plan(r,actor,selected(r,actor));}
    @Transactional public Preview purge(Range r,String actor){
        List<GradingJob> rows=selected(r,actor);
        Set<String> batches=new TreeSet<>();for(GradingJob j:rows)if(j.getBatch()!=null)batches.add(j.getBatch());
        // Match publication's counter-before-job order and re-check membership after locking.
        for(String batch:batches)sequences.lockBatch(batch);
        for(GradingJob j:rows)em.refresh(j,LockModeType.PESSIMISTIC_WRITE);
        List<GradingJob> current=selected(r,actor);
        if(!current.stream().map(GradingJob::getId).collect(java.util.stream.Collectors.toList()).equals(rows.stream().map(GradingJob::getId).collect(java.util.stream.Collectors.toList())))throw new IllegalArgumentException("筛选结果已变化，请重新预览");
        Preview p=plan(r,actor,rows);if(r.getToken()==null||!r.getToken().equals(p.getToken()))throw new IllegalArgumentException("筛选结果已变化，请重新预览后确认");
        List<Long> ids=new ArrayList<>();for(GradingJob j:rows)ids.add(j.getId());
        em.createQuery("delete from GradingEvent e where e.jobId in :ids").setParameter("ids",ids).executeUpdate();
        for(GradingJob j:rows)em.remove(j);em.flush();
        for(String batch:batches){
            long remaining=em.createQuery("select count(j) from GradingJob j where j.batch=:batch",Long.class).setParameter("batch",batch).getSingleResult();
            if(remaining==0){bindings.findById(batch).ifPresent(bindings::delete);sequences.findById(batch).ifPresent(sequences::delete);}
        }
        // Shared templates/day sequence counters remain; they do not contain card records.
        return p;
    }
}
