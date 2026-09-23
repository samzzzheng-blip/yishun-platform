<template>
  <s-layout
    title="寄存规则"
    navbar="normal"
    :dark="false"
    :bgStyle="{ color: 'var(--ys-page-bg)' }"
  >
    <view class="wrap">
      <view class="title">寄存规则</view>
      
      <view class="table-wrap">
        <view class="table-header">
          <view class="th th-level">寄存档位</view>
          <view class="th th-storage">存储空间</view>
          <view class="th th-month">所需能量石/月</view>
          <view class="th th-year">所需能量石/年</view>
        </view>
        <view class="table-body">
          <view class="tr" v-for="(item, index) in storageList" :key="item.id">
            <view class="td td-level">{{ index }}</view>
            <view class="td td-storage">{{ formatStorage(item) }}</view>
            <view class="td td-month">{{ item.monthlyPrice }}</view>
            <view class="td td-year">{{ item.yearlyPrice }}</view>
          </view>
        </view>
      </view>

      <view class="notice-text" v-html="noticeContent"></view>
    </view>
  </s-layout>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import StoragePlanApi from '@/sheep/api/collection/storage-plan'
import sheep from '@/sheep'
import ArticleApi from '@/sheep/api/promotion/article';

const noticeContent = ref('')

const storageList = ref([])

function formatStorage(item) {
  if (item.minCount === 0 && item.maxCount === 0) {
    return '0'
  }
  return item.minCount + '-' + item.maxCount
}

async function getStoragePlanList() {
  const { code, data } = await StoragePlanApi.getStoragePlanList()
  if (code === 0) {
    storageList.value = data
  }
}

onLoad(async () => {
  getStoragePlanList()
  const { data } = await ArticleApi.getArticle(null, '购买须知')
  noticeContent.value = data ? data.content : ''
})
</script>

<style lang="scss" scoped>
.wrap {
  background: var(--ys-page-bg);
  min-height: 100vh;
  box-sizing: border-box;
  padding: 40rpx 30rpx 60rpx;
  color: var(--ys-text);
}

.title {
  text-align: center;
  color: var(--ys-text);
  font-size: 40rpx;
  font-weight: bold;
  margin-bottom: 50rpx;
}

.table-wrap {
  border: 1rpx solid var(--ys-border);
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 50rpx;
  background: var(--ys-surface);
  box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
}

.table-header {
  display: flex;
  background: #f3f4f6;
  border-bottom: 1rpx solid var(--ys-border);
}

.th {
  flex: 1;
  text-align: center;
  color: var(--ys-text);
  font-size: 24rpx;
  font-weight: bold;
  padding: 20rpx 10rpx;
  border-right: 1rpx solid var(--ys-border);
  
  &:last-child {
    border-right: none;
  }
}

.table-body { background: var(--ys-surface); }

.tr {
  display: flex;
  border-bottom: 1rpx solid var(--ys-border);
  
  &:last-child {
    border-bottom: none;
  }
}

.td {
  flex: 1;
  text-align: center;
  color: var(--ys-text-secondary);
  font-size: 24rpx;
  padding: 18rpx 10rpx;
  border-right: 1rpx solid var(--ys-border);
  
  &:last-child {
    border-right: none;
  }
}

.td-level { color: var(--ys-text); font-weight: 700; }

.notice-title {
  color: var(--ys-text);
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 30rpx;
  padding-left: 20rpx;
  border-left: 4rpx solid var(--ys-brand-color);
}

.notice-section {
  margin-bottom: 30rpx;
}

.notice-subtitle {
  color: var(--ys-text);
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 15rpx;
}

.notice-text {
  color: var(--ys-text-secondary);
  font-size: 26rpx;
  line-height: 1.8;
  margin-bottom: 10rpx;
}
</style>
