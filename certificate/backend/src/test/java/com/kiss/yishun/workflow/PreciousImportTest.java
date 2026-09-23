package com.kiss.yishun.workflow;

import com.kiss.yishun.config.UploadConfig;
import com.kiss.yishun.entity.Precious;
import com.kiss.yishun.entity.vo.RateImportResult;
import com.kiss.yishun.service.PreciousImportService;
import com.kiss.yishun.service.PreciousService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.util.Base64;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PreciousImportTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private PreciousImportService importer;
    private PreciousService service;
    @Before public void setup() throws Exception {
        service = mock(PreciousService.class);
        importer = new PreciousImportService();
        UploadConfig config = new UploadConfig();
        config.setDiskPreciousDir(temp.getRoot().toString());
        config.setReturnPreciousDir("/upload/precious/");
        ReflectionTestUtils.setField(importer, "preciousService", service);
        ReflectionTestUtils.setField(importer, "uploadConfig", config);
    }
    private Sheet sheet(Workbook book) {
        Sheet s = book.createSheet(); Row h = s.createRow(0);
        for(int i=0;i<PreciousImportService.HEADERS.length;i++) h.createCell(i).setCellValue(PreciousImportService.HEADERS[i]);
        return s;
    }
    private RateImportResult run(Workbook book) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); book.write(out);
        return importer.importFile(new MockMultipartFile("file", book instanceof HSSFWorkbook ? "test.xls" : "test.xlsx", "application/octet-stream", out.toByteArray()), "测试操作员");
    }
    @Test public void onlyCertificateRequiredXlsxAndXls() throws Exception {
        for(Workbook book : new Workbook[]{new XSSFWorkbook(), new HSSFWorkbook()}) {
            sheet(book).createRow(1).createCell(0).setCellValue("00123");
            assertEquals(1, run(book).getImported()); book.close();
        }
        ArgumentCaptor<Precious> cap = ArgumentCaptor.forClass(Precious.class);
        verify(service,times(2)).addPrecious(cap.capture());
        Precious p = cap.getValue();
        assertEquals("00123",p.getCertNumber()); assertEquals("",p.getSigner());
        assertEquals("",p.getItemType()); assertEquals("",p.getImgUrl()); assertEquals(0,p.getStatus());
    }
    @Test public void allTwelveColumns() throws Exception {
        try(Workbook book=new XSSFWorkbook()) {
            Row r=sheet(book).createRow(1);
            String[] data={"A123","/upload/a.jpg","姓名","卡牌","9.5","上海","2026.09","亲笔","https://example.com/e.jpg","https://example.com/v.mp4","9.0","备注"};
            for(int i=0;i<data.length;i++) r.createCell(i).setCellValue(data[i]);
            assertEquals(1,run(book).getImported());
            ArgumentCaptor<Precious> cap=ArgumentCaptor.forClass(Precious.class);verify(service).addPrecious(cap.capture());
            Precious p=cap.getValue();
            assertArrayEquals(data,new String[]{p.getCertNumber(),p.getImgUrl(),p.getSigner(),p.getItemType(),p.getPublishActivity(),p.getPublishCity(),p.getPublishTime(),p.getPublishSign(),p.getEvidenceImg(),p.getEvidenceVideo(),p.getScore(),p.getRemark()});
        }
    }
    @Test public void missingDuplicateAndUnsafeNumbersAreSkipped() throws Exception {
        when(service.existSameCertNumber("EXISTS")).thenReturn(1);
        try(Workbook book=new XSSFWorkbook()) {
            Sheet s=sheet(book);
            String[] nums={"", "EXISTS", "NEW", "new", "../danger", "   "};
            for(int i=0;i<nums.length;i++){Row r=s.createRow(i+1);r.createCell(0).setCellValue(nums[i]);r.createCell(2).setCellValue("姓名");}
            RateImportResult result=run(book);
            assertEquals(6,result.getTotal());assertEquals(1,result.getImported());assertEquals(5,result.getSkipped());
            verify(service,times(1)).addPrecious(any(Precious.class));
            assertEquals(0,temp.getRoot().list().length);
        }
    }
    @Test public void mainAndEvidencePicturesUseTheirOwnColumns() throws Exception {
        try(Workbook book=new XSSFWorkbook()) {
            Sheet s=sheet(book);s.createRow(1).createCell(0).setCellValue("PIC");
            byte[] png=Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aVZkAAAAASUVORK5CYII=");
            int index=book.addPicture(png,Workbook.PICTURE_TYPE_PNG);
            Drawing<?> d=s.createDrawingPatriarch();
            for(int col:new int[]{1,8}){ClientAnchor a=book.getCreationHelper().createClientAnchor();a.setRow1(1);a.setRow2(2);a.setCol1(col);a.setCol2(col+1);d.createPicture(a,index);}
            assertEquals(1,run(book).getImported());
            ArgumentCaptor<Precious> cap=ArgumentCaptor.forClass(Precious.class);verify(service).addPrecious(cap.capture());
            assertTrue(cap.getValue().getImgUrl().contains("/PIC/img/"));
            assertTrue(cap.getValue().getEvidenceImg().contains("/PIC/evidenceImg/"));
            assertEquals(1,Files.list(temp.getRoot().toPath().resolve("PIC/img")).count());
        }
    }
    @Test(expected=IllegalArgumentException.class) public void emptyWorkbookDoesNotReportSuccess() throws Exception {
        try(Workbook book=new XSSFWorkbook()){sheet(book);run(book);}
    }
    @Test public void localAttachmentsAreSavedAndPathsReplaced() throws Exception {
        try(Workbook book=new XSSFWorkbook()) {
            Row row=sheet(book).createRow(1);row.createCell(0).setCellValue("LOCAL1");
            row.createCell(1).setCellValue("C:\\photos\\a.jpg");
            row.createCell(9).setCellValue("file:///C:/videos/v.mp4");
            ByteArrayOutputStream out=new ByteArrayOutputStream();book.write(out);
            MockMultipartFile excel=new MockMultipartFile("file","test.xlsx","",out.toByteArray());
            assertEquals(2,importer.attachmentReferences(excel).size());
            java.util.Map<String,org.springframework.web.multipart.MultipartFile> files=new java.util.HashMap<>();
            files.put("C:\\photos\\a.jpg",new MockMultipartFile("attachments","a.jpg","image/jpeg",new byte[]{1,2,3}));
            files.put("file:///C:/videos/v.mp4",new MockMultipartFile("attachments","v.mp4","video/mp4",new byte[]{4,5,6}));
            assertEquals(1,importer.importFile(excel,"tester",files).getImported());
            ArgumentCaptor<Precious> cap=ArgumentCaptor.forClass(Precious.class);verify(service).addPrecious(cap.capture());
            assertTrue(cap.getValue().getImgUrl().startsWith("/upload/precious/LOCAL1/img/excel-"));
            assertTrue(cap.getValue().getEvidenceVideo().endsWith(".mp4"));
            assertTrue(cap.getValue().getEvidenceVideo().contains("/evidenceVideo/"));
        }
    }
    @Test public void missingLocalAttachmentDoesNotWriteRowOrFiles() throws Exception {
        try(Workbook book=new XSSFWorkbook()) {
            Row row=sheet(book).createRow(1);row.createCell(0).setCellValue("MISS1");row.createCell(9).setCellValue("C:\\missing.mp4");
            RateImportResult result=run(book);assertEquals(0,result.getImported());assertEquals(1,result.getSkipped());
            verify(service,never()).addPrecious(any(Precious.class));assertEquals(0,temp.getRoot().list().length);
        }
    }
    private MockMultipartFile wpsFixture(String replaceEntry, String replacement) throws Exception {
        String fixture=System.getProperty("wps.fixture");
        org.junit.Assume.assumeNotNull(fixture);
        byte[] data=Files.readAllBytes(Paths.get(fixture));
        if(replaceEntry!=null){
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            try(java.util.zip.ZipInputStream in=new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(data));
                java.util.zip.ZipOutputStream zip=new java.util.zip.ZipOutputStream(out)){
                java.util.zip.ZipEntry entry;
                while((entry=in.getNextEntry())!=null){
                    zip.putNextEntry(new java.util.zip.ZipEntry(entry.getName()));
                    if(entry.getName().equals(replaceEntry))zip.write(replacement.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    else{byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)zip.write(buffer,0,n);}
                    zip.closeEntry();
                }
            }
            data=out.toByteArray();
        }
        return new MockMultipartFile("file","wps.xlsx","",data);
    }
    @Test public void realWpsWorkbookPreservesBothPicturesExactly() throws Exception {
        MockMultipartFile excel=wpsFixture(null,null);
        java.util.List<String> refs=importer.attachmentReferences(excel);
        assertEquals(1,refs.size());
        assertTrue(refs.get(0).endsWith("9月20日(7).mp4"));
        java.util.Map<String,org.springframework.web.multipart.MultipartFile> files=new java.util.HashMap<>();
        files.put(refs.get(0),new MockMultipartFile("attachments","v.mp4","video/mp4",new byte[]{1,2,3}));
        RateImportResult result=importer.importFile(excel,"test",files);
        assertEquals(result.getErrors().toString(),1,result.getImported());
        ArgumentCaptor<Precious> cap=ArgumentCaptor.forClass(Precious.class);verify(service).addPrecious(cap.capture());
        assertEquals("YSAB18365",cap.getValue().getCertNumber());
        try(java.util.zip.ZipFile zip=new java.util.zip.ZipFile(System.getProperty("wps.fixture"))){
            String[] entries={"xl/media/image2.png","xl/media/image3.png"};
            String[] urls={cap.getValue().getImgUrl(),cap.getValue().getEvidenceImg()};
            for(int i=0;i<2;i++){
                ByteArrayOutputStream out=new ByteArrayOutputStream();
                try(java.io.InputStream in=zip.getInputStream(zip.getEntry(entries[i]))){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
                assertArrayEquals(out.toByteArray(),Files.readAllBytes(temp.getRoot().toPath().resolve(urls[i].substring("/upload/precious/".length()))));
            }
        }
    }
    @Test public void missingWpsRelationshipIsRejectedWithoutWrites() throws Exception {
        MockMultipartFile excel=wpsFixture("xl/_rels/cellimages.xml.rels","<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"/>");
        try{importer.attachmentReferences(excel);fail("missing picture accepted");}catch(IllegalArgumentException expected){}
        verify(service,never()).addPrecious(any(Precious.class));assertEquals(0,temp.getRoot().list().length);
    }
    @Test public void wpsXmlExternalEntitiesAreRejected() throws Exception {
        MockMultipartFile excel=wpsFixture("xl/cellimages.xml","<!DOCTYPE x [<!ENTITY e SYSTEM \"file:///should-not-read\">]><x>&e;</x>");
        try{importer.attachmentReferences(excel);fail("DTD accepted");}catch(org.xml.sax.SAXException expected){}
        verify(service,never()).addPrecious(any(Precious.class));assertEquals(0,temp.getRoot().list().length);
    }
}
