package com.techtron.onebook.module.app.controller.admin.workbench;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.techtron.onebook.framework.security.core.service.SecurityFrameworkService;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import com.techtron.onebook.module.app.dal.mysql.auction.AuctionMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkbenchControllerTest {
    private final WorkbenchController controller = new WorkbenchController();
    private final SecurityFrameworkService security = mock(SecurityFrameworkService.class);
    private final CollectionMapper collections = mock(CollectionMapper.class);
    private final WorkbenchReadStore readStore = mock(WorkbenchReadStore.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    WorkbenchControllerTest() {
        ReflectionTestUtils.setField(controller,"security",security);
        ReflectionTestUtils.setField(controller,"collectionMapper",collections);
        ReflectionTestUtils.setField(controller,"jdbc",jdbc);
        ReflectionTestUtils.setField(controller,"readStore",readStore);
        when(readStore.unread(anyString(),anyList())).thenAnswer(inv -> ((List<Map<String,Object>>) inv.getArgument(1)).stream().map(row->row.get("id").toString()).toList());
    }
    @Test void noPermissionMeansNoCountsAndNoQueries() {
        assertTrue(controller.pending().getData().isEmpty());
        verifyNoInteractions(collections,jdbc);
    }
    @Test void onlyAuthorizedQueueIsReturned() {
        when(security.hasPermission("app:collection:query")).thenReturn(true);
        when(collections.selectMaps(any())).thenReturn(records(8));
        var rows=controller.pending().getData();
        assertEquals(1,rows.size());assertEquals(8L,rows.get(0).count());assertTrue(rows.get(0).available());
        verifyNoInteractions(jdbc);
    }
    @Test void queryFailureIsNotReportedAsZero() {
        when(security.hasPermission("app:collection:query")).thenReturn(true);
        when(collections.selectMaps(any())).thenThrow(new IllegalStateException("test failure"));
        var row=controller.pending().getData().get(0);
        assertFalse(row.available());assertNull(row.count());
    }
    @Test void emptyQueueIsSuccessfulZero() {
        when(security.hasPermission("app:collection:query")).thenReturn(true);
        when(collections.selectMaps(any())).thenReturn(records(0));
        var row=controller.pending().getData().get(0);
        assertTrue(row.available());assertEquals(0L,row.count());
    }
    @Test void parcelsAreSeparatedByWorkflowStatus() {
        ReflectionTestUtils.setField(controller,"inboundEnabled",true);
        when(security.hasPermission("app:collection:query")).thenReturn(true);
        when(collections.selectMaps(any())).thenReturn(records(2));
        when(jdbc.queryForList("SELECT id, update_time FROM app_inbound_parcel WHERE status=0")).thenReturn(records(5));
        when(jdbc.queryForList("SELECT id, update_time FROM app_inbound_parcel WHERE status=1")).thenReturn(records(3));
        var rows=controller.pending().getData();
        assertEquals(3,rows.size());assertEquals(5L,rows.get(1).count());assertEquals(3L,rows.get(2).count());
    }
    @Test void settlementExcludesSelfOperatedListings() {
        var mapper=mock(YikoujiaMapper.class);ReflectionTestUtils.setField(controller,"yikoujiaMapper",mapper);
        when(security.hasPermission("app:yikoujia:query")).thenReturn(true);
        when(mapper.selectMaps(any())).thenReturn(records(1));
        assertEquals(3,controller.pending().getData().size());
        var captor=ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper,times(3)).selectMaps(captor.capture());
        var settlement=captor.getAllValues().get(1);
        assertTrue(settlement.getSqlSegment().contains("collection_id IS NOT NULL"));
        assertTrue(settlement.getParamNameValuePairs().containsValue(4));
    }
    @Test void delistOnlyIncludesActiveAuctionApplications() {
        var mapper=mock(AuctionMapper.class);ReflectionTestUtils.setField(controller,"auctionMapper",mapper);
        when(security.hasPermission("app:auction:query")).thenReturn(true);
        when(mapper.selectMaps(any())).thenReturn(records(1));
        assertEquals(4,controller.pending().getData().size());
        var captor=ArgumentCaptor.forClass(QueryWrapper.class);verify(mapper,times(4)).selectMaps(captor.capture());
        var delist=captor.getAllValues().get(3);
        assertTrue(delist.getSqlSegment().contains("delist_status"));
        assertEquals(2,delist.getParamNameValuePairs().size());
        assertTrue(delist.getParamNameValuePairs().values().stream().allMatch(v->Integer.valueOf(1).equals(v)));
    }
    private static List<Map<String,Object>> records(int count) {
        return IntStream.range(0,count).mapToObj(i->Map.<String,Object>of("id",(long)i)).toList();
    }
    @Test void markReadDoesNotHideArrivalsOutsideDisplayedSnapshot() {
        when(security.hasPermission("app:collection:query")).thenReturn(true);
        when(collections.selectMaps(any())).thenReturn(records(3));
        controller.markRead(new WorkbenchController.ReadRequest("collection-review",List.of("0","1")));
        verify(readStore).markRead("collection-review",List.of("0","1"));
    }
    @Test void cannotMarkUnauthorizedQueueRead() {
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
            ()->controller.markRead(new WorkbenchController.ReadRequest("collection-review",List.of("0"))));
        verify(readStore,never()).markRead(anyString(),anyList());
    }
    @Test void processedRecordIsNotMarkedFromStaleSnapshot() {
        when(security.hasPermission("app:collection:query")).thenReturn(true);
        when(collections.selectMaps(any())).thenReturn(records(0));
        controller.markRead(new WorkbenchController.ReadRequest("collection-review",List.of("0")));
        verify(readStore).markRead("collection-review",List.of());
    }
}
