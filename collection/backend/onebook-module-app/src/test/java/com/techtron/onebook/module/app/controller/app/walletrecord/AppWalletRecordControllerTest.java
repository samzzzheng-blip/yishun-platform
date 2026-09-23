package com.techtron.onebook.module.app.controller.app.walletrecord;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class AppWalletRecordControllerTest {
    @Test void rejectsAmbiguousReferences() {
        var a=Map.<String,Object>of("id",1L);
        assertNull(AppWalletRecordController.unique(List.of(a,a)));
        assertNull(AppWalletRecordController.unique(List.of()));
        assertEquals(a,AppWalletRecordController.unique(List.of(a)));
    }
    @Test void rejectsInvalidBusinessIdentifiers() {
        for(String s:List.of("0","-1","1 OR 1=1","99999999999999999999999","","withdraw-reject:3"))
            assertNull(AppWalletRecordController.positiveId(s));
        assertEquals(3L,AppWalletRecordController.positiveId("3"));
    }
    @Test void unknownTypeDoesNotQueryOrLink() {
        var jdbc=mock(JdbcTemplate.class);var controller=new AppWalletRecordController();
        ReflectionTestUtils.setField(controller,"jdbc",jdbc);
        assertNull(controller.resolve(1L,2L,99,"3"));
        assertNull(controller.resolve(null,2L,3,"3"));
        verifyNoInteractions(jdbc);
    }
    @Test void withdrawalQueriesRequireOwnerAndTenant() {
        var jdbc=mock(JdbcTemplate.class);var controller=new AppWalletRecordController();
        ReflectionTestUtils.setField(controller,"jdbc",jdbc);
        {
            assertNull(controller.resolve(7L,2L,8,"3"));
            verify(jdbc).queryForList(contains("user_id=? AND tenant_id=?"),eq(3L),eq(7L),isNull());
        }
    }
    @Test void deniedLedgerDoesNotResolveAnyOrder() {
        var jdbc=mock(JdbcTemplate.class);var controller=new AppWalletRecordController();
        ReflectionTestUtils.setField(controller,"jdbc",jdbc);
        assertEquals(404,controller.get(123L).getCode());
        verify(jdbc).queryForList(contains("w.user_id=? AND w.user_type=1"),eq(123L),isNull(),isNull(),isNull());
        verifyNoMoreInteractions(jdbc);
    }
}
