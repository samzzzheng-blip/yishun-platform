package com.kiss.yishun.service;

import com.kiss.yishun.dao.*;
import com.kiss.yishun.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
public class GradingBatchPublishService {
    @Autowired private GradingJobDao jobs;
    @Autowired private GradingBatchSequenceDao sequences;
    @Autowired private GradingWorkflowService workflow;
    @Autowired private GradingTemplateService templates;
    @Autowired private GradingEventDao events;
    @Autowired private RateDao rates;
    @Autowired private GradingPhotoStore photos;
    @javax.persistence.PersistenceContext private javax.persistence.EntityManager em;
    @Data public static class Row {
        private Long id,version,batchNumber;
        private String name,certNumber,templateSchema,labelText;
    }
    @Data public static class Preview {
        private String batch,startNumber,token;
        private List<Row> rows;
        private boolean temporary;
    }
    public Preview preview(Long id,String start,String actor) {
        GradingJob anchor=workflow.get(id,actor);
        if(!actor.equals(anchor.getCreatedBy())||anchor.getRateId()!=null)throw new IllegalArgumentException("只能发布自己的未完成批次");
        if(anchor.getBatch()==null||anchor.getBatch().isEmpty())throw new IllegalArgumentException("请先填写收卡批次，再提交发布");
        List<GradingJob> all=jobs.findByBatchAndCreatedByOrderByIdAsc(anchor.getBatch(),actor);
        return plan(anchor.getBatch(),start,actor,all);
    }
    private Preview plan(String batch,String start,String actor,List<GradingJob> all) {
        start=GradingWorkflowService.text(start,18);
        boolean temporary=start.isEmpty();
        if(!temporary&&(!start.matches("[0-9]{1,18}")||new BigInteger(start).signum()==0))throw new IllegalArgumentException("起始编号需为1至18位正整数，留空则完成为临时商品");
        GradingBatchSequence counter=sequences.findById(batch).orElse(null);
        if(!temporary&&counter!=null&&counter.getStartNumber()!=null&&!counter.getStartNumber().equals(start))throw new IllegalArgumentException("本批次已发布，起始编号固定为 "+counter.getStartNumber());
        List<GradingJob> pending=new ArrayList<>();for(GradingJob j:all)if(j.getRateId()==null&&j.getDeletedAt()==null&&!"TEMPORARY".equals(j.getStage())&&Objects.equals(batch,j.getBatch()))pending.add(j);
        if(pending.isEmpty())throw new IllegalArgumentException("本批次已完成，没有待发布卡片");
        if(pending.size()>5000)throw new IllegalArgumentException("单批最多发布5000张卡片，请拆分批次");
        long fallback=counter==null?0:counter.getLastNumber();
        for(GradingJob j:all)if(j.getBatchNumber()!=null)fallback=Math.max(fallback,j.getBatchNumber());
        List<Row> rows=new ArrayList<>();
        Set<Long> numbers=new HashSet<>();Set<String> certificates=new HashSet<>();
        for(GradingJob j:pending) {
            if(!"PUBLISHING".equals(j.getStage()))throw new IllegalArgumentException("本批次还有待整理卡片，请全部提交后再发布");
            GradingWorkflowService.validateForPublish(j);
            long number=j.getBatchNumber()==null?++fallback:j.getBatchNumber();
            if(number<=0||number>999999999999999L)throw new IllegalArgumentException("批次内编号无效，请修改为最多15位正整数");
            if(!numbers.add(number)||jobs.existsByBatchAndBatchNumberAndIdNot(batch,number,j.getId()))throw new IllegalArgumentException("批次内编号 "+number+" 重复，整批未发布，请修改后重新预览");
            String cert="";
            if(!temporary){
            BigInteger n=new BigInteger(start).add(BigInteger.valueOf(number-1));
            if(n.toString().length()>18)throw new IllegalArgumentException("批次末尾编号超过18位，请调整起始编号");
            cert=n.toString();while(cert.length()<start.length())cert="0"+cert;
            if(!certificates.add(cert))throw new IllegalArgumentException("正式编号 "+cert+" 重复，整批未发布");
            if(Integer.valueOf(1).equals(rates.existsCertNumber(cert)))throw new IllegalArgumentException("编号 "+cert+" 已存在，整批未发布，请更换起始编号");
            }
            Row row=new Row();row.setId(j.getId());row.setVersion(j.getVersion());row.setBatchNumber(number);row.setName(j.getCardName());row.setCertNumber(cert);row.setTemplateSchema(templates.schema(j));row.setLabelText(templates.summary(j));rows.add(row);
        }
        rows.sort(Comparator.comparingLong(Row::getBatchNumber));
        Preview p=new Preview();p.setBatch(batch);p.setStartNumber(start);p.setRows(rows);p.setTemporary(temporary);
        try {
            byte[] digest=MessageDigest.getInstance("SHA-256").digest((actor+new ObjectMapper().writeValueAsString(p)).getBytes(StandardCharsets.UTF_8));
            StringBuilder token=new StringBuilder();for(byte v:digest)token.append(String.format("%02x",v&255));p.setToken(token.toString());
        } catch(Exception e){throw new IllegalStateException(e);}
        return p;
    }
    @Transactional
    public Preview publish(Long id,String start,String token,String actor) {
        GradingJob anchor=workflow.get(id,actor);
        if(!actor.equals(anchor.getCreatedBy())||anchor.getRateId()!=null)throw new IllegalArgumentException("该批次已发布或无权操作，请刷新");
        String batch=anchor.getBatch();if(batch==null||batch.isEmpty())throw new IllegalArgumentException("请先填写收卡批次");
        GradingBatchSequence counter=sequences.lockBatch(batch).orElse(null);
        if(counter==null){counter=new GradingBatchSequence();counter.setBatch(batch);sequences.saveAndFlush(counter);}
        List<GradingJob> all=jobs.findByBatchAndCreatedByOrderByIdAsc(batch,actor),locked=new ArrayList<>();
        for(GradingJob j:all){em.refresh(j,javax.persistence.LockModeType.PESSIMISTIC_WRITE);locked.add(j);}
        Preview p=plan(batch,start,actor,locked);
        if(token==null||!token.equals(p.getToken()))throw new IllegalArgumentException("批次内容或模板已变化，请重新预览并确认");
        Map<Long,GradingJob> byId=new HashMap<>();for(GradingJob j:locked)byId.put(j.getId(),j);
        // Validate all snapshots before creating any formal records.
        for(Row row:p.getRows())templates.freeze(byId.get(row.getId()));
        for(Row row:p.getRows()) {
            if(p.isTemporary()){
                GradingJob j=byId.get(row.getId());j.setBatchNumber(row.getBatchNumber());j.setCertNumber("");j.setConfirmedBy(actor);j.setStage("TEMPORARY");j.setUpdatedAt(System.currentTimeMillis());jobs.saveAndFlush(j);
                counter.setLastNumber(Math.max(counter.getLastNumber(),row.getBatchNumber()));continue;
            }
            GradingJob j=byId.get(row.getId());Rate r=new Rate();r.setCertNumber(row.getCertNumber());r.setRateName(j.getCardName());
            r.setSurface(j.getSurface());r.setCenter(j.getCenter());r.setEdge(j.getEdge());r.setCorner(j.getCorner());r.setScore(j.getScore());
            r.setImgUrl(j.getFrontPhoto()==null||j.getFrontPhoto().isEmpty()?"":photos.publish(j.getFrontPhoto(),actor));
            r.setRemark(GradingWorkflowService.text(j.getNotes(),500));r.setOperator(actor);r.setCreatedate(System.currentTimeMillis());r.setUpdatedate(r.getCreatedate());rates.saveAndFlush(r);
            j.setBatchNumber(row.getBatchNumber());j.setCertNumber(row.getCertNumber());j.setRateId(r.getId());j.setConfirmedBy(actor);j.setStage("DONE");j.setUpdatedAt(System.currentTimeMillis());jobs.saveAndFlush(j);
            counter.setLastNumber(Math.max(counter.getLastNumber(),row.getBatchNumber()));
            GradingEvent event=new GradingEvent();event.setJobId(j.getId());event.setActor(actor);event.setAction("PUBLISH_BATCH");event.setStage("DONE");event.setCreatedAt(j.getUpdatedAt());events.save(event);
        }
        if(!p.isTemporary())counter.setStartNumber(p.getStartNumber());sequences.saveAndFlush(counter);return p;
    }
}
