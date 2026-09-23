<template>
  <s-layout title="我的订单" :navbar="orderNavbar">
    <view class="order-page">
      <GetbackEntry />
      <view class="order-tabs">
        <view
          v-for="(tab, index) in tabs"
          :key="tab.name"
          class="order-tab"
          :class="{ 'order-tab--active': currentTab === index }"
          hover-class="order-tab--pressed"
          :hover-stay-time="70"
          @tap="changeTab(index)"
        >
          <text>{{ tab.name }}</text>
          <view class="order-tab-line" v-if="currentTab === index"></view>
        </view>
      </view>

      <view class="order-content">
        <view class="order-loading" v-if="loading && orders.length === 0">
          <view class="loading-ring"></view>
          <text>正在加载订单</text>
        </view>

        <view class="order-empty" v-else-if="failed && orders.length === 0">
          <view class="order-empty-title">订单加载失败</view>
          <button @tap="reload">重新加载</button>
        </view>
        <view class="order-empty" v-else-if="!loading && orders.length === 0">
          <image class="order-empty-image" src="/static/order-empty.png" mode="aspectFit"></image>
          <view class="order-empty-title">暂无订单</view>
          <view class="order-empty-desc">{{ currentTab === 3 ? '使用能量石兑换商品后，记录会显示在这里' : '直购订单和已成交拍卖会显示在这里' }}</view>
        </view>

        <view v-else class="order-list">
          <view class="order-card" v-for="order in orders" :key="`${order.isAuction ? 'auction' : order.isExchange ? 'exchange' : 'purchase'}-${order.id}`">
            <view class="order-card-header">
              <text class="order-number">{{ order.isAuction ? '拍卖成交' : '订单号' }} {{ order.id }}</text>
              <text class="order-status" :class="statusClass(order.isAuction ? 1 : order.status)">{{ order.isAuction ? '已成交' : order.isExchange ? exchangeStatus(order.status) : statusText(order.status) }}</text>
            </view>
            <view class="order-product">
              <image v-if="!order.isExchange" class="order-cover" :src="coverOf(order)" mode="aspectFit"></image>
              <view class="order-product-copy">
                <view class="order-name">{{ order.name || `一口价商品 ${order.ykjId}` }}</view>
                <view class="order-time">{{ formatTime(order.createTime) }}</view>
                <view class="order-price" v-if="order.isExchange">能量石兑换 · 数量 {{ order.amount ?? 1 }}</view>
                <view class="order-price" v-else>{{ order.isAuction ? '成交金额 ' : '' }}¥{{ fen2yuan(order.price || 0) }}</view>
                <view class="order-time" v-if="order.isAuction && Number(order.settlementStatus) === 1">手续费 ¥{{ fen2yuan(order.feeAmount || 0) }} · 已到账 ¥{{ fen2yuan(order.sellerIncome || 0) }}</view>
                <view class="order-time" v-if="order.isExchange && order.deliverCode">快递单号：{{ order.deliverCode }}</view>
              </view>
            </view>
            <view class="order-card-footer" v-if="order.isAuction">
              <view class="pay-button" role="button" @tap="sheep.$router.go('/pages/auction/detail', { id: order.id })">查看拍卖详情</view>
            </view>
            <view class="order-card-footer" v-else-if="order.isExchange">
              <view class="pay-button" role="button" hover-class="pay-button--pressed" @tap="sheep.$router.go('/pages/user/wallet/stone')">查看能量石明细</view>
            </view>
            <view class="order-card-footer" v-else-if="Number(order.status) === 0">
              <view
                class="pay-button"
                hover-class="pay-button--pressed"
                :hover-stay-time="70"
                @tap="continuePay(order)"
              >继续付款</view>
            </view>
          </view>

          <view class="load-state" v-if="loading">正在加载更多…</view>
          <button v-else-if="failed" @tap="loadOrders()">加载失败，点击重试</button>
          <view class="load-state" v-else-if="finished">没有更多订单了</view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { ref } from 'vue';
  import GetbackEntry from '@/sheep/components/s-getback-entry/s-getback-entry.vue';
  import { onLoad, onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import OrderApi from '@/sheep/api/collection/order';
  import AuctionApi from '@/sheep/api/collection/auction';
  import ExchangeApi from '@/sheep/api/collection/exchange';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import { createMergedOrders } from './merged-orders.mjs';
  const mergedOrders = createMergedOrders(
    params => OrderApi.getPage(params),
    params => ExchangeApi.getExchangeLog(params),
    10,
    params => AuctionApi.getMyPage({ ...params, status: 3 }),
  );
  const completedOrders = createMergedOrders(
    params => OrderApi.getPage({ ...params, status: 1 }),
    async () => ({ code: 0, data: { list: [], total: 0 } }),
    10,
    params => AuctionApi.getMyPage({ ...params, status: 3 }),
  );

  // 微信使用系统导航，避免自定义标题栏随页面滚动或渲染异常消失。
  let orderNavbar = 'normal';
  // #ifdef MP-WEIXIN
  orderNavbar = 'none';
  // #endif

  const tabs = [
    { name: '全部', status: undefined },
    { name: '待付款', status: 0 },
    { name: '已完成', status: 1 },
    { name: '能量石兑换', exchange: true },
  ];

  const currentTab = ref(0);
  const orders = ref([]);
  const pageNo = ref(1);
  const loading = ref(false);
  const finished = ref(false);
  const initialized = ref(false);
  const failed = ref(false);
  const exchangeStatus = (status) => ({ 0: '待发货', 1: '已发货', 2: '已收货' }[status] || '处理中');

  function changeTab(index) {
    if (loading.value || currentTab.value === index) return;
    currentTab.value = index;
    reload();
  }

  async function loadOrders(reset = false) {
    if (loading.value || (!reset && finished.value)) return;
    if (reset) {
      orders.value = [];
      pageNo.value = 1;
      finished.value = false;
      mergedOrders.reset();
      completedOrders.reset();
    }
    loading.value = true;
    failed.value = false;
    try {
      if (currentTab.value === 0 || currentTab.value === 2) {
        const page = await (currentTab.value === 2 ? completedOrders : mergedOrders).next();
        orders.value = orders.value.concat(page.list);
        finished.value = page.finished;
        return;
      }
      const params = {
        pageNo: pageNo.value,
        pageSize: 10,
      };
      const status = tabs[currentTab.value].status;
      if (status !== undefined) params.status = status;
      const isExchange = !!tabs[currentTab.value].exchange;
      const response = isExchange ? await ExchangeApi.getExchangeLog(params) : await OrderApi.getPage(params);
      if (response.code !== 0) {
        failed.value = true;
        if (reset) orders.value = [];
        sheep.$helper.toast(response.msg || '订单加载失败');
        return;
      }
      const list = Array.isArray(response.data?.list) ? response.data.list.map(item => isExchange
        ? { ...item, isExchange: true, name: item.exchangeName || '兑换商品' } : item) : [];
      orders.value = reset ? list : orders.value.concat(list);
      const total = Number(response.data?.total || 0);
      finished.value = orders.value.length >= total || list.length < params.pageSize;
      if (!finished.value) pageNo.value += 1;
    } catch (error) {
      failed.value = true;
      if (reset) orders.value = [];
      sheep.$helper.toast('订单加载失败，请稍后重试');
    } finally {
      loading.value = false;
      initialized.value = true;
      uni.stopPullDownRefresh();
    }
  }

  function reload() {
    return loadOrders(true);
  }

  function coverOf(order) {
    return Array.isArray(order.picUrl) && order.picUrl.length > 0
      ? order.picUrl[0]
      : '/static/order-empty.png';
  }

  function statusText(status) {
    return Number(status) === 1 ? '已完成' : '待付款';
  }

  function statusClass(status) {
    return Number(status) === 1 ? 'order-status--done' : 'order-status--pending';
  }

  function formatTime(value) {
    return value ? sheep.$helper.timeFormat(value, 'yyyy-mm-dd hh:MM') : '';
  }

  function continuePay(order) {
    if (!order.payOrderId) {
      sheep.$helper.toast('支付单尚未生成，请稍后重试');
      return;
    }
    sheep.$router.go('/pages/pay/index', { id: order.payOrderId });
  }

  onLoad((options) => {
    if (options?.tab === 'exchange') currentTab.value = tabs.findIndex((tab) => tab.exchange);
    reload();
  });
  onShow(() => {
    if (initialized.value) reload();
  });
  onReachBottom(() => loadOrders());
  onPullDownRefresh(() => reload());
</script>

<style lang="scss" scoped>
  .order-page {
    min-height: 100%;
    background: var(--ys-page-bg);
  }

  .order-tabs {
    min-height: 48px;
    padding: 0 16rpx;
    display: flex;
    align-items: stretch;
    background: var(--ys-surface);
    border-bottom: 1rpx solid var(--ys-border);
  }

  .order-tab {
    flex: 1;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--ys-text-secondary);
    font-size: 27rpx;
    transition: color 120ms ease-out, opacity 120ms ease-out;
  }

  .order-tab--active {
    color: var(--ys-text);
    font-weight: 700;
  }

  .order-tab--pressed {
    opacity: 0.62;
  }

  .order-tab-line {
    width: 42rpx;
    height: 6rpx;
    position: absolute;
    left: 50%;
    bottom: 8rpx;
    background: var(--ys-text);
    border-radius: 3rpx;
    transform: translateX(-50%);
  }

  .order-content {
    padding: 24rpx;
  }

  .order-loading,
  .order-empty {
    min-height: 720rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--ys-text-secondary);
  }

  .loading-ring {
    width: 40rpx;
    height: 40rpx;
    margin-bottom: 18rpx;
    border: 4rpx solid #e6e1f4;
    border-top-color: var(--ys-brand-color);
    border-radius: 50%;
    animation: order-loading 0.8s linear infinite;
  }

  @keyframes order-loading {
    to { transform: rotate(360deg); }
  }

  .order-empty-image {
    width: 220rpx;
    height: 220rpx;
    opacity: 0.72;
  }

  .order-empty-title {
    margin-top: 24rpx;
    color: var(--ys-text);
    font-size: 30rpx;
    font-weight: 700;
  }

  .order-empty-desc {
    margin-top: 10rpx;
    font-size: 23rpx;
  }

  .order-list {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
  }

  .order-card {
    padding: 24rpx;
    background: var(--ys-surface);
    border: 1rpx solid var(--ys-border);
    border-radius: 20rpx;
  }

  .order-card-header {
    padding-bottom: 18rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1rpx solid var(--ys-border);
  }

  .order-number {
    color: var(--ys-text-secondary);
    font-size: 22rpx;
  }

  .order-status {
    font-size: 24rpx;
    font-weight: 600;
  }

  .order-status--pending {
    color: #a66b00;
  }

  .order-status--done {
    color: #27865b;
  }

  .order-product {
    padding-top: 22rpx;
    display: flex;
  }

  .order-cover {
    width: 168rpx;
    height: 168rpx;
    flex: 0 0 auto;
    background: var(--ys-surface-muted);
    border-radius: 14rpx;
  }

  .order-product-copy {
    min-width: 0;
    margin-left: 22rpx;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .order-name {
    color: var(--ys-text);
    font-size: 28rpx;
    font-weight: 650;
    line-height: 1.45;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .order-time {
    margin-top: 12rpx;
    color: var(--ys-text-secondary);
    font-size: 22rpx;
  }

  .order-price {
    margin-top: auto;
    color: var(--ys-text);
    font-size: 31rpx;
    font-weight: 700;
  }

  .order-card-footer {
    margin-top: 22rpx;
    padding-top: 20rpx;
    display: flex;
    justify-content: flex-end;
    border-top: 1rpx solid var(--ys-border);
  }

  .pay-button {
    min-width: 168rpx;
    height: 64rpx;
    padding: 0 28rpx;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #ffffff;
    font-size: 25rpx;
    font-weight: 650;
    background: var(--ys-text);
    border-radius: 32rpx;
    transition: transform 100ms ease-out, opacity 100ms ease-out;
  }

  .pay-button--pressed {
    opacity: 0.78;
    transform: scale(0.97);
  }

  .load-state {
    padding: 18rpx 0 8rpx;
    color: var(--ys-text-secondary);
    font-size: 22rpx;
    text-align: center;
  }
</style>
