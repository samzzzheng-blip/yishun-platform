package com.kiss.yishun.workflow;
import com.kiss.yishun.dao.*;
import com.kiss.yishun.entity.*;
import com.kiss.yishun.service.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.TestPropertySource;
import java.util.*;
import static org.junit.Assert.*;

/** Embedded database only; verifies the three-state batch publication flow. */
@RunWith(SpringRunner.class)
@DataJpaTest
@org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
@ContextConfiguration(classes=GradingWorkflowTest.Config.class)
@TestPropertySource(properties={"spring.jpa.hibernate.ddl-auto=create-drop","spring.jpa.database-platform=org.hibernate.dialect.H2Dialect","spring.jpa.show-sql=false"})
public class GradingWorkflowTest {
    @Configuration @EntityScan(basePackageClasses=GradingJob.class)
    @EnableJpaRepositories(basePackageClasses=GradingJobDao.class)
    @Import({GradingWorkflowService.class,GradingBatchPublishService.class,GradingPhotoStore.class,GradingTemplateService.class,GradingTemporaryService.class})
    static class Config {@Bean public com.kiss.yishun.config.UploadConfig upload(){return org.mockito.Mockito.mock(com.kiss.yishun.config.UploadConfig.class);}}
    @Autowired GradingWorkflowService workflow;
    @Autowired GradingBatchPublishService publish;
    @Autowired GradingTemplateService templates;
    @Autowired GradingJobDao jobs;
    @Autowired RateDao rates;
    @Autowired GradingEventDao events;
    @Autowired GradingBatchSequenceDao sequences;
    @Autowired GradingBatchTemplateDao bindings;
    @Autowired GradingLabelTemplateDao templateDao;
    @Autowired GradingBatchDayDao days;
    @Autowired GradingTemporaryService temporary;
    @Before public void reset(){events.deleteAll();jobs.deleteAll();rates.deleteAll();sequences.deleteAll();bindings.deleteAll();templateDao.deleteAll();days.deleteAll();}
    private GradingWorkflowService.ManageRequest manage(String action,String batch){GradingWorkflowService.ManageRequest r=new GradingWorkflowService.ManageRequest();r.setAction(action);r.setBatch(batch);return r;}
    private void apply(GradingWorkflowService.ManageRequest r){r.setToken(workflow.managePreview(r,"tester").getToken());workflow.manageApply(r,"tester");}
    @Test public void automaticBatchNameAndRetry(){String request=UUID.randomUUID().toString();String a=workflow.newBatch(request,"demo").getBatch();java.time.LocalDate d=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));String prefix=d.getMonthValue()+"."+d.getDayOfMonth()+"demo";assertEquals(prefix+"1",a);assertEquals(a,workflow.newBatch(request,"demo").getBatch());assertEquals(prefix+"2",workflow.newBatch(UUID.randomUUID().toString(),"demo").getBatch());}
    @Test public void exactBatchAndStageFilters(){intake("A","tester");ready("A");intake("AB","tester");intake("A","other");assertEquals(2,workflow.list("","","A",1,"tester").getTotalElements());assertEquals(1,workflow.list("COLLECTING","","A",1,"tester").getTotalElements());assertEquals(1,workflow.list("PUBLISHING","","A",1,"tester").getTotalElements());}
    @Test public void bulkDeleteAllPagesPreservesOtherPrivateJobs(){for(int i=0;i<23;i++)intake("A","tester");GradingJob other=intake("A","other");GradingWorkflowService.ManageRequest r=manage("DELETE","A");assertEquals(23,workflow.managePreview(r,"tester").getCount());apply(r);assertEquals(0,workflow.list("","","A",1,"tester").getTotalElements());assertEquals(24,jobs.count());assertNull(jobs.findById(other.getId()).get().getDeletedAt());assertEquals(Long.valueOf(25),intake("A","tester").getBatchNumber());}
    @Test public void deleteCompletedPreservesFormalRate(){GradingJob j=ready("A");GradingBatchPublishService.Preview p=publish.preview(j.getId(),"200","tester");publish.publish(j.getId(),"200",p.getToken(),"tester");apply(manage("DELETE","A"));assertNotNull(rates.findRateByCertNumber("200"));assertEquals(0,workflow.list("DONE","",1,"tester").getTotalElements());fails("删除",()->workflow.get(j.getId(),"tester"));fails("删除",()->workflow.create(j,"tester"));}
    @Test public void staleBulkPreviewDoesNotDeleteAnything(){GradingJob j=intake("A","tester");GradingWorkflowService.ManageRequest r=manage("DELETE","A");r.setToken(workflow.managePreview(r,"tester").getToken());intake("A","tester");fails("变化",()->workflow.manageApply(r,"tester"));assertNull(jobs.findById(j.getId()).get().getDeletedAt());}
    @Test public void moveResetsPendingAndAllocatesTargetNumber(){GradingJob j=ready("A");intake("B","tester");GradingWorkflowService.ManageRequest r=manage("MOVE","A");r.setTargetBatch("B");apply(r);GradingJob moved=workflow.get(j.getId(),"tester");assertEquals("B",moved.getBatch());assertEquals("COLLECTING",moved.getStage());assertEquals(Long.valueOf(2),moved.getBatchNumber());assertEquals("测试卡",moved.getCardName());assertEquals("9.5",moved.getScore());}
    @Test public void deletedDraftDoesNotBlockPublication(){GradingJob j=ready("A"),draft=intake("A","tester");GradingWorkflowService.ManageRequest r=manage("DELETE","");r.setId(draft.getId());apply(r);assertEquals(1,publish.preview(j.getId(),"100","tester").getRows().size());}
    @Test public void completedMoveKeepsCertificate(){GradingJob j=ready("A");GradingBatchPublishService.Preview p=publish.preview(j.getId(),"300","tester");publish.publish(j.getId(),"300",p.getToken(),"tester");intake("B","tester");GradingWorkflowService.ManageRequest r=manage("MOVE","A");r.setTargetBatch("B");apply(r);assertEquals("DONE",workflow.get(j.getId(),"tester").getStage());assertEquals("300",workflow.get(j.getId(),"tester").getCertNumber());assertNotNull(rates.findRateByCertNumber("300"));}
    private void fails(String text,Runnable action){try{action.run();fail("Expected rejection: "+text);}catch(IllegalArgumentException e){assertTrue(e.getMessage(),e.getMessage().contains(text));}}
    @Test public void nextCardUsesSameOwnerBatchAndNumericOrder(){
        GradingJob a=intake("next","tester");intake("next","other");intake("another","tester");
        GradingJob b=intake("next","tester");GradingJob c=intake("next","tester");
        workflow.saveBatchNumber(b.getId(),b.getVersion(),"10","tester");
        assertEquals(c.getId(),workflow.nextInBatch(a.getId(),"tester").getId());
        assertEquals(b.getId(),workflow.nextInBatch(c.getId(),"tester").getId());
        assertNull(workflow.nextInBatch(b.getId(),"tester"));
        fails("无权",()->workflow.nextInBatch(a.getId(),"other"));
    }
    @Test public void nextCardSkipsDeletedAndPublishingAcrossPages(){
        GradingJob a=intake("next","tester");ready("next");GradingJob deleted=intake("next","tester");
        GradingWorkflowService.ManageRequest r=manage("DELETE","");r.setId(deleted.getId());apply(r);
        GradingJob expected=intake("next","tester");for(int i=0;i<25;i++)intake("next","tester");
        assertEquals(expected.getId(),workflow.nextInBatch(a.getId(),"tester").getId());
        assertEquals("COLLECTING",workflow.get(a.getId(),"tester").getStage());
    }
    private GradingTemporaryService.Range today(){GradingTemporaryService.Range r=new GradingTemporaryService.Range();String d=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")).toString();r.setFrom(d);r.setTo(d);return r;}
    private GradingJob temp(String batch){GradingJob j=ready(batch);GradingBatchPublishService.Preview p=publish.preview(j.getId(),"","tester");assertTrue(p.isTemporary());publish.publish(j.getId(),"",p.getToken(),"tester");return jobs.findById(j.getId()).get();}
    @Test public void blankNumberCompletesPrivatelyWithoutFormalRecord(){GradingJob j=temp("temp");assertEquals("TEMPORARY",j.getStage());assertNull(j.getRateId());assertEquals("",j.getCertNumber());assertEquals(0,rates.count());assertEquals(0,workflow.list("","",1,"tester").getTotalElements());assertEquals(1,temporary.list("","",1,"tester").getTotalElements());assertEquals(0,temporary.list("","",1,"other").getTotalElements());assertNull(sequences.findById("temp").get().getStartNumber());fails("临时",()->workflow.save(j.getId(),j,"tester"));fails("临时",()->workflow.advance(j.getId(),j.getVersion(),"NEXT",null,"tester"));fails("临时",()->workflow.get(j.getId(),"tester"));}
    @Test public void temporaryPurgePhysicallyDeletesEventsAndEmptyBatch(){GradingJob j=temp("temp");assertFalse(events.findByJobIdOrderByCreatedAtDesc(j.getId()).isEmpty());GradingTemporaryService.Range r=today();r.setToken(temporary.preview(r,"tester").getToken());assertEquals(1,temporary.purge(r,"tester").getCount());assertFalse(jobs.existsById(j.getId()));assertTrue(events.findByJobIdOrderByCreatedAtDesc(j.getId()).isEmpty());assertFalse(sequences.existsById("temp"));assertFalse(bindings.existsById("temp"));}
    @Test public void temporaryPurgePreservesOtherOwnersDatesAndFormalRecords(){GradingJob selected=temp("temp"),old=temp("old"),other=temp("other");old.setUpdatedAt(0);jobs.saveAndFlush(old);other.setCreatedBy("other");jobs.saveAndFlush(other);GradingJob formal=ready("formal");GradingBatchPublishService.Preview p=publish.preview(formal.getId(),"900","tester");publish.publish(formal.getId(),"900",p.getToken(),"tester");GradingTemporaryService.Range r=today();assertEquals(1,temporary.preview(r,"tester").getCount());r.setToken(temporary.preview(r,"tester").getToken());temporary.purge(r,"tester");assertFalse(jobs.existsById(selected.getId()));assertTrue(jobs.existsById(old.getId()));assertTrue(jobs.existsById(other.getId()));assertNotNull(rates.findRateByCertNumber("900"));assertTrue(jobs.existsById(formal.getId()));}
    @Test public void temporaryPurgeRequiresDatesAndFreshConfirmation(){GradingJob j=temp("temp");fails("日期",()->temporary.preview(new GradingTemporaryService.Range(),"tester"));GradingTemporaryService.Range r=today();r.setToken(temporary.preview(r,"tester").getToken());temp("second");fails("变化",()->temporary.purge(r,"tester"));assertTrue(jobs.existsById(j.getId()));assertFalse(events.findByJobIdOrderByCreatedAtDesc(j.getId()).isEmpty());}
    @Test public void temporaryExcludedFromLaterBatchPublish(){temp("same");GradingJob fresh=ready("same");GradingBatchPublishService.Preview p=publish.preview(fresh.getId(),"1000","tester");assertEquals(1,p.getRows().size());publish.publish(fresh.getId(),"1000",p.getToken(),"tester");assertEquals(1,rates.count());assertEquals(1,temporary.list("","",1,"tester").getTotalElements());}
    @Test public void temporaryCannotBypassIncompleteBatch(){GradingJob j=ready("temp");intake("temp","tester");fails("待整理",()->publish.preview(j.getId(),"","tester"));assertEquals(0,temporary.list("","",1,"tester").getTotalElements());}
    @Test public void temporaryDatesUseInclusiveBeijingDays(){GradingJob a=temp("a"),b=temp("b"),c=temp("c");long begin=java.time.LocalDate.parse("2026-09-11").atStartOfDay(java.time.ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();a.setUpdatedAt(begin);b.setUpdatedAt(begin+86400000L-1);c.setUpdatedAt(begin+86400000L);jobs.saveAll(Arrays.asList(a,b,c));jobs.flush();assertEquals(2,temporary.list("2026-09-11","2026-09-11",1,"tester").getTotalElements());fails("格式",()->temporary.list("bad","",1,"tester"));fails("晚于",()->temporary.list("2026-09-12","2026-09-11",1,"tester"));}
    @Test public void temporaryPurgeCoversAllPages(){GradingJob anchor=null;for(int i=0;i<23;i++)anchor=ready("many");GradingBatchPublishService.Preview p=publish.preview(anchor.getId(),"","tester");publish.publish(anchor.getId(),"",p.getToken(),"tester");assertEquals(20,temporary.list("","",1,"tester").getContent().size());GradingTemporaryService.Range r=today();r.setToken(temporary.preview(r,"tester").getToken());assertEquals(23,temporary.purge(r,"tester").getCount());assertEquals(0,jobs.count());assertEquals(0,events.count());}
    private GradingJob intake(String batch,String actor){GradingJob j=new GradingJob();j.setJobNumber("G-"+UUID.randomUUID());j.setBatch(batch);return workflow.create(j,actor);}
    private GradingJob ready(String batch){GradingJob j=intake(batch,"tester");j.setCardName("测试卡");j.setSurface("10");j.setCenter("9.5");j.setEdge("9");j.setCorner("10");j.setScore("9.5");j=workflow.save(j.getId(),j,"tester");return workflow.advance(j.getId(),j.getVersion(),"NEXT",null,"tester");}
    @Test public void emptyIntakeSequenceAndRetryAreStable(){GradingJob a=intake("A","tester");assertEquals(Long.valueOf(1),a.getBatchNumber());assertEquals("",a.getCertNumber());assertEquals(a.getId(),workflow.create(a,"tester").getId());assertEquals(Long.valueOf(2),intake("A","other").getBatchNumber());assertEquals(Long.valueOf(1),intake("B","tester").getBatchNumber());}
    @Test public void moveToAnotherBatchAllocatesNewNumber(){GradingJob a=intake("A","tester");intake("B","tester");a.setBatch("B");a=workflow.save(a.getId(),a,"tester");assertEquals(Long.valueOf(2),a.getBatchNumber());assertEquals(Long.valueOf(2),workflow.save(a.getId(),a,"tester").getBatchNumber());}
    @Test public void submitGoesDirectlyToPublishingWithoutPhotos(){GradingJob j=ready("A");assertEquals("PUBLISHING",j.getStage());assertEquals(0,rates.count());fails("整批发布",()->workflow.advance(j.getId(),j.getVersion(),"NEXT",null,"tester"));}
    @Test public void incompleteScoresCannotBeSubmitted(){GradingJob j=intake("A","tester");j.setCardName("有名称");j=workflow.save(j.getId(),j,"tester");GradingJob value=j;fails("完整评分",()->workflow.advance(value.getId(),value.getVersion(),"NEXT",null,"tester"));}
    @Test public void invalidGradeRejected(){GradingJob j=intake("A","tester");j.setScore("9.2");fails("评分",()->workflow.save(j.getId(),j,"tester"));}
    @Test public void batchPublishesDirectlyToDoneWithMappedCertificates(){
        GradingJob a=ready("A"),b=ready("A");GradingBatchPublishService.Preview p=publish.preview(a.getId(),"2022007","tester");
        assertEquals("2022007",p.getRows().get(0).getCertNumber());assertEquals("2022008",p.getRows().get(1).getCertNumber());
        publish.publish(a.getId(),p.getStartNumber(),p.getToken(),"tester");
        assertEquals(2,rates.count());assertEquals("DONE",workflow.get(a.getId(),"other").getStage());assertEquals("2022008",workflow.get(b.getId(),"tester").getCertNumber());
        assertEquals("",rates.findRateByCertNumber("2022007").getImgUrl());assertEquals(2,workflow.list("DONE","",1,"other").getTotalElements());
        fails("已发布",()->publish.publish(a.getId(),p.getStartNumber(),p.getToken(),"tester"));assertEquals(2,rates.count());
    }
    @Test public void leadingZerosAndGapsPreserved(){GradingJob a=intake("A","tester");a.setBatch("B");workflow.save(a.getId(),a,"tester");GradingJob b=ready("A");assertEquals("000008",publish.preview(b.getId(),"000007","tester").getRows().get(0).getCertNumber());}
    @Test public void conflictDoesNotPartiallyPublish(){
        GradingJob existing=ready("B");GradingBatchPublishService.Preview first=publish.preview(existing.getId(),"2022008","tester");publish.publish(existing.getId(),first.getStartNumber(),first.getToken(),"tester");
        GradingJob a=ready("A");ready("A");fails("已存在",()->publish.preview(a.getId(),"2022007","tester"));assertEquals(1,rates.count());assertNull(workflow.get(a.getId(),"tester").getRateId());
    }
    @Test public void changedVersionInvalidatesPreview(){GradingJob a=ready("A");GradingBatchPublishService.Preview p=publish.preview(a.getId(),"100","tester");workflow.advance(a.getId(),a.getVersion(),"REOPEN",null,"tester");fails("待整理",()->publish.publish(a.getId(),"100",p.getToken(),"tester"));assertEquals(0,rates.count());}
    @Test public void addingNewCardInvalidatesWholeBatch(){GradingJob a=ready("A");GradingBatchPublishService.Preview p=publish.preview(a.getId(),"100","tester");ready("A");fails("变化",()->publish.publish(a.getId(),"100",p.getToken(),"tester"));assertEquals(0,rates.count());}
    @Test public void otherAccountPrivateCardsAreNotPublished(){GradingJob a=ready("A"),other=intake("A","other");GradingBatchPublishService.Preview p=publish.preview(a.getId(),"100","tester");assertEquals(1,p.getRows().size());publish.publish(a.getId(),"100",p.getToken(),"tester");assertNull(workflow.get(other.getId(),"other").getRateId());fails("无权",()->workflow.get(other.getId(),"tester"));}
    @Test public void draftBlocksBatchAndBlankBatchMustBeNamed(){GradingJob a=ready("A");intake("A","tester");fails("待整理",()->publish.preview(a.getId(),"100","tester"));GradingJob b=ready("");fails("收卡批次",()->publish.preview(b.getId(),"100","tester"));}
    @Test public void overflowAndBadStartRejected(){GradingJob a=ready("A");ready("A");fails("超过18位",()->publish.preview(a.getId(),"999999999999999999","tester"));fails("正整数",()->publish.preview(a.getId(),"0","tester"));}
    @Test public void publishedStartIsFixedForLaterCards(){GradingJob a=ready("A");GradingBatchPublishService.Preview p=publish.preview(a.getId(),"100","tester");publish.publish(a.getId(),"100",p.getToken(),"tester");GradingJob b=ready("A");assertEquals("101",publish.preview(b.getId(),"100","tester").getRows().get(0).getCertNumber());fails("固定",()->publish.preview(b.getId(),"200","tester"));}
    @Test public void completedCannotBeEditedInWorkflow(){GradingJob a=ready("A");GradingBatchPublishService.Preview p=publish.preview(a.getId(),"100","tester");publish.publish(a.getId(),"100",p.getToken(),"tester");GradingJob done=workflow.get(a.getId(),"tester");fails("评级管理",()->workflow.save(done.getId(),done,"tester"));}
    @Test public void templateChangeInvalidatesPreview(){
        GradingJob a=ready("A");GradingBatchPublishService.Preview p=publish.preview(a.getId(),"100","tester");
        GradingLabelTemplate t=new GradingLabelTemplate();t.setName("信息");t.setFieldsJson("[{\"key\":\"cardName\",\"label\":\"名称\",\"join\":\"space\"}]");t=templates.save(t,"tester");
        templates.select(a.getId(),a.getVersion(),t.getId(),"","SINGLE","tester");
        fails("变化",()->publish.publish(a.getId(),"100",p.getToken(),"tester"));assertEquals(0,rates.count());
    }
    @Test public void manualSummaryPersistsThroughPublishWithoutChangingFields(){
        GradingJob j=ready("summary");String content="2026  系列\n测试卡（人工修订）\n#123  SR";
        GradingJob saved=workflow.saveSummary(j.getId(),j.getVersion(),content,j.getTemplateSchema(),"tester");
        assertEquals("PUBLISHING",saved.getStage());assertEquals("测试卡",saved.getCardName());assertEquals("9.5",saved.getScore());
        assertEquals(content,workflow.get(j.getId(),"tester").getLabelText());
        GradingBatchPublishService.Preview p=publish.preview(j.getId(),"800","tester");assertEquals(content,p.getRows().get(0).getLabelText());publish.publish(j.getId(),"800",p.getToken(),"tester");
        GradingJob done=workflow.get(j.getId(),"tester");assertEquals(content,done.getLabelText());assertEquals("测试卡",rates.findRateByCertNumber("800").getRateName());
        fails("仅待发布",()->workflow.saveSummary(done.getId(),done.getVersion(),"变更",done.getTemplateSchema(),"tester"));
    }
    @Test public void summaryRequiresPendingOwnerAndCurrentVersion(){
        GradingJob draft=intake("summary","tester");fails("仅待发布",()->workflow.saveSummary(draft.getId(),draft.getVersion(),"x","","tester"));
        GradingJob j=ready("other");fails("无权",()->workflow.saveSummary(j.getId(),j.getVersion(),"x","","other"));
        workflow.saveSummary(j.getId(),j.getVersion(),"x",j.getTemplateSchema(),"tester");fails("修改",()->workflow.saveSummary(j.getId(),j.getVersion(),"y",j.getTemplateSchema(),"tester"));
    }
    @Test public void restoreAutomaticAndDeliberatelyEmptySummary(){
        GradingJob j=ready("summary");j=workflow.saveSummary(j.getId(),j.getVersion(),"manual",j.getTemplateSchema(),"tester");
        j=workflow.saveSummary(j.getId(),j.getVersion(),null,j.getTemplateSchema(),"tester");assertEquals("测试卡",templates.summary(j));
        j=workflow.saveSummary(j.getId(),j.getVersion(),"",j.getTemplateSchema(),"tester");assertEquals("",templates.summary(j));
        GradingBatchPublishService.Preview p=publish.preview(j.getId(),"810","tester");publish.publish(j.getId(),"810",p.getToken(),"tester");assertEquals("",workflow.get(j.getId(),"tester").getLabelText());
    }
    @Test public void summaryUpdateInvalidatesPublishPreview(){
        GradingJob j=ready("summary");GradingBatchPublishService.Preview p=publish.preview(j.getId(),"820","tester");workflow.saveSummary(j.getId(),j.getVersion(),"changed",j.getTemplateSchema(),"tester");fails("变化",()->publish.publish(j.getId(),"820",p.getToken(),"tester"));assertEquals(0,rates.count());
    }
    @Test public void oversizedSummaryRejected(){GradingJob j=ready("summary");fails("5000",()->workflow.saveSummary(j.getId(),j.getVersion(),String.join("",Collections.nCopies(5001,"字")),j.getTemplateSchema(),"tester"));assertNull(workflow.get(j.getId(),"tester").getLabelText());}
    @Test public void editedNumberPreservesStageAndMovesAutomaticCounter(){GradingJob j=ready("number");GradingJob edited=workflow.saveBatchNumber(j.getId(),j.getVersion(),"7","tester");assertEquals(Long.valueOf(7),edited.getBatchNumber());assertEquals("PUBLISHING",edited.getStage());assertEquals("测试卡",edited.getCardName());assertEquals(Long.valueOf(8),intake("number","tester").getBatchNumber());}
    @Test public void duplicateNumberCannotBeSavedAcrossOwners(){GradingJob j=intake("number","tester");intake("number","other");fails("已占用",()->workflow.saveBatchNumber(j.getId(),j.getVersion(),"2","tester"));assertEquals(Long.valueOf(1),workflow.get(j.getId(),"tester").getBatchNumber());assertEquals(Long.valueOf(3),intake("number","tester").getBatchNumber());}
    @Test public void numberEditRequiresCurrentVersionAndOwner(){GradingJob j=intake("number","tester");fails("无权",()->workflow.saveBatchNumber(j.getId(),j.getVersion(),"8","other"));workflow.saveBatchNumber(j.getId(),j.getVersion(),"8","tester");fails("变化",()->workflow.saveBatchNumber(j.getId(),j.getVersion(),"9","tester"));}
    @Test public void numberValidationAndCompletedLock(){GradingJob j=ready("number");for(String v:Arrays.asList("","0","-1","1.5","01","9999999999999999"))fails(v.length()>15?"长度":"正整数",()->workflow.saveBatchNumber(j.getId(),j.getVersion(),v,"tester"));GradingBatchPublishService.Preview p=publish.preview(j.getId(),"700","tester");publish.publish(j.getId(),"700",p.getToken(),"tester");GradingJob done=workflow.get(j.getId(),"tester");fails("已完成",()->workflow.saveBatchNumber(done.getId(),done.getVersion(),"9","tester"));}
    @Test public void numberChangeInvalidatesPublishPreviewAndUsesNewMapping(){GradingJob j=ready("number");GradingBatchPublishService.Preview old=publish.preview(j.getId(),"1000","tester");workflow.saveBatchNumber(j.getId(),j.getVersion(),"5","tester");fails("变化",()->publish.publish(j.getId(),"1000",old.getToken(),"tester"));GradingBatchPublishService.Preview next=publish.preview(j.getId(),"1000","tester");assertEquals("1004",next.getRows().get(0).getCertNumber());publish.publish(j.getId(),"1000",next.getToken(),"tester");assertNotNull(rates.findRateByCertNumber("1004"));}
    @Test public void publicationRejectsDuplicateInputEvenBeforeDatabaseConstraint(){GradingJob a=ready("number"),b=ready("number");b.setBatchNumber(a.getBatchNumber());fails("重复",()->org.springframework.test.util.ReflectionTestUtils.invokeMethod(org.springframework.test.util.AopTestUtils.getUltimateTargetObject(publish),"plan","number","1000","tester",Arrays.asList(a,b)));assertEquals(0,rates.count());}
}
