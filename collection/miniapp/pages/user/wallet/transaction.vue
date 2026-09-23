<template>
  <s-layout title="关联订单" navbar="normal" :dark="false">
    <view class="page">
      <view v-if="record">
        <view class="card" v-if="record.relatedOrder">
          <view class="heading">{{ record.relatedOrder.kind }}</view>
          <view class="name">{{ record.relatedOrder.name }}</view>
          <view class="row"><text>订单编号</text><text>{{ record.relatedOrder.id }}</text></view>
          <view class="row"><text>订单金额</text><text>￥{{ fen2yuan(record.relatedOrder.amount || 0) }}</text></view>
          <view class="row"><text>订单状态</text><text>{{ orderStatus(record.relatedOrder) }}</text></view>
          <view class="row"><text>创建时间</text><text>{{ formatTime(record.relatedOrder.createTime) }}</text></view>
          <view v-if="record.relatedOrder.feeAmount != null" class="row"><text>手续费</text><text>￥{{ fen2yuan(record.relatedOrder.feeAmount) }}</text></view>
          <view v-if="record.relatedOrder.netAmount != null" class="row"><text>结算入账</text><text>￥{{ fen2yuan(record.relatedOrder.netAmount) }}</text></view>
          <view v-if="record.relatedOrder.fulfillmentType" class="row"><text>收货方式</text><text>{{ record.relatedOrder.fulfillmentType === 'SHIP' ? '直接寄出' : '入库到仓库' }}</text></view>
        </view>
        <view v-else class="card">暂无可确认的关联订单，历史流水可能缺少关联信息。</view>
        <view class="card">
          <view class="heading">本次资金变动</view>
          <view class="row"><text>{{ record.title }}</text><text>{{ record.price > 0 ? '+' : '' }}￥{{ fen2yuan(record.price) }}</text></view>
          <view class="row"><text>流水号</text><text class="number">{{ record.no }}</text></view>
          <view class="row"><text>发生时间</text><text>{{ formatTime(record.createTime) }}</text></view>
          <view class="row"><text>变动后余额</text><text>￥{{ fen2yuan(record.balance) }}</text></view>
        </view>
      </view>
      <view v-else class="state"><text>{{ loading ? '加载中…' : '记录不存在或暂时无法加载' }}</text><button v-if="!loading" @tap="load">重新加载</button></view>
    </view>
  </s-layout>
</template>
<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import WalletRecordApi from '@/sheep/api/pay/wallet-record';
import { fen2yuan } from '@/sheep/hooks/useGoods';
import sheep from '@/sheep';
import { orderStatus } from './transaction-status.mjs';
const id=ref('');const record=ref(null);const loading=ref(false);
const formatTime=value => value ? sheep.$helper.timeFormat(value,'yyyy-mm-dd hh:MM:ss') : '—';
async function load() {
  if(!id.value || loading.value)return;
  loading.value=true;
  try {const res=await WalletRecordApi.get(id.value);record.value=res.code===0?res.data:null;}
  catch {record.value=null;}
  finally {loading.value=false;}
}
onLoad(options=>{id.value=options.id || '';load();});
</script>
<style lang="scss" scoped>
.page {padding: 28rpx 28rpx calc(48rpx + env(safe-area-inset-bottom)); color: var(--ys-text);}
.card {padding: 28rpx; margin-bottom: 24rpx; background: var(--ys-surface); border: 1rpx solid var(--ys-border); border-radius: 18rpx; font-size: 26rpx; line-height: 1.6;}
.heading {font-size: 30rpx; font-weight: 600; margin-bottom: 20rpx;}
.name {font-size: 28rpx; margin-bottom: 20rpx; word-break: break-all;}
.row {display: flex; justify-content: space-between; gap: 24rpx; padding: 12rpx 0; font-size: 26rpx;}
.row text:first-child {flex-shrink: 0; color: var(--ys-text-secondary);}
.row text:last-child {text-align: right; overflow-wrap: anywhere; min-width: 0;}
.state {text-align: center; padding: 80rpx 0; font-size: 28rpx;}
.state button {margin-top: 24rpx;}
</style>
