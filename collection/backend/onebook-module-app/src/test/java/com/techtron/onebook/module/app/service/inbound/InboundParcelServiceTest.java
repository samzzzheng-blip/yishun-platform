package com.techtron.onebook.module.app.service.inbound;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.service.notify.AppSubscribeMessageService;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InboundParcelServiceTest {
 @Test void deletionRequiresNoParcelEvenWhenInboundDisabled() {
  Long id=tx.execute(s->service.save(10L,request(1L)));
  ReflectionTestUtils.setField(service,"enabled",false);
  assertThrows(RuntimeException.class,()->service.assertUnlinkedForDelete(1L));
  assertDoesNotThrow(()->service.assertUnlinkedForDelete(2L));
 }
 JdbcTemplate jdbc; InboundParcelService service; CollectionMapper mapper; TransactionTemplate tx;
 @BeforeEach void setup() throws Exception {
  var ds=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");
  jdbc=new JdbcTemplate(ds); tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
  String sql=Files.readString(Path.of("../sql/mysql/inbound_parcel_20260907.sql"));
  for(String statement:sql.split(";")) if(!statement.isBlank()) jdbc.execute(statement);
  String offlineSql=Files.readString(Path.of("../sql/mysql/inbound_offline_20260910.sql"));
  for(String statement:offlineSql.split(";")) if(!statement.isBlank()) jdbc.execute(statement);
  jdbc.execute("CREATE TABLE app_collection(id bigint PRIMARY KEY,user_id bigint,status int,stock int,name varchar(100),pic_url varchar(200),pic_urls varchar(2000),deleted int DEFAULT 0)");
  service=new InboundParcelService(); mapper=mock(CollectionMapper.class);
  ReflectionTestUtils.setField(service,"jdbc",jdbc); ReflectionTestUtils.setField(service,"collections",mapper);
  ReflectionTestUtils.setField(service,"notifications",mock(AppSubscribeMessageService.class));
  ReflectionTestUtils.setField(service,"enabled",true); ReflectionTestUtils.setField(service,"address","");
  for(long i=1;i<=3;i++){
   jdbc.update("INSERT INTO app_collection(id,user_id,status,stock,name) VALUES(?,10,0,1,?)",i,"藏品"+i);
   when(mapper.selectByIdForUpdate(i)).thenReturn(CollectionDO.builder().id(i).userId(10L).status(0).stock(1).build());
  }
 }
 InboundParcelService.SaveRequest request(Long... ids){
  var r=new InboundParcelService.SaveRequest(); r.setCarrier("顺丰速运");r.setTrackingNo("SF123456");r.setCollectionIds(Arrays.asList(ids));return r;
 }
 Long save(InboundParcelService.SaveRequest r){return tx.execute(s->service.save(10L,r));}
 @Test void offlineWithoutTrackingSupportsSeparateDeliveriesAndSearch(){
  var r=request(1L,2L);r.setDeliveryMethod("offline");r.setCarrier(null);r.setTrackingNo(null);
  Long id=save(r);
  var p=service.detail(id,10L);
  assertEquals("offline",p.get("delivery_method")); assertNull(p.get("tracking_no"));
  assertEquals(2,((List<?>)p.get("items")).size());
  assertEquals(1,service.list(10L,String.valueOf(id),1).size());
  assertTrue(service.list(20L,String.valueOf(id),1).isEmpty());
  var another=request(3L);another.setDeliveryMethod("offline");save(another);
  assertEquals(2,service.list(10L,null,1).size());
  assertThrows(RuntimeException.class,()->service.beforeAudit(1L,1));
  tx.executeWithoutResult(s->service.receive(99L,id));
  assertThrows(RuntimeException.class,()->service.beforeAudit(1L,1));
  var inspect=new InboundParcelService.InspectRequest();inspect.setParcelId(id);inspect.setCollectionId(1L);inspect.setStatus(1);
  tx.executeWithoutResult(s->service.inspect(99L,inspect));
  assertDoesNotThrow(()->service.beforeAudit(1L,1));
 }
 @Test void courierStillRequiresTrackingAndUnknownMethodRejected(){
  var r=request(1L);r.setTrackingNo(""); assertThrows(RuntimeException.class,()->save(r));
  r.setDeliveryMethod("other");assertThrows(RuntimeException.class,()->save(r));
 }
 @Test void customerCanChangeMethodBeforeReceiptAndOfflineCanBeCancelled(){
  Long id=save(request(1L));var r=request(1L);r.setId(id);r.setVersion(0);r.setDeliveryMethod("offline");save(r);
  assertNull(service.detail(id,10L).get("tracking_no"));
  r.setDeliveryMethod("courier");r.setVersion(1);save(r);
  assertEquals("SF123456",service.detail(id,10L).get("tracking_no"));
  r.setDeliveryMethod("offline");r.setVersion(2);save(r);
  tx.executeWithoutResult(s->service.cancel(10L,id));
  assertEquals(3,service.available(10L,null).size());
 }
 @Test void detailIncludesGalleryWhenCoverIsEmpty(){
  String photos="[\"https://example.com/front.jpg\",\"https://example.com/back.jpg\"]";
  jdbc.update("UPDATE app_collection SET pic_urls=? WHERE id=1",photos);
  Long id=save(request(1L));
  var items=(List<Map<String,Object>>)service.detail(id,10L).get("items");
  assertEquals(photos,items.get(0).get("pic_urls"));
  assertNull(items.get(0).get("pic_url"));
 }
 @Test void oneParcelContainsMultipleItemsAndLeavesStockUnaudited(){
  Long id=save(request(1L,2L));
  assertEquals(2,((List<?>)service.detail(id,10L).get("items")).size());
  assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM app_collection WHERE id IN (1,2) AND status=0",Integer.class));
  assertEquals(1,service.available(10L,null).size());
 }
 @Test void anotherUserCannotReadOrChangeParcel(){
  Long id=save(request(1L));
  assertThrows(RuntimeException.class,()->service.detail(id,20L));
  var req=request(2L);req.setId(id);req.setVersion(0);
  assertThrows(RuntimeException.class,()->tx.execute(s->service.save(20L,req)));
 }
 @Test void foreignCollectionAndDuplicateAssignmentRejected(){
  when(mapper.selectByIdForUpdate(3L)).thenReturn(CollectionDO.builder().id(3L).userId(20L).status(0).stock(1).build());
  assertThrows(RuntimeException.class,()->save(request(3L)));
  save(request(1L));
  var r=request(1L);r.setTrackingNo("SF999999");
  assertThrows(RuntimeException.class,()->save(r));
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM app_inbound_parcel",Integer.class));
 }
 @Test void customerCanCorrectBeforeReceiptButNotAfter(){
  Long id=save(request(1L));var r=request(1L,2L);r.setId(id);r.setVersion(0);r.setTrackingNo("SF999999");save(r);
  assertThrows(RuntimeException.class,()->save(r));
  tx.executeWithoutResult(s->service.receive(99L,id));r.setVersion(2);
  assertThrows(RuntimeException.class,()->save(r));
  assertEquals(3,((List<?>)service.detail(id,10L).get("events")).size());
 }
 @Test void auditRequiresReceivedAndVerifiedItem(){
  Long id=save(request(1L,2L));
  assertThrows(RuntimeException.class,()->service.beforeAudit(1L,1));
  tx.executeWithoutResult(s->service.receive(99L,id));
  assertThrows(RuntimeException.class,()->service.beforeAudit(1L,1));
  var req=new InboundParcelService.InspectRequest();req.setParcelId(id);req.setCollectionId(1L);req.setStatus(1);
  tx.executeWithoutResult(s->service.inspect(99L,req));
  assertDoesNotThrow(()->service.beforeAudit(1L,1));
  assertThrows(RuntimeException.class,()->service.beforeAudit(2L,1));
 }
 @Test void exceptionRequiresExplanationAndCanBeResolved(){
  Long id=save(request(1L));tx.executeWithoutResult(s->service.receive(99L,id));
  var r=new InboundParcelService.InspectRequest();r.setParcelId(id);r.setCollectionId(1L);r.setStatus(2);
  assertThrows(RuntimeException.class,()->tx.executeWithoutResult(s->service.inspect(99L,r)));
  r.setNote("尚未找到，请客户确认");tx.executeWithoutResult(s->service.inspect(99L,r));
  assertThrows(RuntimeException.class,()->service.beforeAudit(1L,1));
  r.setStatus(1);r.setNote("已找到并核对");tx.executeWithoutResult(s->service.inspect(99L,r));
  assertDoesNotThrow(()->service.beforeAudit(1L,1));
 }
 @Test void disabledFeatureDoesNotAccessNewTablesForLegacyAudit(){
  ReflectionTestUtils.setField(service,"enabled",false);
  jdbc.execute("DROP TABLE app_inbound_item");
  assertDoesNotThrow(()->service.beforeAudit(1L,1));
  assertDoesNotThrow(()->service.lockForAudit(1L));
  assertThrows(RuntimeException.class,()->save(request(1L)));
 }
 @Test void editingPreservesAuditTrailAndRejectsDuplicateTracking(){
  Long id=save(request(1L));assertThrows(RuntimeException.class,()->save(request(2L)));
  var r=request(2L);r.setId(id);r.setVersion(0);r.setRemark("第一件留到下次寄");save(r);
  assertEquals(2,((List<?>)service.detail(id,10L).get("events")).size());
  assertEquals(2,service.available(10L,null).size());
 }
 @Test void cancelSingleItemParcelReleasesItAndCanBeReregistered(){
  Long id=save(request(1L));
  tx.executeWithoutResult(s->service.cancel(10L,id));
  assertEquals(3,((Number)service.detail(id,10L).get("status")).intValue());
  assertEquals(3,service.available(10L,null).size());
  var r=request(1L);r.setId(id);r.setVersion(1);save(r);
  assertEquals(0,((Number)service.detail(id,10L).get("status")).intValue());
  tx.executeWithoutResult(s->service.receive(99L,id));
  assertThrows(RuntimeException.class,()->tx.executeWithoutResult(s->service.cancel(10L,id)));
 }
 @Test void rejectedReceivedItemCanBeReopenedWithoutBecomingAudited(){
  Long id=save(request(1L));tx.executeWithoutResult(s->service.receive(99L,id));
  when(mapper.selectByIdForUpdate(1L)).thenReturn(CollectionDO.builder().id(1L).userId(10L).status(2).stock(1).build());
  tx.executeWithoutResult(s->service.reopen(99L,id,1L));
  verify(mapper).updateById(argThat((CollectionDO c)->c.getStatus()==0 && c.getId()==1L));
  assertThrows(RuntimeException.class,()->service.beforeAudit(1L,1));
 }
}
