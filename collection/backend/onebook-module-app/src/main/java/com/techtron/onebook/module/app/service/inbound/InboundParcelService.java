package com.techtron.onebook.module.app.service.inbound;

import com.techtron.onebook.framework.common.exception.ServiceException;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.mysql.collection.CollectionMapper;
import com.techtron.onebook.module.app.service.notify.AppSubscribeMessageService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.time.LocalDateTime;

@Service
public class InboundParcelService {
    @Resource private JdbcTemplate jdbc;
    @Resource private CollectionMapper collections;
    @Resource private AppSubscribeMessageService notifications;
    @Value("${onebook.inbound.enabled:false}") private boolean enabled;
    @Value("${onebook.inbound.receiving-address:}") private String address;

    @Data public static class SaveRequest {
        private Long id;
        private Integer version;
        @NotNull @Pattern(regexp="courier|offline") private String deliveryMethod = "courier";
        @Size(max=60) private String carrier;
        @Size(max=64) private String trackingNo;
        @Size(max=500) private String remark = "";
        @NotEmpty @Size(max=100) private List<@NotNull Long> collectionIds;
        @Size(max=100) private Map<Long, @Size(max=500) String> itemNotes = new HashMap<>();
    }
    @Data public static class InspectRequest {
        @NotNull private Long parcelId;
        @NotNull private Long collectionId;
        @NotNull @Min(1) @Max(2) private Integer status;
        @Size(max=500) private String note = "";
        @Size(max=2000) private String evidence = "";
    }
    @Data public static class CorrectionRequest {
        @NotNull private Long id;
        @NotNull private Integer version;
        @NotBlank @Size(max=60) private String carrier;
        @NotBlank @Pattern(regexp="[A-Za-z0-9-]{6,64}") private String trackingNo;
        @NotBlank @Size(max=500) private String reason;
    }
    public Map<String,Object> config() {
        return Map.of("enabled", enabled, "receivingAddress", address,
            "receivingHours", "周一至周五 9:00–18:00", "offlineEnabled", true);
    }
    private void checkEnabled() {
        if (!enabled) fail("寄送登记暂未开放");
    }
    private static void fail(String message) { throw new ServiceException(2_001_020_001, message); }
    private Map<String,Object> parcel(Long id, Long userId, boolean lock) {
        checkEnabled();
        List<Map<String,Object>> rows = jdbc.queryForList("SELECT * FROM app_inbound_parcel WHERE id=?" + (lock ? " FOR UPDATE" : ""), id);
        if (rows.isEmpty()) fail("包裹不存在");
        Map<String,Object> p = rows.get(0);
        if (userId != null && ((Number)p.get("user_id")).longValue() != userId) fail("无权操作此包裹");
        return p;
    }
    public List<Map<String,Object>> list(Long userId, String tracking, int page) {
        return list(userId, tracking, page, null);
    }
    public List<Map<String,Object>> list(Long userId, String tracking, int page, Integer status) {
        checkEnabled();
        String sql = "SELECT p.*, (SELECT COUNT(*) FROM app_inbound_item i WHERE i.parcel_id=p.id) AS expected_count, " +
            "(SELECT COUNT(*) FROM app_inbound_item i WHERE i.parcel_id=p.id AND i.status=1) AS received_count " +
            "FROM app_inbound_parcel p WHERE 1=1";
        List<Object> args = new ArrayList<>();
        if (userId != null) { sql += " AND p.user_id=?"; args.add(userId); }
        if (status != null) { sql += " AND p.status=?"; args.add(status); }
        if (tracking != null && !tracking.isBlank()) {
            sql += " AND (p.tracking_no=? OR CAST(p.id AS CHAR)=?)";
            args.add(tracking.trim()); args.add(tracking.trim());
        }
        sql += " ORDER BY p.id DESC LIMIT 20 OFFSET ?";
        args.add((Math.max(1, Math.min(page, 10000))-1)*20);
        return jdbc.queryForList(sql, args.toArray());
    }
    public Map<String,Object> detail(Long id, Long userId) {
        Map<String,Object> p = parcel(id, userId, false);
        p.put("items", jdbc.queryForList("SELECT i.*, c.name, c.pic_url, c.pic_urls, c.stock, c.status AS audit_status " +
            "FROM app_inbound_item i LEFT JOIN app_collection c ON c.id=i.collection_id WHERE i.parcel_id=? ORDER BY i.collection_id", id));
        p.put("events", jdbc.queryForList("SELECT actor_type,detail,create_time FROM app_inbound_event WHERE parcel_id=? ORDER BY id DESC LIMIT 100", id));
        return p;
    }
    public List<Map<String,Object>> available(Long userId, Long parcelId) {
        checkEnabled();
        if (parcelId != null) parcel(parcelId, userId, false);
        return jdbc.queryForList("SELECT c.id,c.name,c.pic_url,c.stock FROM app_collection c " +
            "LEFT JOIN app_inbound_item i ON i.collection_id=c.id WHERE c.user_id=? AND c.deleted=0 AND c.status=0 " +
            "AND c.stock=1 AND (i.parcel_id IS NULL OR i.parcel_id=?) ORDER BY c.id DESC", userId, parcelId);
    }
    public List<Map<String,Object>> collectionStates(Long userId) {
        checkEnabled();
        return jdbc.queryForList("SELECT i.collection_id,i.status AS item_status,p.id AS parcel_id,p.status AS parcel_status " +
            "FROM app_inbound_item i JOIN app_inbound_parcel p ON p.id=i.parcel_id " +
            "JOIN app_collection c ON c.id=i.collection_id WHERE p.user_id=? AND c.status<>1 AND c.deleted=0",userId);
    }
    @Transactional(rollbackFor=Exception.class)
    public Long save(Long userId, SaveRequest req) {
        checkEnabled();
        List<Long> ids = req.getCollectionIds().stream().distinct().sorted().toList();
        if (ids.size() != req.getCollectionIds().size()) fail("同一藏品不能重复选择");
        String method = req.getDeliveryMethod();
        if (!"courier".equals(method) && !"offline".equals(method)) fail("请选择有效交付方式");
        boolean offline = "offline".equals(method);
        String carrier = offline ? "线下送达" : Objects.toString(req.getCarrier(), "").trim();
        String tracking = offline ? null : Objects.toString(req.getTrackingNo(), "").trim().toUpperCase(Locale.ROOT);
        if (!offline && (carrier.isBlank() || carrier.length()>60 || !tracking.matches("[A-Z0-9-]{6,64}")))
            fail("请选择快递公司并填写有效单号");
        Long id = req.getId();
        if (id != null) {
            Map<String,Object> p = parcel(id,userId,true);
            if (!List.of(0,3).contains(((Number)p.get("status")).intValue())) fail("仓库已收货，请由工作人员更正");
            if (req.getVersion()==null || ((Number)p.get("version")).intValue()!=req.getVersion()) fail("包裹已被修改，请刷新后重试");
        }
        // Lock collection rows in a fixed order; a unique collection_id also prevents double assignment.
        for (Long collectionId : ids) {
            CollectionDO c = collections.selectByIdForUpdate(collectionId);
            if (c==null || !Objects.equals(c.getUserId(), userId) || !Objects.equals(c.getStatus(),0)
                    || !Objects.equals(c.getStock(),1)) fail("仅可选择自己的待审核单件藏品，请刷新后重试");
            List<Map<String,Object>> links = jdbc.queryForList("SELECT parcel_id FROM app_inbound_item WHERE collection_id=?", collectionId);
            if (!links.isEmpty() && !Objects.equals(((Number)links.get(0).get("parcel_id")).longValue(),id)) fail("藏品已关联其他包裹");
        }
        Integer duplicates=jdbc.queryForObject("SELECT COUNT(*) FROM app_inbound_parcel WHERE carrier=? AND tracking_no=? AND id<>?",Integer.class,carrier,tracking,id==null?0L:id);
        if (duplicates!=null && duplicates>0) fail("此快递单号已登记，请勿重复提交");
        if (id==null) {
            jdbc.update("INSERT INTO app_inbound_parcel(user_id,carrier,tracking_no,remark,delivery_method) VALUES(?,?,?,?,?)",userId,carrier,tracking,Objects.toString(req.getRemark(),""),method);
            id=jdbc.queryForObject("SELECT LAST_INSERT_ID()",Long.class);
        } else {
            jdbc.update("UPDATE app_inbound_parcel SET carrier=?,tracking_no=?,remark=?,delivery_method=?,status=0,version=version+1,update_time=NOW() WHERE id=?",carrier,tracking,Objects.toString(req.getRemark(),""),method,id);
            jdbc.update("DELETE FROM app_inbound_item WHERE parcel_id=?",id);
        }
        for (Long collectionId:ids) jdbc.update("INSERT INTO app_inbound_item(collection_id,parcel_id,note) VALUES(?,?,?)",collectionId,id,
            req.getItemNotes()==null ? "" : Objects.toString(req.getItemNotes().get(collectionId),""));
        event(id,userId,"user","登记/修改交付信息："+carrier+" "+Objects.toString(tracking,"无需快递单号")+"；藏品："+ids+"；备注："+Objects.toString(req.getRemark(),"")+"；单件备注："+req.getItemNotes());
        return id;
    }
    @Transactional(rollbackFor=Exception.class)
    public void receive(Long adminId, Long id) {
        Map<String,Object> p=parcel(id,null,true);
        if (((Number)p.get("status")).intValue()!=0) return;
        jdbc.update("UPDATE app_inbound_parcel SET status=1,version=version+1,received_time=NOW(),update_time=NOW() WHERE id=?",id);
        event(id,adminId,"admin","公司已收到包裹，开始逐件核对");
        notifyUser(p,"已收货","包裹已收到，等待逐件核对");
    }
    @Transactional(rollbackFor=Exception.class)
    public void cancel(Long userId, Long id) {
        Map<String,Object> p=parcel(id,userId,true);
        if(((Number)p.get("status")).intValue()==3) return;
        if(((Number)p.get("status")).intValue()!=0) fail("公司已收货，不能撤销寄送登记");
        List<Long> ids=jdbc.queryForList("SELECT collection_id FROM app_inbound_item WHERE parcel_id=? ORDER BY collection_id",Long.class,id);
        for(Long collectionId:ids) collections.selectByIdForUpdate(collectionId);
        event(id,userId,"user","撤销寄送登记，释放藏品关联："+ids+"；原快递信息和历史记录保留");
        jdbc.update("DELETE FROM app_inbound_item WHERE parcel_id=?",id);
        jdbc.update("UPDATE app_inbound_parcel SET status=3,version=version+1,update_time=NOW() WHERE id=?",id);
    }
    @Transactional(rollbackFor=Exception.class)
    public void reopen(Long adminId, Long parcelId, Long collectionId) {
        Map<String,Object> p=parcel(parcelId,null,true);
        if(((Number)p.get("status")).intValue()!=1) fail("仅收货核对中的包裹可重新核对");
        CollectionDO c=collections.selectByIdForUpdate(collectionId);
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM app_inbound_item WHERE parcel_id=? AND collection_id=?",Integer.class,parcelId,collectionId);
        if(count==null || count!=1 || c==null || !Objects.equals(c.getStatus(),2)) fail("仅可重新核对此包裹内已驳回藏品");
        collections.updateById(new CollectionDO().setId(collectionId).setStatus(0));
        jdbc.update("UPDATE app_inbound_item SET status=0 WHERE collection_id=?",collectionId);
        event(parcelId,adminId,"admin","藏品 "+collectionId+" 重新进入核对，须核对实物后再次审核");
    }
    @Transactional(rollbackFor=Exception.class)
    public void correct(Long adminId, CorrectionRequest req) {
        Map<String,Object> p=parcel(req.getId(),null,true);
        if ("offline".equals(p.get("delivery_method"))) fail("线下送达无需更正快递信息");
        if(((Number)p.get("version")).intValue()!=req.getVersion()) fail("包裹信息已变化，请刷新后重试");
        String carrier=req.getCarrier().trim(), tracking=req.getTrackingNo().trim().toUpperCase(Locale.ROOT);
        Integer duplicates=jdbc.queryForObject("SELECT COUNT(*) FROM app_inbound_parcel WHERE carrier=? AND tracking_no=? AND id<>?",Integer.class,carrier,tracking,req.getId());
        if(duplicates!=null && duplicates>0) fail("此快递单号已登记");
        jdbc.update("UPDATE app_inbound_parcel SET carrier=?,tracking_no=?,version=version+1,update_time=NOW() WHERE id=?",carrier,tracking,req.getId());
        event(req.getId(),adminId,"admin","更正快递信息："+p.get("carrier")+" "+p.get("tracking_no")+" → "+carrier+" "+tracking+"；原因："+req.getReason());
    }
    @Transactional(rollbackFor=Exception.class)
    public void inspect(Long adminId, InspectRequest req) {
        Map<String,Object> p=parcel(req.getParcelId(),null,true);
        if (((Number)p.get("status")).intValue()!=1) fail("请先确认收货");
        CollectionDO c=collections.selectByIdForUpdate(req.getCollectionId());
        if(c==null || !Objects.equals(c.getStatus(),0)) fail("藏品已审核，不能重复核对");
        if(req.getStatus()==2 && (req.getNote()==null || req.getNote().isBlank())) fail("请填写少件、错件或破损等异常说明");
        int updated=jdbc.update("UPDATE app_inbound_item SET status=?,note=?,evidence=? WHERE parcel_id=? AND collection_id=?",
            req.getStatus(),Objects.toString(req.getNote(),""),Objects.toString(req.getEvidence(),""),req.getParcelId(),req.getCollectionId());
        if(updated==0) fail("藏品不属于此包裹");
        event(req.getParcelId(),adminId,"admin","藏品 "+req.getCollectionId()+ (req.getStatus()==1?" 已核对收到":" 异常待处理")+"："+Objects.toString(req.getNote(),""));
        if(req.getStatus()==2) notifyUser(p,"待补充","藏品存在异常，请查看包裹详情");
    }
    // Called with the collection row locked by the existing audit transaction.
    public void lockForAudit(Long collectionId) {
        if(!enabled) return;
        List<Long> ids=jdbc.queryForList("SELECT parcel_id FROM app_inbound_item WHERE collection_id=?",Long.class,collectionId);
        if(!ids.isEmpty()) parcel(ids.get(0),null,true);
    }
    public void beforeAudit(Long collectionId, Integer status) {
        if(!enabled || !Objects.equals(status,1)) return;
        List<Map<String,Object>> links=jdbc.queryForList("SELECT i.status,p.status AS parcel_status FROM app_inbound_item i JOIN app_inbound_parcel p ON p.id=i.parcel_id WHERE i.collection_id=?",collectionId);
        if(!links.isEmpty() && (((Number)links.get(0).get("status")).intValue()!=1 ||
                ((Number)links.get(0).get("parcel_status")).intValue()==0)) fail("请先在入库包裹中确认收货并核对该藏品");
    }
    public void afterAudit(Long collectionId, Long adminId, Integer status) {
        if(!enabled) return;
        List<Map<String,Object>> links=jdbc.queryForList("SELECT p.* FROM app_inbound_item i JOIN app_inbound_parcel p ON p.id=i.parcel_id WHERE i.collection_id=?",collectionId);
        if(links.isEmpty()) return;
        Map<String,Object> p=links.get(0); Long id=((Number)p.get("id")).longValue();
        event(id,adminId==null?0L:adminId,"admin","藏品 "+collectionId+(Objects.equals(status,1)?" 审核通过，已入库":" 审核驳回，请查看详情"));
        // Completion is derived from original collection audit results; no stock is created here.
        jdbc.update("UPDATE app_inbound_parcel SET status=2,update_time=NOW() WHERE id=? AND NOT EXISTS " +
            "(SELECT 1 FROM app_inbound_item i JOIN app_collection c ON c.id=i.collection_id WHERE i.parcel_id=? AND c.status<>1)",id,id);
        notifyUser(p,Objects.equals(status,1)?"已通过":"未通过","藏品审核结果已更新，请查看包裹详情");
    }
    public void assertEditable(Long collectionId) {
        if(!enabled) return;
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM app_inbound_item WHERE collection_id=?",Integer.class,collectionId);
        if(count!=null && count>0) fail("藏品已登记寄送，请先从未收货包裹移除，或联系工作人员处理");
    }
    // Deletion must retain parcel protection even if new inbound registration is disabled.
    public void assertUnlinkedForDelete(Long collectionId) {
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM app_inbound_item WHERE collection_id=?",Integer.class,collectionId);
        if(count==null || count>0) fail("藏品已关联包裹，不能删除。尚未交付请先撤销登记；已交付请联系工作人员");
    }
    private void event(Long id,Long actor,String type,String detail) {
        jdbc.update("INSERT INTO app_inbound_event(parcel_id,actor_id,actor_type,detail) VALUES(?,?,?,?)",id,actor,type,detail);
    }
    private void notifyUser(Map<String,Object> p,String status,String tip) {
        notifications.sendAfterCommit(((Number)p.get("user_id")).longValue(),AppSubscribeMessageService.REVIEW_RESULT,"pages/collection/parcel",
            Map.of("phrase1",status,"thing2","藏品寄送入库","thing3",AppSubscribeMessageService.thing(tip),
                "time4",AppSubscribeMessageService.time(LocalDateTime.now()),"thing5","包裹 "+p.get("id")));
    }
}
