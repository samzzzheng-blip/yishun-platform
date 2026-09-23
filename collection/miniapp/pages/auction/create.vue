<template>
  <s-layout title="上架竞价" navbar="normal" :dark="false">
    <view class="create-page">
      <view class="title">设置竞价规则</view>
      <view class="desc"
        >提交后由工作人员审核，并在闲鱼人工发布竞拍。后台录入对应链接后，商品才会展示在竞价市场。</view
      >
      <view class="form-card">
        <view class="field"
          ><text>起拍价（元）</text><text class="fixed-price">1.00（固定）</text></view>
        <view class="field"
          ><text>最低加价（元）</text><input v-model="increment" type="digit" placeholder="0.00"
        /></view>
        <view class="field field-column">
          <text>竞价时长</text>
          <view class="duration-list">
            <view
              v-for="day in [1, 3, 7]"
              :key="day"
              class="duration"
              :class="{ active: days === day }"
              @tap="days = day"
              >{{ day }}天</view
            >
          </view>
        </view>
      </view>
      <view class="notice"
        >工作人员会根据本页信息创建闲鱼竞拍。出价、付款、成交与售后均在闲鱼完成，小程序只同步展示图片和竞拍数据。</view
      >
      <view class="submit" :class="{ disabled: submitting }" @tap="submit">{{
        submitting ? '正在提交…' : '提交送拍申请'
      }}</view>
    </view>
  </s-layout>
</template>

<script setup>
  import { ref } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import AuctionApi from '@/sheep/api/collection/auction';
  const collectionId = ref();
  const increment = ref('');
  const days = ref(3);
  const submitting = ref(false);
  onLoad((options) => {
    collectionId.value = Number(options.collectionId);
  });
  async function submit() {
    if (submitting.value) return;
    const step = Number(increment.value);
    if (!collectionId.value || !Number.isFinite(step) || Math.round(step * 100) < 1)
      return sheep.$helper.toast('请完整填写竞价信息');
    if (step > 1000000) return sheep.$helper.toast('竞价金额不能超过100万元');
    // 后端的 LocalDateTime 全局按毫秒时间戳反序列化，不能发送日期字符串。
    const endTime = Date.now() + days.value * 86400000;
    submitting.value = true;
    try {
      const res = await AuctionApi.create({
        collectionId: collectionId.value,
        startPrice: 100,
        minIncrement: Math.round(step * 100),
        endTime,
      });
      if (res.code === 0) setTimeout(() => sheep.$router.go('/pages/auction/my'), 500);
    } finally {
      submitting.value = false;
    }
  }
</script>

<style scoped lang="scss">
  .create-page {
    min-height: 100%;
    box-sizing: border-box;
    padding: 44rpx 28rpx;
    color: var(--ys-text);
    background: var(--ys-page-bg);
  }
  .title {
    font-size: 40rpx;
    font-weight: 700;
  }
  .desc {
    margin-top: 14rpx;
    color: var(--ys-text-secondary);
    font-size: 24rpx;
    line-height: 1.6;
  }
  .form-card {
    margin-top: 34rpx;
    padding: 8rpx 28rpx;
    border: 2rpx solid var(--ys-border);
    border-radius: var(--ys-radius);
    background: var(--ys-surface);
    box-shadow: var(--ys-shadow);
  }
  .field {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 112rpx;
    border-bottom: 2rpx solid var(--ys-border);
    font-size: 26rpx;
  }
  .field:last-child {
    border-bottom: 0;
  }
  .field input {
    width: 260rpx;
    text-align: right;
    font-size: 28rpx;
  }
  .field-column {
    display: block;
    padding: 28rpx 0;
  }
  .duration-list {
    display: flex;
    gap: 14rpx;
    margin-top: 22rpx;
  }
  .duration {
    flex: 1;
    height: 72rpx;
    border: 2rpx solid var(--ys-border);
    border-radius: 14rpx;
    text-align: center;
    line-height: 72rpx;
  }
  .duration.active {
    border-color: #18181b;
    color: #fff;
    background: #18181b;
  }
  .notice {
    margin-top: 24rpx;
    padding: 22rpx;
    border-radius: 16rpx;
    color: #5b5568;
    font-size: 22rpx;
    line-height: 1.6;
    background: #faf8ff;
  }
  .submit {
    margin-top: 36rpx;
    height: 92rpx;
    border-radius: 18rpx;
    color: #fff;
    text-align: center;
    font-size: 28rpx;
    font-weight: 650;
    line-height: 92rpx;
    background: #18181b;
  }
  .submit.disabled {
    opacity: 0.55;
  }
</style>
