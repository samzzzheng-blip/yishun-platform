package com.kiss.yishun.workflow;

import com.kiss.yishun.config.UploadConfig;
import com.kiss.yishun.controller.admin.CartoonController;
import com.kiss.yishun.controller.admin.PreciousController;
import com.kiss.yishun.controller.admin.RateController;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.*;
import java.util.*;
import static org.junit.Assert.*;

/** Runs only against disposable local fixtures, never production media. */
@RunWith(Parameterized.class)
public class LegacyPhotoRetentionTest {
    @Parameterized.Parameters(name="{0}")
    public static Collection<Object[]> controllers() {
        return Arrays.asList(new Object[][] {
            {RateController.class}, {PreciousController.class}, {CartoonController.class}
        });
    }
    @Parameterized.Parameter public Class<?> controllerType;
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private Object controller;
    private Path root, photo, sibling, outside;
    private final byte[] content = new byte[] {1, 3, 5, 7};

    @Before public void setup() throws Exception {
        root = temporary.newFolder("precious").toPath();
        photo = Files.createDirectories(root.resolve("YSLL8146/img")).resolve("old.jpg");
        sibling = Files.createDirectories(root.resolve("YSLL8136/img")).resolve("other.jpg");
        outside = temporary.newFile("outside.jpg").toPath();
        for (Path p : Arrays.asList(photo, sibling, outside)) Files.write(p, content);
        UploadConfig config = new UploadConfig();
        config.setDiskPreciousDir(root.toString());
        controller = controllerType.getDeclaredConstructor().newInstance();
        ReflectionTestUtils.setField(controller, "uploadConfig", config);
    }
    private void assertRetained() throws Exception {
        assertTrue(Files.isDirectory(root));
        for (Path p : Arrays.asList(photo, sibling, outside))
            assertArrayEquals(p.toString(), content, Files.readAllBytes(p));
    }
    @Test public void blankIdentifiersCannotDeleteSharedRoot() throws Exception {
        for (String id : new String[] {null, "", " ", "\t", ".", ".."}) {
            ReflectionTestUtils.invokeMethod(controller, "deleteResource", new Object[] {id});
            assertRetained();
        }
    }
    @Test public void pathLikeIdentifiersCannotDeleteAnyFiles() throws Exception {
        for (String id : new String[] {"../", "YSLL8146/..", "..\\", "C:\\", "D:/", "/",
                root.toString(), "YSLL8146.", "YSLL8146 ", "YSLL8146:stream"}) {
            ReflectionTestUtils.invokeMethod(controller, "deleteResource", id);
            assertRetained();
        }
    }
    @Test public void deletingOrRenumberingValidRecordRetainsHistoricalPhotos() throws Exception {
        ReflectionTestUtils.invokeMethod(controller, "deleteResource", "YSLL8146");
        assertRetained();
    }
    @Test public void replacingPhotosRetainsOriginalEvenWithMalformedInput() throws Exception {
        for (String id : new String[] {null, "", "..", "YSLL8146"}) {
            for (String url : new String[] {null, "", "../outside.jpg", "/upload/precious/YSLL8146/img/old.jpg"}) {
                ReflectionTestUtils.invokeMethod(controller, "deleteOldResource", id, url, "img");
                ReflectionTestUtils.invokeMethod(controller, "deleteOldResource", id, url, "..");
                assertRetained();
            }
        }
    }
}
