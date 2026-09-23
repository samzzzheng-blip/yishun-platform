<template>
  <s-layout title="竞价详情" navbar="normal" :dark="false" :bgStyle="{ color: 'var(--ys-page-bg)' }">
    <view class="detail-page">
      <view v-if="lot" class="detail-card">
        <swiper v-if="images.length" class="gallery" :indicator-dots="images.length > 1" indicator-active-color="#7c3aed">
          <swiper-item v-for="(image, index) in images" :key="image">
            <image class="photo" :src="image" mode="aspectFit" :aria-label="'查看第' + (index + 1) + '张大图'" @tap="preview(index)" />
          </swiper-item>
        </swiper>
        <view v-else class="gallery empty-photo">拍卖照片待补充</view>
        <view v-if="images.length" class="hint">共 {{ images.length }} 张图片 · 点击查看大图</view>
        <view class="name">{{ lot.collectionName }}</view>
        <view class="category">{{ lot.categoryName || '藏品' }} · 编号 {{ lot.id }}</view>
        <view class="price">{{ lot.bidCount ? '当前价' : '起拍价' }} ￥{{ money(lot.currentPrice) }}</view>
        <view class="facts">
          <view>{{ lot.bidCount || 0 }} 次出价</view>
          <view>最低加价 ￥{{ money(lot.minIncrement) }}</view>
          <view>截止时间 {{ deadline }}</view>
          <view>{{ statusText }}</view>
        </view>
        <view class="link-section">
          <view class="section-title">前往闲鱼查看</view>
          <button v-if="lot.goofishUrl || appShare" class="auction-link" hover-class="pressed" @tap="openGoofish">{{ appShare ? '复制闲鱼口令' : '复制商品网址' }}</button>
          <view v-else class="hint">闲鱼商品链接待补充</view>
          <button class="auction-link" hover-class="pressed" @tap="copyShop">复制闲鱼用户名</button>
          <view class="hint">在闲鱼搜索用户“一瞬收藏仓”，进入商铺查看商品。点击上方按钮可复制用户名。</view>
          <view class="notice">实际出价、付款及成交结果以闲鱼页面为准。</view>
        </view>
      </view>
      <view v-else-if="loading" class="state">加载中...</view>
      <view v-else class="state">
        <view>商品暂时无法显示</view>
        <button class="retry" @tap="loadDetail">重新加载</button>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
import { computed, ref, onUnmounted } from 'vue';
import { onLoad, onShow, onHide, onPullDownRefresh } from '@dcloudio/uni-app';
import AuctionApi from '@/sheep/api/collection/auction';
import { auctionAppShare, copyAuctionShare, copyAuctionShop } from '@/sheep/api/collection/auction-share';

const id = ref('');
const lot = ref(null);
const loading = ref(false);
const appShare = computed(() => auctionAppShare(lot.value));
let syncClock;
const images = computed(() => Array.isArray(lot.value?.displayPicUrls) ? lot.value.displayPicUrls.filter(Boolean) : []);
const money = (value) => ((Number(value) || 0) / 100).toFixed(2);
const deadline = computed(() => {
  if (!lot.value?.endTime) return '待同步';
  const date = new Date(lot.value.endTime);
  if (Number.isNaN(date.getTime())) return '待同步';
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
});
const statusText = computed(() => lot.value?.status === 1 ? '竞拍中，价格每 10 秒更新' : '竞拍状态已更新，请查看闲鱼详情');
function preview(index) { uni.previewImage({ urls: images.value, current: images.value[index] }); }
function copyShop() { copyAuctionShop(uni); }
function openGoofish() {
  if (!lot.value) return;
  // #ifdef MP-WEIXIN
  copyAuctionShare(lot.value, uni);
  return;
  // #endif
  // #ifdef APP-PLUS
  plus.runtime.openURL(lot.value.goofishUrl);
  // #endif
  // #ifdef H5
  window.location.href = lot.value.goofishUrl;
  // #endif
}
async function loadDetail() {
  if (!id.value || loading.value) return;
  loading.value = true;
  try {
    const res = await AuctionApi.getDetail({ id: id.value });
    if (res.code !== 0 || !res.data) throw new Error('商品暂时无法显示');
    lot.value = res.data;
  } catch {
    if (lot.value) uni.showToast({ title: '价格刷新失败，请稍后重试', icon: 'none' });
  } finally { loading.value = false; }
}
onLoad(options => { id.value = options.id || ''; });
onShow(() => { loadDetail(); clearInterval(syncClock); syncClock = setInterval(loadDetail, 10000); });
onHide(() => clearInterval(syncClock));
onUnmounted(() => clearInterval(syncClock));
onPullDownRefresh(async () => { try { await loadDetail(); } finally { uni.stopPullDownRefresh(); } });
</script>

<style lang="scss" scoped>
.detail-page { padding: 28rpx 28rpx calc(48rpx + env(safe-area-inset-bottom)); color: var(--ys-text); }
.detail-card { padding: 24rpx; border: 1rpx solid var(--ys-border); border-radius: 18rpx; background: var(--ys-surface); box-shadow: 0 8rpx 24rpx rgba(24,24,27,.06); }
.gallery { height: 640rpx; width: 100%; border-radius: 14rpx; overflow: hidden; background: var(--ys-surface-muted); }
.photo { width: 100%; height: 100%; }
.empty-photo { display: flex; align-items: center; justify-content: center; color: var(--ys-text-secondary); }
.name { margin-top: 24rpx; font-size: 32rpx; font-weight: 600; line-height: 1.5; word-break: break-all; }
.category, .hint, .notice { margin-top: 14rpx; font-size: 24rpx; line-height: 1.6; color: var(--ys-text-secondary); }
.price { margin-top: 20rpx; font-size: 36rpx; font-weight: 600; }
.facts { margin-top: 20rpx; font-size: 26rpx; line-height: 1.9; }
.link-section { margin-top: 28rpx; padding-top: 24rpx; border-top: 1rpx solid var(--ys-border); }
.section-title { font-size: 28rpx; font-weight: 600; }
.auction-link { margin: 12rpx 0 0; padding: 20rpx 0; min-height: 96rpx; font-size: 26rpx; line-height: 1.6; text-align: left; text-decoration: underline; word-break: break-all; color: var(--ys-text); background: transparent; }
.auction-link::after { border: 0; }
.pressed { opacity: .7; }
.state { padding: 80rpx 0; text-align: center; font-size: 28rpx; }
.retry { margin-top: 24rpx; font-size: 28rpx; }
@media (min-width: 768px) { .detail-page { max-width: 720px; margin: 0 auto; } }
</style>
