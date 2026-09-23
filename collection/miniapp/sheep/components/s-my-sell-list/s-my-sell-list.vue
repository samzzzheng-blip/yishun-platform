<template>
  <view class="other-list">
    <view class="loading-state" v-if="loading">正在加载在售藏品…</view>
    <view class="other" :class="{ 'auction-card': item.saleType === 'auction' }" v-for="item in filteredDataList" :key="`${item.saleType}-${item.id}`" @tap="openItem(item)">
      <view class="sale-badge" :class="{ auction: item.saleType === 'auction' }">{{ item.saleType === 'auction' ? '闲鱼竞拍' : '直购' }}</view>
      <su-image
        :width="250"
        :height="260"
        :radius="14"
        mode="aspectFill"
        :src="item.cover"
      ></su-image>
      <view class="name">{{ item.name }}</view>
      <view class="amount">{{ item.saleType === 'auction' ? '当前价' : '直购价' }} ￥{{ fen2yuan(item.price) }}</view>
      <view v-if="item.saleType === 'auction'" class="auction-state">{{ auctionStatus(item) }}</view>
      <view v-else class="direct-state">{{ directSaleStatus(item) }}</view>
      <view v-if="item.saleType === 'direct' && Number(item.status) === 0" class="pending-hint">等待工作人员上架</view>
      <button
        v-if="canDelist(item)"
        class="delist-button"
        :disabled="delistingId === item.id"
        @tap.stop="confirmDelist(item)"
      >{{ delistingId === item.id ? '处理中…' : Number(item.status) === 0 ? '取消上架' : '下架' }}</button>
    </view>
    <s-empty
      v-if="!loading && filteredDataList.length === 0"
      icon="/static/demo/empty-collection.jpg"
      :text="loadError ? '在售藏品加载失败' : '这里还没有在售藏品'"
      :description="loadError ? '请检查网络后重新加载' : '提交挂售后，待上架和在售藏品会显示在这里'"
      :showAction="loadError"
      actionText="重新加载"
      paddingTop="54"
      @clickAction="loadList"
    />
  </view>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import SellApi from '@/sheep/api/collection/sell';
  import AuctionApi from '@/sheep/api/collection/auction';
  import sheep from '@/sheep';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import { canDelist, directSaleStatus } from './direct-sale-state.mjs';

  const dataList = ref([]);
  const loading = ref(false);
  const loadError = ref(false);
  const delistingId = ref(null);

  function firstImage(item) {
    const picUrl = Array.isArray(item?.picUrl) ? item.picUrl : [item?.picUrl];
    const urls = [...picUrl, ...(Array.isArray(item?.picUrls) ? item.picUrls : [])];
    return urls.find((url) => typeof url === 'string' && url.trim()) || '';
  }

  const props = defineProps({
    categoryName: {
      type: String,
      default: '全部',
    },
  });

  const filteredDataList = computed(() => {
    if (props.categoryName === '全部') return dataList.value;
    return dataList.value.filter((item) => item.categoryName === props.categoryName);
  });

  async function loadList() {
    loading.value = true;
    loadError.value = false;
    const [sellResult, auctionResult] = await Promise.all([
      SellApi.getMySellList().catch(() => null),
      AuctionApi.getMyPage({ pageNo: 1, pageSize: 100 }).catch(() => null),
    ]);
    const directItems = Array.isArray(sellResult?.data)
      ? sellResult.data.map((item) => ({ ...item, saleType: 'direct', cover: firstImage(item) }))
      : [];
    const auctionItems = Array.isArray(auctionResult?.data?.list)
      ? auctionResult.data.list.filter((item) => [0, 1, 6].includes(item.status)).map((item) => ({
        ...item,
        saleType: 'auction',
        name: item.collectionName,
        price: item.currentPrice,
        cover: firstImage(item),
      }))
      : [];
    dataList.value = [...auctionItems, ...directItems];
    loadError.value = !sellResult && !auctionResult;
    loading.value = false;
  }

  function auctionStatus(item) {
    if (item.delistStatus === 1) return '下架审核中';
    if (item.status === 0) return '等待发布到闲鱼';
    if (item.status === 6) return '发布失败';
    return '竞拍进行中';
  }

  function openItem(item) {
    if (item.saleType === 'auction') sheep.$router.go('/pages/auction/my');
  }

  function confirmDelist(item) {
    if (!canDelist(item) || delistingId.value !== null) return;
    uni.showModal({
      title: Number(item.status) === 0 ? '取消上架' : '确认下架',
      content: '确认后藏品仍保留在仓库，可重新挂售、修改类型或申请取回。',
      confirmText: '确认',
      confirmColor: '#7c3aed',
      success: async ({ confirm }) => {
        if (!confirm) return;
        delistingId.value = item.id;
        try {
          const response = await SellApi.delistYikoujia(item.id);
          if (response?.code !== 0) return;
          dataList.value = dataList.value.filter(
            (current) => !(current.saleType === 'direct' && current.id === item.id),
          );
          uni.showToast({ title: Number(item.status) === 0 ? '已取消上架' : '已下架', icon: 'success' });
        } finally {
          delistingId.value = null;
        }
      },
    });
  }

  loadList();
</script>

<style lang="scss" scoped>
  .other-list {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    box-sizing: border-box;
    padding: 40rpx;
    .loading-state {
      width: 100%;
      padding: 80rpx 0;
      color: var(--ys-text-secondary);
      font-size: 24rpx;
      text-align: center;
    }
    .other {
      width: 305.5rpx;
      min-height: 458rpx;
      background: var(--ys-surface);
      border: 1rpx solid var(--ys-border);
      border-radius: 18rpx;
      box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
      margin-bottom: 30rpx;
      box-sizing: border-box;
      padding: 24rpx 26rpx;
      position: relative;
      overflow: hidden;
      &.auction-card { height: 438rpx; }
      .sale-badge {
        position: absolute;
        z-index: 1;
        top: 34rpx;
        left: 36rpx;
        padding: 6rpx 12rpx;
        border-radius: 999rpx;
        color: #fff;
        font-size: 18rpx;
        line-height: 1.2;
        background: #18181b;
        &.auction { background: #7c3aed; }
      }
      .name {
        font-size: 20rpx;
        font-weight: bold;
        color: var(--ys-text);
        width: 250rpx;
        margin: 18rpx 0 4rpx;
        height: 30rpx;
        line-height: 30rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .amount {
        font-size: 20rpx;
        color: var(--ys-text-secondary);
        width: 250rpx;
      }
      .auction-state {
        margin-top: 4rpx;
        color: #7c3aed;
        font-size: 18rpx;
      }
      .direct-state {
        margin-top: 12rpx;
        color: var(--ys-text);
        font-size: 26rpx;
        font-weight: 600;
      }
      .pending-hint {
        margin-top: 6rpx;
        color: var(--ys-text-secondary);
        font-size: 24rpx;
        line-height: 1.5;
      }
      .delist-button {
        width: 100%;
        height: 64rpx;
        margin: 12rpx 0 0;
        padding: 0;
        border: 1rpx solid #7c3aed;
        border-radius: 999rpx;
        background: #f5f0ff;
        color: #6d28d9;
        font-size: 22rpx;
        font-weight: 600;
        line-height: 62rpx;
        &::after { border: 0; }
        &[disabled] {
          opacity: 0.55;
          color: #6d28d9;
          background: #f5f0ff;
        }
      }
    }
  }
</style>
