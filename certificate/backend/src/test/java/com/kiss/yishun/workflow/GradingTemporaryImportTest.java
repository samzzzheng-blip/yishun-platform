package com.kiss.yishun.workflow;

import com.kiss.yishun.dao.*;
import com.kiss.yishun.entity.*;
import com.kiss.yishun.entity.vo.*;
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
import org.springframework.mock.web.MockMultipartFile;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class) @DataJpaTest
@org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
@ContextConfiguration(classes=GradingTemporaryImportTest.Config.class)
@TestPropertySource(properties={"spring.jpa.hibernate.ddl-auto=create-drop","spring.jpa.database-platform=org.hibernate.dialect.H2Dialect","spring.jpa.show-sql=false","grading.private-photo-dir=${java.io.tmpdir}/yishun-temporary-import-test-photos"})
public class GradingTemporaryImportTest {
    @Configuration @EntityScan(basePackageClasses=GradingJob.class) @EnableJpaRepositories(basePackageClasses=GradingJobDao.class)
    @Import({GradingWorkflowService.class,GradingTemplateService.class,GradingPhotoStore.class,GradingTemporaryService.class,GradingTemporaryImportService.class,RateImportService.class})
    static class Config {
        @Bean public com.kiss.yishun.config.UploadConfig upload(){return org.mockito.Mockito.mock(com.kiss.yishun.config.UploadConfig.class);}
        @Bean public RateService rates(){return org.mockito.Mockito.mock(RateService.class);}
    }
    @Autowired GradingTemporaryImportService importer;
    @Autowired RateImportService parser;
    @Autowired GradingTemporaryService temporary;
    @Autowired GradingJobDao jobs;
    @Autowired RateDao rates;
    @Autowired GradingBatchSequenceDao sequences;
    @Autowired GradingBatchDayDao days;
    @Autowired GradingEventDao events;
    @Autowired RateService formal;
    @Before public void clean(){events.deleteAll();jobs.deleteAll();rates.deleteAll();sequences.deleteAll();days.deleteAll();org.mockito.Mockito.reset(formal);}
    private MockMultipartFile sheet(int count,boolean xlsx,boolean bad) throws Exception {
        try(Workbook w=xlsx?new XSSFWorkbook():new HSSFWorkbook()){
            Sheet s=w.createSheet("Sheet1");String[] headers={"标签","表面","居中","边缘","角落","总分","分数标志","二维码","编号","图片"};
            Row h=s.createRow(0);for(int i=0;i<headers.length;i++)h.createCell(i).setCellValue(headers[i]);
            for(int i=0;i<count;i++){Row r=s.createRow(i+3);r.createCell(0).setCellValue("2025 系列\n测试卡"+i+"\n#001");r.createCell(1).setCellValue(bad&&i==count-1?"bad":"9.0");r.createCell(7).setCellValue("qrcode_"+(100+i)+".png");}
            ByteArrayOutputStream out=new ByteArrayOutputStream();w.write(out);return new MockMultipartFile("file","cards."+(xlsx?"xlsx":"xls"),"application/octet-stream",out.toByteArray());
        }
    }
    @Test public void oneHundredBlankCertificatesStayPrivateAndRetriedOnce() throws Exception {
        MockMultipartFile f=sheet(100,false,false);RateImportResult r=importer.importFile(f,"demo");assertEquals(100,r.getImported());assertEquals(0,rates.count());
        assertEquals(100,temporary.list("","",1,"demo").getTotalElements());assertEquals(0,temporary.list("","",1,"other").getTotalElements());
        GradingJob j=jobs.findAll().get(0);assertEquals("TEMPORARY",j.getStage());assertNull(j.getRateId());assertEquals("",j.getCertNumber());assertEquals("2025 系列\n测试卡0\n#001",j.getLabelText());assertEquals("测试卡0",j.getCardName());
        List<GradingJob> batch=jobs.findByBatchAndCreatedByOrderByIdAsc(j.getBatch(),"demo");for(int i=0;i<100;i++)assertEquals(Long.valueOf(i+1),batch.get(i).getBatchNumber());
        assertEquals(0,importer.importFile(f,"demo").getImported());assertEquals(100,jobs.count());assertEquals(100,importer.importFile(f,"other").getImported());
        org.mockito.Mockito.verifyZeroInteractions(formal);
    }
    @Test public void badRowRollsBackWholeFile() throws Exception {try{importer.importFile(sheet(2,true,true),"demo");fail();}catch(IllegalArgumentException e){assertTrue(e.getMessage().contains("第5行"));}assertEquals(0,jobs.count());assertEquals(0,sequences.count());}
    @Test public void xlsxSupportedAndFormalParserNameUnchanged() throws Exception {MockMultipartFile f=sheet(2,true,false);assertEquals("测试卡0",parser.parseFile(f).get(0).getRateName());assertEquals(2,importer.importFile(f,"demo").getImported());}
    @Test public void xmlBlankScoresAndDuplicateCertificatesAreAllowed() throws Exception {
        String xml="<评级记录><记录><编号>123</编号><标签>第一行\n名称\n第三行</标签></记录><记录><编号>123</编号><标签>另一个名称</标签></记录></评级记录>";
        assertEquals(2,importer.importFile(new MockMultipartFile("file","cards.xml","application/xml",xml.getBytes("UTF-8")),"demo").getImported());
        for(GradingJob j:jobs.findAll()){assertEquals("",j.getCertNumber());assertEquals("",j.getScore());assertNull(j.getRateId());}
    }
    @Test public void purgeRemovesImportedRowsAndAllowsReimport() throws Exception {
        MockMultipartFile f=sheet(2,false,false);importer.importFile(f,"demo");
        GradingTemporaryService.Range r=new GradingTemporaryService.Range();String d=LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();r.setFrom(d);r.setTo(d);r.setToken(temporary.preview(r,"demo").getToken());temporary.purge(r,"demo");
        assertEquals(0,jobs.count());assertEquals(0,events.count());assertEquals(0,sequences.count());assertEquals(0,rates.count());assertEquals(2,importer.importFile(f,"demo").getImported());
    }
    @Test public void suppliedCustomerFileIfProvided() throws Exception {
        String fixture=System.getProperty("temporary.import.fixture");Assume.assumeTrue(fixture!=null);
        MockMultipartFile f=new MockMultipartFile("file","customer.xls","application/vnd.ms-excel",Files.readAllBytes(Paths.get(fixture)));
        RateImportResult result=importer.importFile(f,"demo");assertEquals(100,result.getTotal());assertEquals(100,result.getImported());assertEquals(0,result.getSkipped());assertEquals(0,rates.count());
        assertEquals("贪心栗鼠",jobs.findAll().get(0).getCardName());assertTrue(jobs.findAll().get(0).getLabelText().contains("#090"));
    }
    @Test public void invalidPhotoRollsBackRowsBatchAndNewPrivateFiles() throws Exception {
        Path root=Paths.get(System.getProperty("java.io.tmpdir"),"yishun-temporary-import-test-photos");Files.createDirectories(root);
        long before;try(java.util.stream.Stream<Path> files=Files.list(root)){before=files.count();}
        ByteArrayOutputStream image=new ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(1,1,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",image);
        String xml="<评级记录><记录><标签>A</标签><照片>data:image/png;base64,"+Base64.getEncoder().encodeToString(image.toByteArray())+"</照片></记录><记录><标签>B</标签><照片>missing.png</照片></记录></评级记录>";
        try{importer.importFile(new MockMultipartFile("file","cards.xml","application/xml",xml.getBytes("UTF-8")),"demo");fail();}catch(IllegalArgumentException e){assertTrue(e.getMessage().contains("本地文件名"));}
        assertEquals(0,jobs.count());assertEquals(0,sequences.count());assertEquals(0,rates.count());
        try(java.util.stream.Stream<Path> files=Files.list(root)){assertEquals(before,files.count());}
    }
}
