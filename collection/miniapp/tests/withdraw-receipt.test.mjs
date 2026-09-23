import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import assert from 'node:assert/strict';
const source = readFileSync(new URL('../pages/commission/components/withdraw-receipt.vue', import.meta.url), 'utf8')
  .split('<script setup>')[1].split('</script>')[0].replace(/^import .*;$/gm, '');
function setup(status = 10) {
  let callbacks;
  let mounted, unmount, calls = 0;
  const response = { status, type: 5, payTransferId: 137, transferChannelMchId: 'merchant', transferChannelPackageInfo: 'package' };
  const make = new Function('ref','computed','onMounted','onBeforeUnmount','defineProps','sheep','BrokerageApi','PayTransferApi','wx', source + '\nreturn {refresh,refreshAndConfirm,confirmReceipt,canConfirm,busy,error,detail};');
  const state = make(v=>({value:v}), f=>({get value(){return f();}}), fn=>{mounted=fn;}, fn=>{unmount=fn;}, ()=>({id:98,amount:100}),
    {$platform:{useProvider:()=>({requestMerchantTransfer:(m,p,ok,fail)=>{calls++;callbacks={ok,fail};}})}},
    {getBrokerageWithdraw:async()=>({code:0,data:{...response}})}, {syncTransfer:async()=>({code:0})}, {canIUse:()=>true});
  return {state,response,mount:()=>mounted(),unmount:()=>unmount(),get calls(){return calls;},get callbacks(){return callbacks;}};
}
test('pending transfer exposes confirmation immediately after lookup',async()=>{
  const {state}=setup(); await state.refresh(); assert.equal(state.canConfirm.value,true);
});
test('cancel retains confirmation and unlocks retry',async()=>{
  const ctx=setup(); await ctx.state.refresh(); ctx.state.confirmReceipt(); ctx.callbacks.fail();
  assert.equal(ctx.state.busy.value,false); assert.equal(ctx.state.canConfirm.value,true);
});
test('native success callback alone never means funds arrived',async()=>{
  const ctx=setup(); await ctx.state.refresh(); ctx.state.confirmReceipt(); await ctx.callbacks.ok();
  assert.equal(ctx.state.detail.value.status,10); assert.match(ctx.state.error.value,/尚未确认/);
});
test('only backend success removes confirmation',async()=>{
  const ctx=setup(); await ctx.state.refresh(); ctx.state.confirmReceipt(); ctx.response.status=11; await ctx.callbacks.ok();
  assert.equal(ctx.state.canConfirm.value,false); assert.equal(ctx.state.busy.value,false);
});
test('failed transfer does not offer confirmation',async()=>{
  const {state}=setup(21); await state.refresh(); assert.equal(state.canConfirm.value,false);
});
test('double tap does not invoke the receipt request twice',async()=>{
  const ctx=setup(); await ctx.state.refresh(); ctx.state.confirmReceipt(); const first=ctx.callbacks;
  ctx.state.confirmReceipt(); assert.equal(ctx.callbacks,first);
});
test('new withdrawal opens WeChat receipt without a second tap',async()=>{
  const ctx=setup(); await ctx.mount(); assert.equal(ctx.calls,1); assert.equal(ctx.state.busy.value,true);
});
test('cancel never auto reopens and manual continue remains available',async()=>{
  const ctx=setup(); await ctx.mount(); ctx.callbacks.fail(); await ctx.mount();
  assert.equal(ctx.calls,1); ctx.state.confirmReceipt(); assert.equal(ctx.calls,2);
});
test('completed or failed withdrawal never auto opens receipt',async()=>{
  for (const status of [11,20,21]) { const ctx=setup(status); await ctx.mount(); assert.equal(ctx.calls,0); }
});
test('missing package does not open and explicit refresh continues once ready',async()=>{
  const ctx=setup(); ctx.response.transferChannelPackageInfo=''; await ctx.mount(); assert.equal(ctx.calls,0);
  ctx.response.transferChannelPackageInfo='package'; await ctx.state.refreshAndConfirm(); assert.equal(ctx.calls,1);
});
test('leaving page during initial lookup prevents delayed popup',async()=>{
  const ctx=setup(); const pending=ctx.mount(); ctx.unmount(); await pending; assert.equal(ctx.calls,0);
});
