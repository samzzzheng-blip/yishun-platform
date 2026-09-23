package com.kiss.yishun.workflow;
import com.kiss.yishun.service.GradingPhotoStore;
import com.kiss.yishun.config.UploadConfig;
import com.kiss.yishun.dao.GradingJobDao;
import org.junit.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
public class GradingPhotoStoreTest {
    private GradingPhotoStore store;private GradingJobDao jobs;private Path publicDir;
    @Before public void setup() throws Exception {
        store=new GradingPhotoStore();jobs=mock(GradingJobDao.class);UploadConfig upload=mock(UploadConfig.class);
        publicDir=Files.createTempDirectory("grading-public-test-");
        when(upload.getDiskPreciousDir()).thenReturn(publicDir.toString());
        ReflectionTestUtils.setField(store,"directory",Files.createTempDirectory("grading-private-test-").toString());
        ReflectionTestUtils.setField(store,"jobs",jobs);ReflectionTestUtils.setField(store,"upload",upload);
    }
    @Test public void pendingPhotosRequireOwnerAndAreNotPublicFiles() throws Exception {
        String path=store.store(new byte[]{1,2,3},true,"owner");assertArrayEquals(new byte[]{1,2,3},store.read(path,"owner"));
        try {store.read(path,"other");fail();}catch(IllegalArgumentException expected){}
        assertFalse(Files.exists(publicDir.resolve("workflow").resolve(path.substring(path.lastIndexOf('/')+1))));
        try {store.read(GradingPhotoStore.PREFIX+"../x","owner");fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void onlyLinkedConfirmedPhotosCanBeReadByOtherStaff() throws Exception {
        String path=store.store(new byte[]{1},true,"owner");when(jobs.countPublishedPhoto(path)).thenReturn(1L);
        assertArrayEquals(new byte[]{1},store.read(path,"other"));
        String published=store.publish(path,"other");assertTrue(published.startsWith("/upload/precious/workflow/"));
        assertArrayEquals(new byte[]{1},Files.readAllBytes(publicDir.resolve("workflow").resolve(published.substring(published.lastIndexOf('/')+1))));
    }
}
