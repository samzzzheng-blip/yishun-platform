<template>
  <s-layout
    title="竞价市场"
    navbar="normal"
    tabbar="/pages/index/trade"
    :dark="false"
  >
    <scroll-view class="auction-page" scroll-y>
      <view class="market-toolbar">
        <view class="back-home" hover-class="toolbar-button--pressed" @tap="goHome">
          <text class="back-arrow">←</text>
          <text>返回首页</text>
        </view>
        <view class="my-auctions" hover-class="toolbar-button--pressed" @tap="goMyAuctions">
          我的竞价
        </view>
      </view>

      <view class="safety-tip">
        <view class="tip-icon">i</view>
        <text>竞拍和付款均在闲鱼完成，本页面只展示闲鱼同步的图片与当前价格</text>
      </view>

      <scroll-view class="filter-scroll" scroll-x :show-scrollbar="false">
        <view class="filter-list">
          <view
            v-for="item in filters"
            :key="item.id"
            class="filter-item"
            :class="{ active: activeFilter === item.id }"
            hover-class="filter-item--pressed"
            :hover-stay-time="70"
            @tap="activeFilter = item.id"
            >{{ item.label }}</view
          >
        </view>
      </scroll-view>

      <view class="section-head">
        <view>
          <view class="section-title">{{ activeFilterLabel }}</view>
          <view class="section-desc">截止时间以服务器时间为准</view>
        </view>
        <view class="rules-link" hover-class="text-pressed" @tap="showRules">竞价规则</view>
      </view>

      <view class="auction-list">
        <view v-for="lot in visibleLots" :key="lot.id" class="auction-card"
          hover-class="card-pressed" role="button" :aria-label="'查看' + lot.name" @tap="goDetail(lot)">
          <view class="cover-wrap">
            <image v-if="lot.images.length" class="cover" :src="lot.images[0]" mode="aspectFit" lazy-load />
            <view v-else class="cover cover-empty">照片待补充</view>
          </view>
          <view class="lot-title">{{ lot.name }}</view>
          <view class="price">{{ lot.bidCount ? '当前价' : '起拍价' }} ￥{{ lot.currentPrice.toLocaleString() }}</view>
          <view class="card-meta">{{ lot.bidCount || 0 }} 次出价 · {{ lot.statusText }}</view>
          <view class="deadline">剩余 {{ formatCountdown(lot.endAt) }}</view>
        </view>
      </view>
      <view v-if="loading && !lots.length" class="page-state">加载中...</view>
      <view v-else-if="loadError" class="page-state" @tap="loadAuctions">加载失败，点击重试</view>
      <view v-else-if="lots.length && !visibleLots.length" class="page-state">当前筛选下暂无竞价商品</view>

      <view v-if="!loading && !loadError && !lots.length" class="preview-note">
        <view class="preview-title">暂无竞价商品</view>
        <view class="preview-desc">已审核的藏品上架后会显示在这里。</view>
      </view>
    </scroll-view>

  </s-layout>
</template>

<script setup>
  import { computed, onMounted, onUnmounted, ref } from 'vue';
  import { onHide, onPullDownRefresh, onShow } from '@dcloudio/uni-app';
  import AuctionApi from '@/sheep/api/collection/auction';


  const filters = [
    { id: 'all', label: '全部' },
    { id: 'ending', label: '即将结束' },
    { id: 'popular', label: '热门竞价' },
    { id: 'new', label: '新上架' },
  ];

  const activeFilter = ref('all');
  const now = ref(Date.now());
  const loading = ref(false);
  const loadError = ref(false);
  let clock;
  let syncClock;

  const lots = ref([]);

  const activeFilterLabel = computed(
    () => filters.find((item) => item.id === activeFilter.value)?.label || '全部',
  );
  const visibleLots = computed(() =>
    activeFilter.value === 'all'
      ? lots.value
      : lots.value.filter((item) => item.uiStatus === activeFilter.value),
  );

  function formatCountdown(endAt) {
    const left = Math.max(0, endAt - now.value);
    const h = Math.floor(left / 3600000);
    const m = Math.floor((left % 3600000) / 60000);
    const s = Math.floor((left % 60000) / 1000);
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(
      2,
      '0',
    )}`;
  }

  function goDetail(lot) {
    uni.navigateTo({ url: `/pages/auction/detail?id=${encodeURIComponent(lot.id)}` });
  }

  function showRules() {
    uni.showModal({
      title: '竞价规则',
      content: '商品由一瞬收藏账号发布到闲鱼竞拍。本页面价格来自闲鱼同步，实际出价、付款、成交与售后均以闲鱼页面为准。',
      showCancel: false,
    });
  }

  function goMyAuctions() {
    uni.navigateTo({ url: '/pages/auction/my' });
  }

  function goHome() {
    uni.switchTab({ url: '/pages/index/index' });
  }

  onMounted(() => {
    clock = setInterval(() => {
      now.value = Date.now();
    }, 1000);
  });
  onShow(() => {
    loadAuctions();
    clearInterval(syncClock);
    syncClock = setInterval(loadAuctions, 10000);
  });
  onHide(() => clearInterval(syncClock));
  onPullDownRefresh(async () => { await loadAuctions(); uni.stopPullDownRefresh(); });
  onUnmounted(() => {
    clearInterval(clock);
    clearInterval(syncClock);
  });

  async function loadAuctions() {
    if (loading.value) return;
    loading.value = true;
    loadError.value = false;
    try {
      const res = await AuctionApi.getPage({ pageNo: 1, pageSize: 100, status: 1 });
      if (res.code !== 0) throw new Error('加载失败');
      lots.value = (res.data?.list || []).map((item) => {
        const endAt = new Date(item.endTime).getTime();
        const left = endAt - Date.now();
        const uiStatus = left < 3600000 ? 'ending' : item.bidCount >= 10 ? 'popular' : 'new';
        return {
          ...item,
          name: item.collectionName,
          category: item.categoryName || '藏品',
          code: `YS-${String(item.id).padStart(4, '0')}`,
          cover: item.displayPicUrls?.[0],
          images: Array.isArray(item.displayPicUrls) ? item.displayPicUrls.filter(Boolean) : [],
          currentPrice: item.currentPrice / 100,
          increment: item.minIncrement / 100,
          endAt,
          uiStatus,
          statusText: uiStatus === 'ending' ? '即将结束' : uiStatus === 'popular' ? '热门' : '新上架',
        };
      });
    } catch { loadError.value = true; } finally { loading.value = false; }
  }
</script>

<style lang="scss" scoped>
  .auction-page {
    height: 100%;
    box-sizing: border-box;
    padding: 24rpx 28rpx calc(64rpx + env(safe-area-inset-bottom));
    color: var(--ys-text);
    background: var(--ys-page-bg);
  }
  .market-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;
  }
  .back-home,
  .my-auctions {
    display: flex;
    align-items: center;
    min-height: 64rpx;
    box-sizing: border-box;
    padding: 0 24rpx;
    border: 2rpx solid var(--ys-border);
    border-radius: 32rpx;
    color: var(--ys-text);
    font-size: 24rpx;
    font-weight: 600;
    background: var(--ys-surface);
  }
  .back-arrow {
    margin-right: 10rpx;
    font-size: 32rpx;
    line-height: 1;
  }
  .my-auctions {
    color: #6d28d9;
    border-color: #ddd6fe;
    background: #f5f3ff;
  }
  .toolbar-button--pressed {
    opacity: 0.68;
  }
  .safety-tip {
    display: flex;
    align-items: center;
    gap: 14rpx;
    margin: 22rpx 0;
    padding: 20rpx;
    border: 2rpx solid #ede9fe;
    border-radius: 16rpx;
    color: #5b5568;
    font-size: 22rpx;
    line-height: 1.45;
    background: #faf8ff;
  }
  .tip-icon {
    display: flex;
    flex: 0 0 34rpx;
    align-items: center;
    justify-content: center;
    width: 34rpx;
    height: 34rpx;
    border: 2rpx solid #8b5cf6;
    border-radius: 50%;
    color: #7c3aed;
    font-size: 20rpx;
    font-weight: 700;
  }
  .filter-scroll {
    width: 100%;
    white-space: nowrap;
  }
  .filter-list {
    display: inline-flex;
    gap: 12rpx;
    padding-bottom: 22rpx;
  }
  .filter-item {
    min-height: 68rpx;
    padding: 0 26rpx;
    border: 2rpx solid var(--ys-border);
    border-radius: 34rpx;
    color: var(--ys-text-secondary);
    font-size: 23rpx;
    line-height: 66rpx;
    background: var(--ys-surface);
  }
  .filter-item.active {
    border-color: #18181b;
    color: #fff;
    background: #18181b;
  }
  .filter-item--pressed,
  .text-pressed {
    opacity: 0.68;
  }
  .section-head {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    margin: 6rpx 2rpx 22rpx;
  }
  .section-title {
    font-size: 32rpx;
    font-weight: 700;
  }
  .section-desc {
    margin-top: 6rpx;
    color: #8a8f99;
    font-size: 20rpx;
  }
  .rules-link {
    min-height: 64rpx;
    padding: 0 8rpx;
    color: #6d28d9;
    font-size: 22rpx;
    line-height: 64rpx;
  }
  .auction-list { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 24rpx; }
  .auction-card {
    min-width: 0; padding: 24rpx; border: 1rpx solid var(--ys-border);
    border-radius: 18rpx; background: var(--ys-surface);
    box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
  }
  .card-pressed { opacity: .72; }
  .cover-wrap { height: 260rpx; overflow: hidden; border-radius: 14rpx; background: var(--ys-surface-muted); }
  .cover { width: 100%; height: 100%; }
  .cover-empty { display: flex; align-items: center; justify-content: center; color: var(--ys-text-secondary); font-size: 24rpx; }
  .lot-title { margin: 18rpx 0 8rpx; font-size: 26rpx; font-weight: 600; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
  .price { font-size: 24rpx; line-height: 1.5; color: var(--ys-text); word-break: break-all; }
  .card-meta, .deadline { margin-top: 8rpx; font-size: 22rpx; line-height: 1.5; color: var(--ys-text-secondary); }
  .deadline { font-variant-numeric: tabular-nums; }
  .page-state { padding: 40rpx 0; text-align: center; color: var(--ys-text-secondary); font-size: 26rpx; }
  .preview-note {
    margin-top: 24rpx;
    padding: 22rpx;
    border: 2rpx dashed var(--ys-border-strong);
    border-radius: 16rpx;
    background: rgba(255, 255, 255, 0.55);
  }
  .preview-title {
    font-size: 22rpx;
    font-weight: 600;
  }
  .preview-desc {
    margin-top: 7rpx;
    color: var(--ys-text-secondary);
    font-size: 20rpx;
    line-height: 1.55;
  }
  @media (min-width: 768px) {
    .auction-page {
      padding-right: calc((100% - 720px) / 2);
      padding-left: calc((100% - 720px) / 2);
    }
  }
</style>
