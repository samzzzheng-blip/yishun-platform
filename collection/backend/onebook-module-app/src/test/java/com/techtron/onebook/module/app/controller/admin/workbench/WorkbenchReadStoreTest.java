package com.techtron.onebook.module.app.controller.admin.workbench;

import com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkbenchReadStoreTest {
    @Test void receiptsPersistAreIsolatedAndNeverHideNewOrChangedItems() {
        var jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE app_workbench_read(user_id BIGINT, queue_key VARCHAR(64), item_token CHAR(64), PRIMARY KEY(user_id,queue_key,item_token))");
        var store = new WorkbenchReadStore(); ReflectionTestUtils.setField(store,"jdbc",jdbc);
        Map<String,Object> first=Map.of("id",1L,"update_time","2026-09-16 10:00:00");
        Map<String,Object> second=Map.of("id",2L,"update_time","2026-09-16 10:00:00");
        Map<String,Object> changed=Map.of("id",1L,"update_time","2026-09-16 11:00:00");
        try(var login=mockStatic(SecurityFrameworkUtils.class)) {
            login.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(143L);
            var snapshot=store.unread("getback-ship",List.of(first));assertEquals(1,snapshot.size());
            store.markRead("getback-ship",snapshot);store.markRead("getback-ship",snapshot);
            var reloaded=new WorkbenchReadStore();ReflectionTestUtils.setField(reloaded,"jdbc",jdbc);
            assertTrue(reloaded.unread("getback-ship",List.of(first)).isEmpty());
            assertEquals(1,reloaded.unread("getback-ship",List.of(first,second)).size());
            assertEquals(1,reloaded.unread("getback-ship",List.of(changed)).size());
            assertEquals(1,reloaded.unread("collection-review",List.of(first)).size());
            assertTrue(reloaded.unread("getback-ship",List.of()).isEmpty());
            login.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(144L);
            assertEquals(1,reloaded.unread("getback-ship",List.of(first)).size());
        }
    }
}
