package com.techtron.onebook.module.app.service.inbound;
import com.techtron.onebook.module.app.service.collection.CollectionServiceImpl;
import com.techtron.onebook.module.app.controller.admin.collection.vo.CollectionUpdateReqVO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionRecordDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionRecordMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class InboundAuditTest {
 @Test void legacyUpdateCannotBypassParcelLock(){
  var service=new CollectionServiceImpl();var mapper=mock(CollectionMapper.class);var inbound=mock(InboundParcelService.class);
  ReflectionTestUtils.setField(service,"collectionMapper",mapper);ReflectionTestUtils.setField(service,"inboundParcels",inbound);
  doThrow(new IllegalStateException("parcel locked")).when(inbound).assertEditable(1L);
  var req=new com.techtron.onebook.module.app.controller.admin.collection.vo.CollectionSaveReqVO();
  req.setId(1L);req.setStatus(1);req.setStock(99);
  assertThrows(IllegalStateException.class,()->service.updateCollection(req));
  assertThrows(IllegalStateException.class,()->service.deleteCollection(1L));
  assertThrows(IllegalStateException.class,()->service.deleteCollectionListByIds(java.util.List.of(1L)));
  verify(mapper,never()).updateById(any(CollectionDO.class));verify(mapper,never()).deleteById(anyLong());
 }
 @Test void repeatedAuditDoesNotCreateDuplicateInventoryRecord() {
  var service=new CollectionServiceImpl();
  var mapper=mock(CollectionMapper.class);var records=mock(CollectionRecordMapper.class);var inbound=mock(InboundParcelService.class);
  ReflectionTestUtils.setField(service,"collectionMapper",mapper);ReflectionTestUtils.setField(service,"collectionRecordMapper",records);
  ReflectionTestUtils.setField(service,"inboundParcels",inbound);
  var c=CollectionDO.builder().id(1L).userId(10L).categoryId(2L).status(0).stock(1).build();
  when(mapper.selectByIdForUpdate(1L)).thenAnswer(inv->c);
  when(mapper.updateById(any(CollectionDO.class))).thenAnswer(inv->{c.setStatus(((CollectionDO)inv.getArgument(0)).getStatus());return 1;});
  var req=new CollectionUpdateReqVO();req.setId(1L);req.setStatus(1);req.setStock(99);
  service.auditCollection(req);service.auditCollection(req);
  verify(records,times(1)).insert(any(CollectionRecordDO.class));assertEquals(1,c.getStock());
 }
 @Test void failedParcelVerificationDoesNotChangeAuditOrInventory() {
  var service=new CollectionServiceImpl();var mapper=mock(CollectionMapper.class);
  var records=mock(CollectionRecordMapper.class);var inbound=mock(InboundParcelService.class);
  ReflectionTestUtils.setField(service,"collectionMapper",mapper);ReflectionTestUtils.setField(service,"collectionRecordMapper",records);
  ReflectionTestUtils.setField(service,"inboundParcels",inbound);
  when(mapper.selectByIdForUpdate(1L)).thenReturn(CollectionDO.builder().id(1L).status(0).stock(1).build());
  doThrow(new IllegalStateException("not received")).when(inbound).beforeAudit(1L,1);
  var req=new CollectionUpdateReqVO();req.setId(1L);req.setStatus(1);
  assertThrows(IllegalStateException.class,()->service.auditCollection(req));
  verify(mapper,never()).updateById(any(CollectionDO.class));verifyNoInteractions(records);
 }
}
