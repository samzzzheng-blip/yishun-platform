package com.techtron.onebook.module.app.controller.app.walletrecord;

import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionPageReqVO;
import com.techtron.onebook.module.pay.service.wallet.PayWalletTransactionService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static com.techtron.onebook.framework.common.pojo.CommonResult.success;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils.getLoginUser;

/** Read-only ledger/order views. Authorize using wallet ownership before resolving business references. */
@RestController
@RequestMapping("/app/wallet-record")
public class AppWalletRecordController {
    @Resource private PayWalletTransactionService transactions;
    @Resource private JdbcTemplate jdbc;

    @GetMapping("/page")
    public CommonResult<PageResult<Map<String,Object>>> page(@Valid AppPayWalletTransactionPageReqVO req) {
        Long user = getLoginUserId();
        var page = transactions.getWalletTransactionPage(user, 1, req);
        List<Map<String,Object>> rows = new ArrayList<>();
        for (var t : page.getList()) {
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("id",t.getId()); row.put("title",t.getTitle()); row.put("price",t.getPrice());
            row.put("createTime",t.getCreateTime()); row.put("bizType",t.getBizType());
            row.put("relatedOrder",resolve(user, t.getWalletId(),t.getBizType(),t.getBizId()));
            rows.add(row);
        }
        return success(new PageResult<>(rows,page.getTotal()));
    }

    @GetMapping("/get")
    public CommonResult<Map<String,Object>> get(@RequestParam Long id) {
        Long user = getLoginUserId();
        var rows = jdbc.queryForList("SELECT t.id,t.wallet_id,t.biz_type,t.biz_id,t.no,t.title,t.price,t.balance,t.create_time FROM pay_wallet_transaction t JOIN pay_wallet w ON w.id=t.wallet_id WHERE t.id=? AND w.user_id=? AND w.user_type=1 AND w.tenant_id=? AND t.tenant_id=? AND w.deleted=0 AND t.deleted=0",id,user,getTenantId(),getTenantId());
        if (rows.isEmpty()) return CommonResult.error(404,"流水不存在或无权查看");
        var t=rows.get(0);
        Map<String,Object> result=new LinkedHashMap<>();
        for (String key:List.of("id","no","title","price","balance")) result.put(key,t.get(key));
        result.put("createTime",t.get("create_time"));
        result.put("relatedOrder",resolve(user,((Number)t.get("wallet_id")).longValue(),((Number)t.get("biz_type")).intValue(),String.valueOf(t.get("biz_id"))));
        return success(result);
    }

    Map<String,Object> resolve(Long user,Long wallet,int type,String biz) {
        if (user==null || biz==null) return null;
        if (type==10 && biz.startsWith("auction-settlement:")) {
            Long id=positiveId(biz.substring(19)); if(id==null)return null;
            return one("竞拍结算",jdbc.queryForList("SELECT a.id,a.collection_name AS name,a.current_price AS amount,a.status,a.create_time,s.gross_amount AS grossAmount,s.fee_amount AS feeAmount,s.seller_income AS netAmount FROM app_auction_settlement s JOIN app_auction a ON a.id=s.auction_id WHERE s.id=? AND s.seller_id=? AND a.seller_id=? AND s.deleted=0 AND a.deleted=0",id,user,user));
        }
        if(type==5 && (biz.startsWith("withdraw-reject:") || biz.startsWith("withdraw-transfer-fail:"))) {
            Long id=positiveId(biz.substring(biz.indexOf(':')+1)); return id==null?null:withdraw(user,id);
        }
        Long id=positiveId(biz); if(id==null)return null;
        if(type==8)return withdraw(user,id);
        if(type==1 || type==2) return one("充值订单",jdbc.queryForList("SELECT id,'钱包充值' AS name,total_price AS amount,IF(pay_status=1,'充值成功','待支付') AS status,create_time FROM pay_wallet_recharge WHERE id=? AND wallet_id=? AND tenant_id=? AND deleted=0",id,wallet,getTenantId()));
        if(type==3 || type==4) {
            Long payId=id;
            if(type==4) {
                var refunds=jdbc.queryForList("SELECT order_id FROM pay_refund WHERE id=? AND tenant_id=? AND deleted=0",id,getTenantId());
                if(refunds.size()!=1)return null;
                payId=((Number)refunds.get(0).get("order_id")).longValue();
            }
            List<Map<String,Object>> candidates=new ArrayList<>();
            var purchase=one("购买订单",jdbc.queryForList("SELECT o.id,y.name,o.price AS amount,o.status,o.create_time,o.fulfillment_type AS fulfillmentType FROM app_ykj_order o JOIN app_yikoujia y ON y.id=o.ykj_id WHERE o.pay_order_id=? AND o.user_id=? AND o.deleted=0",payId,user));
            if(purchase!=null)candidates.add(purchase);
            var batch=one("批量买单",jdbc.queryForList("SELECT b.id,c.name,b.price*b.deal_amount AS amount,b.status,b.create_time,(b.deal_amount-b.amount) AS quantity FROM app_buy_order b LEFT JOIN app_category c ON c.id=b.category_id WHERE b.pay_order_id=? AND b.user_id=?",payId,user));
            if(batch!=null)candidates.add(batch);
            return unique(candidates);
        }
        if(type==7) {
            // Legacy INCOME reused numeric IDs across business tables. Never infer from ID alone.
            List<Map<String,Object>> candidates=new ArrayList<>();
            var sale=one("挂售订单",jdbc.queryForList("SELECT id,name,price AS amount,status,create_time,sale_channel AS saleChannel FROM app_yikoujia WHERE id=? AND user_id=?",id,user));
            if(sale!=null)candidates.add(sale);
            var fast=one("快速变现订单",jdbc.queryForList("SELECT id,'快速变现' AS name,price AS amount,status,create_time FROM app_fast_trade WHERE id=? AND user_id=?",id,user));
            if(fast!=null)candidates.add(fast);
            // Batch-sale credits use the counterparty buy-order ID without a seller reference.
            if(!jdbc.queryForList("SELECT id FROM app_buy_order WHERE id=?",id).isEmpty())return null;
            return unique(candidates);
        }
        return null;
    }
    private Map<String,Object> withdraw(Long user,Long id) {
        return one("提现申请",jdbc.queryForList("SELECT id,'钱包提现' AS name,price AS amount,status,create_time,fee_price AS feeAmount FROM trade_brokerage_withdraw WHERE id=? AND user_id=? AND tenant_id=? AND deleted=0",id,user,getTenantId()));
    }
    private static Long getTenantId() { return getLoginUser()==null ? null : getLoginUser().getTenantId(); }
    static Long positiveId(String value) {
        if(value==null || !value.matches("[1-9][0-9]*"))return null;
        try{return Long.valueOf(value);}catch(NumberFormatException ignored){return null;}
    }
    static Map<String,Object> unique(List<Map<String,Object>> rows) {return rows.size()==1?rows.get(0):null;}
    private Map<String,Object> one(String kind,List<Map<String,Object>> rows) {
        if(rows.size()!=1)return null;
        Map<String,Object> order=new LinkedHashMap<>(rows.get(0));order.put("kind",kind);
        order.put("createTime",order.remove("create_time"));return order;
    }
}
