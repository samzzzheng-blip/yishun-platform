<template>
  <s-layout title="能量石明细" :dark="false">
    <view class="stone-page">
      <view class="summary">
        <view>当前能量石</view>
        <view class="balance">{{ balance === null ? '—' : balance }}</view>
        <view class="muted">兑换、消费及调整记录</view>
      </view>
      <view class="tabs">
        <button v-for="tab in tabs" :key="tab.value" class="tab" :class="{ active: direction === tab.value }"
          :aria-label="tab.label" :disabled="loading" hover-class="pressed" @tap="changeTab(tab.value)">{{ tab.label }}</button>
      </view>
      <view class="records">
        <view v-for="item in records" :key="item.id" class="record">
          <view class="row"><text>{{ typeName(item.type) }}</text><text class="amount">{{ item.amount > 0 ? '+' : '' }}{{ item.amount }}</text></view>
          <view v-if="item.exchangeName" class="product-name">{{ item.exchangeName }}<text v-if="item.exchangeQuantity"> ×{{ item.exchangeQuantity }}</text></view>
          <view v-else-if="item.type === 2" class="muted detail-note">历史记录未关联具体商品</view>
          <view class="row muted"><text>{{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM:ss') }}</text><text>结余 {{ item.balance }}</text></view>
          <button v-if="item.exchangeLogId" class="detail-link" hover-class="pressed" @tap="showExchange(item)">查看兑换记录</button>
        </view>
        <view v-if="loading" class="state">正在加载明细…</view>
        <view v-else-if="failed" class="state">明细加载失败，请重试<button class="retry" @tap="load()">重新加载</button></view>
        <view v-else-if="!records.length" class="state">{{ direction === 'expense' ? '暂无支出记录' : direction === 'income' ? '暂无收入记录' : '暂无能量石记录' }}</view>
        <button v-else-if="records.length < total" class="retry" hover-class="pressed" @tap="load()">加载更多</button>
        <view v-else class="state">已显示全部记录</view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
import sheep from '@/sheep';
import request from '@/sheep/request';
import sellApi from '@/sheep/api/collection/sell';

const tabs = [{ label: '全部', value: 'all' }, { label: '收入', value: 'income' }, { label: '支出', value: 'expense' }];
const direction = ref('all');
const records = ref([]);
const total = ref(0);
const pageNo = ref(1);
const loading = ref(false);
const failed = ref(false);
const balance = ref(null);
const typeName = (type) => ({ 1: '兑换能量石', 2: '兑换藏品', 3: '管理员调整', 4: '寄存容量消费' }[type] || '能量石变动');
function showExchange(item) {
  uni.showModal({ title: '兑换记录',
    content: `订单编号：${item.exchangeLogId}\n商品：${item.exchangeName || '兑换藏品'}\n数量：${item.exchangeQuantity ?? '—'}\n能量石变动：${item.amount}\n结余：${item.balance}`,
    confirmText: '我的订单', cancelText: '关闭',
    success: (res) => { if (res.confirm) sheep.$router.go('/pages/order/list', { tab: 'exchange' }); }
  });
}

async function load(reset = false) {
  if (loading.value) return;
  loading.value = true;
  failed.value = false;
  if (reset) { records.value = []; total.value = 0; pageNo.value = 1; }
  try {
    const res = await request({ url: '/app/stone-record/page', method: 'GET',
      params: { pageNo: pageNo.value, pageSize: 20, direction: direction.value },
      custom: { showLoading: false, showError: false } });
    if (!res || res.code !== 0 || !Array.isArray(res.data?.list)) throw new Error('明细获取失败');
    records.value.push(...res.data.list);
    total.value = res.data.total;
    pageNo.value += 1;
  } catch { failed.value = true; }
  finally { loading.value = false; }
}
async function refreshBalance() {
  try {
    const res = await sellApi.getUserTradeInfo();
    if (res?.code === 0) balance.value = res.data.stoneAmount ?? 0;
  } catch { /* 不把获取失败显示为零余额 */ }
}
function changeTab(value) {
  if (loading.value || direction.value === value) return;
  direction.value = value;
  load(true);
}
onLoad(() => { load(true); refreshBalance(); });
onReachBottom(() => { if (!failed.value && records.value.length < total.value) load(); });
onPullDownRefresh(async () => { try { await Promise.all([load(true), refreshBalance()]); } finally { uni.stopPullDownRefresh(); } });
</script>

<style lang="scss" scoped>
.stone-page { padding: 32rpx 24rpx; padding-bottom: calc(32rpx + env(safe-area-inset-bottom)); color: var(--ys-text); }
.summary, .records { background: var(--ys-surface, #fff); border-radius: 20rpx; padding: 32rpx; }
.summary { text-align: center; font-size: 28rpx; }
.balance { font-size: 56rpx; font-weight: 700; margin: 16rpx 0; overflow-wrap: anywhere; }
.muted { color: var(--ys-text-secondary); font-size: 24rpx; }
.tabs { display: flex; gap: 16rpx; margin: 24rpx 0; }
.tab, .retry { min-height: 48px; font-size: 28rpx; line-height: 48px; color: var(--ys-text); background: var(--ys-surface, #fff); }
.tab { flex: 1; margin: 0; padding: 0 8rpx; }
.active { color: var(--ys-brand-color); font-weight: 700; border-bottom: 4rpx solid var(--ys-brand-color); }
.record { padding: 24rpx 0; border-bottom: 1rpx solid var(--ys-border-strong); }
.row { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 12rpx; font-size: 28rpx; }
.row.muted { font-size: 24rpx; margin-top: 16rpx; }
.amount { font-weight: 700; }
.product-name { margin-top: 12rpx; font-size: 28rpx; overflow-wrap: anywhere; line-height: 1.5; }
.detail-note { margin-top: 12rpx; }
.detail-link { margin: 12rpx 0 0; min-height: 48px; line-height: 48px; font-size: 26rpx; background: var(--ys-surface, #fff); color: var(--ys-text); }
.state { text-align: center; padding: 32rpx 0; font-size: 28rpx; color: var(--ys-text-secondary); }
.retry { margin-top: 24rpx; }
.pressed { opacity: .65; }
</style>
