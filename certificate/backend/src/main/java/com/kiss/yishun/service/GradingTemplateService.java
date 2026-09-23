package com.kiss.yishun.service;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.kiss.yishun.dao.*;
import com.kiss.yishun.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class GradingTemplateService {
    @Autowired private GradingLabelTemplateDao templates;
    @Autowired private GradingBatchTemplateDao batches;
    @Autowired private GradingJobDao jobs;
    private final ObjectMapper json=new ObjectMapper();
    private JsonNode parse(String value) {
        try {return json.readTree(value);}catch(Exception e){throw new IllegalArgumentException("模板内容格式不正确");}
    }
    public List<GradingLabelTemplate> list() {return templates.findAll(org.springframework.data.domain.Sort.by("id"));}
    @Transactional public GradingLabelTemplate save(GradingLabelTemplate input,String actor) {
        String name=GradingWorkflowService.text(input.getName(),80);
        if(name.isEmpty())throw new IllegalArgumentException("请填写模板名称");
        if(input.getFieldsJson()==null||input.getFieldsJson().length()>12000)throw new IllegalArgumentException("模板字段过多");
        JsonNode fields=parse(input.getFieldsJson());
        if(!fields.isArray()||fields.size()<1||fields.size()>20)throw new IllegalArgumentException("模板需有1至20个字段");
        ArrayNode clean=json.createArrayNode();Set<String> keys=new HashSet<>(),labels=new HashSet<>();int names=0;
        for(JsonNode field:fields) {
            String key=field.path("key").asText(),label=GradingWorkflowService.text(field.path("label").asText(),40),join=field.path("join").asText();
            if(!key.matches("[a-zA-Z][a-zA-Z0-9_-]{0,63}")||!keys.add(key)||label.isEmpty()||!labels.add(label))throw new IllegalArgumentException("字段名称和标识不可为空或重复");
            if("cardName".equals(key)) {names++;if(!"名称".equals(label))throw new IllegalArgumentException("固定名称字段不可改名");}
            else if("名称".equals(label))throw new IllegalArgumentException("名称是固定字段");
            if(!Arrays.asList("space","newline").contains(join))throw new IllegalArgumentException("字段间只能用空格或换行连接");
            ObjectNode f=clean.addObject();f.put("key",key);f.put("label",label);f.put("join",join);
        }
        if(names!=1)throw new IllegalArgumentException("模板必须保留一个固定名称字段");
        GradingLabelTemplate t=input.getId()==null?new GradingLabelTemplate():templates.findById(input.getId()).orElseThrow(()->new IllegalArgumentException("模板不存在"));
        if(t.getId()!=null&&!Objects.equals(t.getVersion(),input.getVersion()))throw new IllegalArgumentException("模板已更新，请刷新重试");
        t.setName(name);t.setFieldsJson(clean.toString());t.setArchived(input.isArchived());t.setUpdatedBy(actor);t.setUpdatedAt(System.currentTimeMillis());
        return templates.saveAndFlush(t);
    }
    public String schema(GradingJob job) {
        if(job.getRateId()==null && !"TEMPORARY".equals(job.getStage()) && job.getBatch()!=null&&!job.getBatch().trim().isEmpty()) {
            Optional<GradingBatchTemplate> b=batches.findById(job.getBatch());
            String current=b.isPresent()?b.get().getSchemaJson():"";
            String own=job.getTemplateSchema();
            if(own!=null&&!own.isEmpty()) {
                JsonNode saved=parse(own);
                if(saved.path("singleCard").asBoolean(false)&&Objects.equals(saved.path("batchSchema").asText(),current))return own;
            }
            return current;
        }
        return job.getTemplateSchema()==null?"":job.getTemplateSchema();
    }
    public GradingJob decorate(GradingJob job) {job.setTemplateSchema(schema(job));return job;}
    @Transactional public void select(Long id,Long version,Long templateId,String expected,String actor) {
        select(id,version,templateId,expected,"BATCH",actor);
    }
    @Transactional public void select(Long id,Long version,Long templateId,String expected,String scope,String actor) {
        if(!Arrays.asList("SINGLE","BATCH").contains(scope))throw new IllegalArgumentException("请选择当前卡片或同批次应用");
        GradingJob j=jobs.lockById(id).orElseThrow(()->new IllegalArgumentException("档案不存在或无权访问"));
        if(j.getDeletedAt()!=null||!actor.equals(j.getCreatedBy())||j.getRateId()!=null||"TEMPORARY".equals(j.getStage()))throw new IllegalArgumentException("只能为自己的未确认档案选择模板");
        if(!Objects.equals(version,j.getVersion()))throw new IllegalArgumentException("档案已更新，请刷新重试");
        GradingLabelTemplate t=templates.findById(templateId).orElseThrow(()->new IllegalArgumentException("模板不存在"));
        if(t.isArchived())throw new IllegalArgumentException("模板已停用，请重新选择");
        ObjectNode s=json.createObjectNode();s.put("id",t.getId());s.put("version",t.getVersion());s.put("name",t.getName());s.set("fields",parse(t.getFieldsJson()));
        if(j.getBatch()!=null&&!j.getBatch().isEmpty()) {
            GradingBatchTemplate b=batches.lockBatch(j.getBatch()).orElseGet(()->{GradingBatchTemplate n=new GradingBatchTemplate();n.setBatch(j.getBatch());return n;});
            if(!Objects.equals(schema(j),expected==null?"":expected))throw new IllegalArgumentException("本批次模板已被更新，请刷新后重试");
            if("SINGLE".equals(scope)) {
                s.put("singleCard",true);s.put("batchSchema",b.getSchemaJson()==null?"":b.getSchemaJson());
            } else {
                // A fresh binding also replaces older single-card overrides in this batch.
                s.put("binding",UUID.randomUUID().toString());
                b.setSchemaJson(s.toString());b.setUpdatedBy(actor);b.setUpdatedAt(System.currentTimeMillis());batches.saveAndFlush(b);
            }
            j.setTemplateSchema(s.toString());j.setUpdatedAt(System.currentTimeMillis());jobs.saveAndFlush(j);
        } else {
            if(!Objects.equals(schema(j),expected==null?"":expected))throw new IllegalArgumentException("模板已更新，请刷新后重试");
            j.setTemplateSchema(s.toString());j.setUpdatedAt(System.currentTimeMillis());jobs.saveAndFlush(j);
        }
    }
    public void saveValues(GradingJob job,GradingJob input) {
        String current=schema(job);
        if(!Objects.equals(current,input.getTemplateSchema()==null?"":input.getTemplateSchema()))throw new IllegalArgumentException("批次模板已变更，请刷新档案后重新填写");
        String raw=input.getTemplateValues();
        if(raw==null||raw.trim().isEmpty())raw="{}";
        if(raw.length()>12000)throw new IllegalArgumentException("标签内容过长");
        JsonNode values=parse(raw);if(!values.isObject())throw new IllegalArgumentException("标签字段格式不正确");
        ObjectNode clean=json.createObjectNode();
        // Retain previous fields across template changes; they are never copied between cards.
        Iterator<Map.Entry<String,JsonNode>> it=values.fields();int count=0;
        while(it.hasNext()) {Map.Entry<String,JsonNode> e=it.next();if(++count>60||!e.getKey().matches("[a-zA-Z][a-zA-Z0-9_-]{0,63}")||!e.getValue().isTextual())throw new IllegalArgumentException("标签字段格式不正确");
            if(!"cardName".equals(e.getKey()))clean.put(e.getKey(),GradingWorkflowService.text(e.getValue().asText(),200));}
        job.setTemplateSchema(current);job.setTemplateValues(clean.toString());
    }
    public void freeze(GradingJob job) {
        String s=job.getTemplateSchema()==null?"":job.getTemplateSchema();
        if(job.getBatch()!=null&&!job.getBatch().isEmpty()) {
            batches.lockBatch(job.getBatch());String current=schema(job);
            if(!Objects.equals(s,current))throw new IllegalArgumentException("批次模板已变更，请刷新档案并保存后再确认");
            s=current;
        }
        job.setTemplateSchema(s);
        job.setLabelText(summary(job));
    }
    public String summary(GradingJob job) {
        if(job.getLabelText()!=null)return job.getLabelText();
        String s=schema(job);
        if(s.isEmpty()) {
            StringJoiner out=new StringJoiner(" ");
            for(String value:Arrays.asList(job.getCardName(),job.getSeries(),job.getCardYear(),job.getLanguage(),job.getCardNumber()))if(value!=null&&!value.trim().isEmpty())out.add(value.trim());
            return out.toString();
        }
        JsonNode values=parse(job.getTemplateValues()==null?"{}":job.getTemplateValues());StringBuilder out=new StringBuilder();String join="";
        for(JsonNode field:parse(s).path("fields")) {
            String key=field.path("key").asText();String v="cardName".equals(key)?job.getCardName():values.path(key).asText("");
            if(v!=null&&!v.trim().isEmpty()) {if(out.length()>0)out.append(join);out.append(v.trim());join="newline".equals(field.path("join").asText())?"\n":" ";}
        }
        return out.toString();
    }
}
