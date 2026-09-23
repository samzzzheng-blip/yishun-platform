import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { orderStatus } from '../pages/user/wallet/transaction-status.mjs';
test('各业务独立解释状态，未知状态不显示成功',()=>{
 assert.equal(orderStatus({kind:'购买订单',status:1}),'已支付');
 assert.equal(orderStatus({kind:'提现申请',status:21}),'提现失败');
 assert.equal(orderStatus({kind:'挂售订单',status:3}),'在售中');
 assert.equal(orderStatus({kind:'购买订单',status:99}),'状态待确认');
});
test('使用流水编号打开授权详情，不把业务编号当订单编号跳转',()=>{
 const s=readFileSync(new URL('../pages/user/wallet/money.vue',import.meta.url),'utf8');
 assert.match(s,/v-if="item.relatedOrder"/);
 assert.match(s,/\/pages\/user\/wallet\/transaction\?id=' \+ item.id/);
 assert.match(s,/WalletRecordApi.page/);
});
test('日期方括号和空格编码，全部筛选不发送空数字参数',async()=>{
 const source=readFileSync(new URL('../sheep/api/pay/wallet-record.js',import.meta.url),'utf8').replace("import request from '@/sheep/request';",'const request = value => value;');
 const {walletRecordQuery,default:api}=await import('data:text/javascript;base64,'+Buffer.from(source).toString('base64'));
 const params={pageNo:1,type:'','createTime[0]':'2026-09-18 00:00:00'};
 const query=walletRecordQuery(params);
 assert.equal(query,'pageNo=1&createTime%5B0%5D=2026-09-18%2000%3A00%3A00');
 assert.equal(api.page(params).url,'/app/wallet-record/page?'+query);
 assert.equal(api.page(params).params,undefined);
});
