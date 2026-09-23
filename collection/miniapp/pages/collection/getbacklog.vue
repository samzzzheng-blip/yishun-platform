<template>
  <s-layout :title="recordId ? '取回详情' : '取回记录'" navbar="normal" :dark="false">
    <view class="page">
      <view v-if="loading" class="card">正在加载取回信息…</view>
      <view v-else-if="error" class="card">{{ error }}<button @tap="load">重新加载</button></view>
      <template v-else-if="recordId && selected">
        <view class="card">
          <view class="heading">{{ statusText(selected.status) }}</view>
          <view>取回编号：{{ selected.id }}</view>
          <view>申请时间：{{ time(selected.createTime) }}</view>
        </view>
        <view class="card">
          <view class="heading">收货信息</view>
          <view>{{ selected.receiverName || '未提供收件人' }}　{{ selected.receiverMobile || '' }}</view>
          <view>{{ [selected.receiverAreaName, selected.receiverDetailAddress].filter(Boolean).join(' ') || '暂无地址信息' }}</view>
        </view>
        <view class="card">
          <view class="heading">物流信息</view>
          <template v-if="selected.deliverCode">
            <view>快递单号：{{ selected.deliverCode }}</view>
            <button @tap="copyCode">复制快递单号</button>
            <view v-if="logisticsLoading" class="muted">正在查询物流…</view>
            <template v-else-if="logistics?.availability === 'OK'">
              <view class="heading">{{ logistics.status }}</view>
              <view class="muted">查询时间：{{ logistics.queriedAt }}（北京时间）</view>
              <view class="track-node" v-for="(node, index) in logistics.nodes" :key="index">
                <view class="muted">{{ node.time }}</view>
                <view>{{ node.description }}</view>
              </view>
            </template>
            <view v-else class="muted">{{ logistics?.message || '暂未获取物流信息，请点击刷新。' }}</view>
            <button :disabled="logisticsLoading" @tap="loadLogistics">刷新物流</button>
            <view class="muted">物流以快递公司更新为准，查询结果缓存30分钟。</view>
          </template>
          <view v-else>{{ Number(selected.status) === 0 ? '等待公司发货，发货后将在这里显示快递单号。' : '暂无快递单号，请联系客服核实。' }}</view>
        </view>
        <view class="card">
          <view class="heading">全部取回商品</view>
          <view v-if="!selected.items?.length" class="muted">暂无商品明细，请联系客服核实。</view>
          <view class="product" v-for="item in selected.items || []" :key="item.id">
            <view class="product-name">{{ item.name || '藏品信息暂缺' }}</view>
            <view class="muted">藏品编号：{{ item.collectionId ?? '—' }} · 数量：{{ item.amount ?? '—' }}</view>
            <view class="photos">
              <button v-for="(url, index) in photos(item)" :key="url" class="photo-button" :aria-label="'查看商品第' + (index + 1) + '张照片'" @tap="preview(item, url)">
                <image :src="url" mode="aspectFit" />
              </button>
              <text v-if="!photos(item).length" class="muted">暂无商品图片</text>
            </view>
          </view>
        </view>
        <button v-if="Number(selected.status) === 1" class="primary" :disabled="saving" :loading="saving" @tap="confirmReceived">确认收货</button>
        <button @tap="sheep.$router.go('/pages/collection/getbacklog')">查看全部取回记录</button>
      </template>
      <view v-else-if="recordId" class="card">未找到该取回记录，可能已取消或不属于当前账号。</view>
      <template v-else>
        <view v-if="!records.length" class="card">暂无取回记录。申请取回或购买后选择直接寄出，记录会显示在这里。</view>
        <view class="card" v-for="record in records" :key="record.id">
          <view class="heading">取回 #{{ record.id }} · {{ statusText(record.status) }}</view>
          <view class="muted">{{ time(record.createTime) }}</view>
          <view v-for="item in record.items || []" :key="item.id" class="summary">{{ item.name || '藏品信息暂缺' }} × {{ item.amount ?? '—' }}</view>
          <view v-if="record.deliverCode">快递单号：{{ record.deliverCode }}</view>
          <button @tap="sheep.$router.go('/pages/collection/getbacklog', { id: record.id })">查看商品与物流详情</button>
        </view>
      </template>
    </view>
  </s-layout>
</template>
<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import sheep from '@/sheep';
import getbackApi from '@/sheep/api/collection/getback';
import { getbackPhotos as photos, getbackStatus as statusText, findGetback } from './getback-view.mjs';
const recordId = ref('');
const records = ref([]);
const loading = ref(false);
const error = ref('');
const saving = ref(false);
const logistics = ref(null);
const logisticsLoading = ref(false);
let logisticsRequestId = 0;
const selected = computed(() => findGetback(records.value, recordId.value));
const time = value => value ? sheep.$helper.timeFormat(value, 'yyyy-mm-dd hh:MM:ss') : '—';
onLoad(options => { recordId.value = String(options?.id || ''); });
onShow(load);
async function load() {
  if (loading.value) return;
  loading.value = true; error.value = '';
  ++logisticsRequestId; logisticsLoading.value = false; logistics.value = null;
  try {
    // 后端按登录用户过滤，详情仅从本人记录查找，不使用管理员接口或传入的地址。
    const res = await getbackApi.getGetbackList({});
    if (res?.code !== 0 || !Array.isArray(res.data)) throw new Error();
    records.value = res.data;
    if (recordId.value && selected.value?.deliverCode) loadLogistics();
  } catch { records.value = []; error.value = '取回信息加载失败，请重试。'; }
  finally { loading.value = false; }
}
async function loadLogistics() {
  if (logisticsLoading.value || !selected.value?.deliverCode) return;
  const requestId = ++logisticsRequestId;
  logisticsLoading.value = true;
  try {
    const res = await getbackApi.getLogistics(selected.value.id);
    if (requestId !== logisticsRequestId) return;
    if (res?.code !== 0 || !res.data) throw new Error();
    logistics.value = res.data;
  } catch {
    if (requestId === logisticsRequestId) logistics.value = { message: '物流查询暂不可用，请稍后重试，或复制单号到快递官方渠道查询。' };
  } finally {
    if (requestId === logisticsRequestId) logisticsLoading.value = false;
  }
}
function copyCode() {
  if (selected.value?.deliverCode) uni.setClipboardData({ data: String(selected.value.deliverCode) });
}
function preview(item, current) { uni.previewImage({ current, urls: photos(item) }); }
function confirmReceived() {
  if (saving.value || Number(selected.value?.status) !== 1) return;
  saving.value = true;
  const id = selected.value.id;
  uni.showModal({
    title: '确认已收到商品？', content: '请核对包裹内全部商品后再确认。',
    success: async result => {
      if (!result.confirm) { saving.value = false; return; }
      try {
        const res = await getbackApi.confirmDeliver({ id, status: 2 });
        if (res?.code !== 0) throw new Error();
        await load();
      } catch { sheep.$helper.toast('确认未完成，请刷新状态后重试'); }
      finally { saving.value = false; }
    },
    fail: () => { saving.value = false; },
  });
}
</script>
<style scoped>
.track-node { border-left: 4rpx solid var(--ys-brand-color, #7954d8); padding: 0 0 28rpx 24rpx; margin-left: 8rpx; }
.page { padding: 24rpx 24rpx calc(40rpx + env(safe-area-inset-bottom)); color: var(--ys-text, #18181b); font-size: 28rpx; line-height: 1.7; overflow-wrap: anywhere; }
.card { padding: 28rpx; margin-bottom: 24rpx; border-radius: 20rpx; background: var(--ys-surface, #fff); }
.heading { font-weight: 700; font-size: 32rpx; margin-bottom: 16rpx; }
.muted { color: var(--ys-text-secondary, #62626c); font-size: 26rpx; }
.product { padding: 24rpx 0; border-top: 1px solid var(--ys-border, #dedee6); }
.product-name { font-size: 30rpx; font-weight: 600; }
.summary { margin-top: 16rpx; }
button { min-height: 48px; margin-top: 20rpx; padding: 20rpx; font-size: 28rpx; line-height: 1.5; color: var(--ys-text, #18181b); background: var(--ys-surface, #fff); border: 1px solid var(--ys-border, #dedee6); border-radius: 12rpx; }
.primary { color: var(--ys-surface, #fff); background: var(--ys-text, #18181b); }
.photos { display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 16rpx; }
.photo-button { padding: 0; margin: 0; width: 144rpx; height: 144rpx; overflow: hidden; }
.photo-button image { width: 100%; height: 100%; }
</style>
