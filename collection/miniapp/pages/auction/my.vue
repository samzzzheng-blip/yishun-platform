<template>
  <s-layout title="我的竞价" navbar="normal" :dark="false">
    <scroll-view class="page" scroll-y>
      <scroll-view class="tabs" scroll-x :show-scrollbar="false">
        <view class="tabs-inner">
          <view
            v-for="item in tabs"
            :key="item.label"
            class="tab"
            :class="{ active: status === item.value }"
            @tap="changeStatus(item.value)"
            >{{ item.label }}</view
          >
        </view>
      </scroll-view>
      <view v-for="item in list" :key="item.id" class="card">
        <image v-if="coverUrl(item)" class="cover" :src="coverUrl(item)" mode="aspectFill" />
        <view v-else class="cover cover-empty">拍卖照片待补充</view>
        <view class="info">
          <view class="name">{{ item.collectionName }}</view>
          <view class="price"
            >{{ item.status === 0 ? '申请起拍价' : '当前价' }} ¥{{
              (item.currentPrice / 100).toFixed(2)
            }}</view
          >
          <view class="meta">{{ item.bidCount }} 次出价 · {{ statusText(item) }}</view>
          <view v-if="item.syncError" class="error">{{ item.syncError }}</view>
          <view v-if="item.status === 7 && item.reviewRejectReason" class="reject-reason"
            >送拍申请被驳回：{{ item.reviewRejectReason }}</view
          >
          <view v-if="item.delistStatus === 3 && item.delistRejectReason" class="reject-reason"
            >下架申请被拒绝：{{ item.delistRejectReason }}</view
          >
          <view v-if="item.status === 3 && item.settlementStatus === 1" class="settlement">
            <view>成交金额 ¥{{ (item.grossAmount / 100).toFixed(2) }}</view>
            <view>平台手续费 {{ item.feeRate }}%：¥{{ (item.feeAmount / 100).toFixed(2) }}</view>
            <view class="income">已进入钱包 ¥{{ (item.sellerIncome / 100).toFixed(2) }}</view>
            <view class="settlement-note">提现无需人工审核，系统核验金额和来源后自动发起转账，请在提现记录中查看进度并按提示确认收款。</view>
            <view class="withdraw-action" @tap="requestPayout(item)">申请打款</view>
          </view>
          <view v-if="[0, 6].includes(item.status)" class="cancel" @tap="cancel(item)"
            >取消送拍</view
          >
          <view
            v-else-if="item.status === 1 && item.delistStatus !== 1"
            class="cancel"
            @tap="requestDelist(item)"
            >申请下架</view
          >
          <view v-else-if="item.status === 1 && item.delistStatus === 1" class="pending-action"
            >下架审核中</view
          >
        </view>
      </view>
      <view v-if="!loading && !list.length" class="empty">暂无记录</view>
    </scroll-view>
  </s-layout>
</template>

<script setup>
  import { ref } from 'vue';
  import { onShow } from '@dcloudio/uni-app';
  import AuctionApi from '@/sheep/api/collection/auction';
  import sheep from '@/sheep';
  const tabs = [
    { label: '全部', value: undefined },
    { label: '待人工发布', value: 0 },
    { label: '进行中', value: 1 },
    { label: '待确认成交', value: 2 },
    { label: '已成交', value: 3 },
    { label: '流拍', value: 4 },
    { label: '已取消', value: 5 },
    { label: '异常', value: 6 },
    { label: '已驳回', value: 7 },
  ];
  const status = ref(undefined);
  const list = ref([]);
  const loading = ref(false);
  async function load() {
    loading.value = true;
    try {
      const res = await AuctionApi.getMyPage({ pageNo: 1, pageSize: 100, status: status.value });
      if (res.code === 0) list.value = res.data?.list || [];
    } finally {
      loading.value = false;
    }
  }
  function changeStatus(value) {
    status.value = value;
    load();
  }
  function coverUrl(item) {
    const urls = Array.isArray(item?.displayPicUrls) ? item.displayPicUrls : [];
    return urls.find((url) => typeof url === 'string' && url.trim()) || '';
  }
  function statusText(item) {
    if (item.status === 1 && item.delistStatus === 1) return '下架审核中';
    if (item.status === 1 && item.delistStatus === 3) return '闲鱼竞拍中（下架被拒）';
    return (
      {
        0: '等待工作人员审核并发布闲鱼',
        1: '闲鱼竞拍中',
        2: '拍卖结束，结果待工作人员确认',
        3: '已售出，实收金额已进入钱包',
        4: '流拍',
        5: '已取消',
        6: '同步异常，等待工作人员处理',
        7: '已驳回',
      }[item.status] || '处理中'
    );
  }
  function cancel(item) {
    uni.showModal({
      title: '取消送拍',
      content: '该商品尚未发布到闲鱼，确认取消送拍吗？',
      success: async ({ confirm }) => {
        if (!confirm) return;
        const res = await AuctionApi.cancel(item.id);
        if (res.code === 0) {
          uni.showToast({ title: '已取消送拍', icon: 'success' });
          load();
        }
      },
    });
  }
  function requestDelist(item) {
    uni.showModal({
      title: '申请下架竞拍',
      content: '提交后需等待后台审核；审核通过后商品将从闲鱼下架并恢复到您的藏品中。',
      confirmText: '提交申请',
      success: async ({ confirm }) => {
        if (!confirm) return;
        const res = await AuctionApi.requestDelist(item.id);
        if (res.code === 0) {
          uni.showToast({ title: '申请已提交', icon: 'success' });
          load();
        }
      },
    });
  }
  function requestPayout(item) {
    sheep.$router.go('/pages/commission/withdraw', {
      auctionId: item.id,
      amount: item.sellerIncome,
      collectionName: item.collectionName,
    });
  }
  onShow(load);
</script>

<style scoped lang="scss">
  .page {
    height: 100%;
    box-sizing: border-box;
    padding: 28rpx;
    background: var(--ys-page-bg);
  }
  .tabs {
    width: 100%;
    margin-bottom: 24rpx;
    white-space: nowrap;
  }
  .tabs-inner {
    display: inline-flex;
    gap: 12rpx;
    padding-right: 4rpx;
  }
  .tab {
    min-width: 128rpx;
    height: 70rpx;
    padding: 0 18rpx;
    border: 2rpx solid var(--ys-border);
    border-radius: 35rpx;
    text-align: center;
    line-height: 70rpx;
    background: var(--ys-surface);
  }
  .tab.active {
    border-color: #18181b;
    color: #fff;
    background: #18181b;
  }
  .card {
    display: flex;
    gap: 22rpx;
    margin-bottom: 20rpx;
    padding: 22rpx;
    border: 2rpx solid var(--ys-border);
    border-radius: var(--ys-radius);
    background: var(--ys-surface);
    box-shadow: var(--ys-shadow);
  }
  .cover {
    width: 170rpx;
    height: 170rpx;
    border-radius: 14rpx;
    background: var(--ys-surface-muted);
  }
  .cover-empty {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    color: var(--ys-text-secondary);
    font-size: 20rpx;
  }
  .info {
    flex: 1;
    min-width: 0;
  }
  .name {
    overflow: hidden;
    font-size: 27rpx;
    font-weight: 650;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .price {
    margin-top: 18rpx;
    font-size: 27rpx;
    font-weight: 650;
  }
  .meta {
    margin-top: 8rpx;
    color: var(--ys-text-secondary);
    font-size: 21rpx;
  }
  .error {
    margin-top: 8rpx;
    color: #dc2626;
    font-size: 20rpx;
    line-height: 1.4;
  }
  .reject-reason {
    margin-top: 8rpx;
    color: #b45309;
    font-size: 20rpx;
    line-height: 1.4;
  }
  .cancel {
    display: inline-block;
    margin-top: 16rpx;
    color: #dc2626;
    font-size: 22rpx;
  }
  .pending-action {
    display: inline-block;
    margin-top: 16rpx;
    color: #7c3aed;
    font-size: 22rpx;
  }
  .settlement {
    margin-top: 14rpx;
    padding: 14rpx;
    border-radius: 12rpx;
    color: #52525b;
    font-size: 20rpx;
    line-height: 1.6;
    background: #f4f4f5;
  }
  .income {
    color: #15803d;
    font-weight: 650;
  }
  .settlement-note {
    margin-top: 6rpx;
    color: #7c3aed;
  }
  .withdraw-action {
    display: inline-block;
    margin-top: 10rpx;
    color: #7c3aed;
    font-weight: 650;
  }
  .empty {
    padding: 160rpx 0;
    color: var(--ys-text-secondary);
    text-align: center;
  }
</style>
