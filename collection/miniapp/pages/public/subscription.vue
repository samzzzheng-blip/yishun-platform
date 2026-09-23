<template>
  <s-layout title="订阅信息管理" :bgStyle="{ color: '#f6f4ff' }" navbar="normal">
    <view class="subscription-page">
      <view class="hero-card">
        <view class="hero-eyebrow">服务通知</view>
        <view class="hero-title">重要进度，不错过</view>
        <view class="hero-desc"
          >重要通知用于保障交易与资产安全；建议通知可按模块关闭。这里只管理服务通知，不发送营销内容。</view
        >
        <view class="legend">
          <view class="legend-item"><text class="dot required-dot"></text>重要通知</view>
          <view class="legend-item"><text class="dot recommended-dot"></text>建议通知</view>
        </view>
      </view>

      <view v-if="loading" class="state-card">正在读取微信订阅模板…</view>
      <view v-else-if="loadFailed" class="state-card error-state">
        <view>暂时无法读取订阅配置</view>
        <button class="retry-btn" @tap="loadTemplates">重新加载</button>
      </view>

      <view v-for="module in modules" :key="module.key" class="module-card">
        <view class="module-header">
          <view class="module-icon">{{ module.icon }}</view>
          <view class="module-copy">
            <view class="module-title">{{ module.title }}</view>
            <view class="module-desc">{{ module.description }}</view>
          </view>
          <view class="template-count" :class="{ ready: moduleTemplates[module.key]?.length }">
            {{
              moduleTemplates[module.key]?.length
                ? `${moduleTemplates[module.key].length}项可授权`
                : '待配置'
            }}
          </view>
        </view>

        <view class="notice-group">
          <view class="group-heading">
            <view><text class="dot required-dot"></text>重要通知</view>
            <text class="locked-text">始终保留</text>
          </view>
          <view v-for="item in module.required" :key="item" class="notice-row">
            <text class="check-mark">✓</text><text>{{ item }}</text>
          </view>
        </view>

        <view class="notice-group recommended-group">
          <view class="group-heading">
            <view><text class="dot recommended-dot"></text>建议通知</view>
            <switch
              color="#7c3aed"
              :checked="preferences[module.key]"
              @change="onPreferenceChange(module.key, $event)"
            />
          </view>
          <view v-for="item in module.recommended" :key="item" class="notice-row muted-row">
            <text class="small-mark">•</text><text>{{ item }}</text>
          </view>
        </view>

        <button
          class="module-action"
          :class="{ disabled: !moduleTemplates[module.key]?.length }"
          @tap="requestModule(module)"
        >
          {{ moduleTemplates[module.key]?.length ? '开启本模块微信通知' : '微信后台暂未配置模板' }}
        </button>
      </view>

      <view class="footnote">
        微信订阅授权由微信统一管理。即使重要通知已保留，若在微信授权框选择“拒绝”，小程序也无法发送对应微信提醒，但站内业务状态仍会正常更新。
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import sheep from '@/sheep';
  import {
    getSubscriptionPreferences,
    matchTemplatesToModules,
    saveSubscriptionPreference,
    subscriptionModules,
  } from '@/sheep/helper/subscription';

  const modules = subscriptionModules;
  const preferences = reactive(getSubscriptionPreferences());
  const moduleTemplates = reactive({});
  const loading = ref(true);
  const loadFailed = ref(false);

  async function loadTemplates() {
    loading.value = true;
    loadFailed.value = false;
    try {
      // #ifdef MP-WEIXIN
      const provider = sheep.$platform.useProvider('wechat');
      const templates = await provider.getSubscribeTemplate();
      Object.assign(moduleTemplates, matchTemplatesToModules(templates));
      // #endif
      // #ifndef MP-WEIXIN
      Object.assign(moduleTemplates, matchTemplatesToModules([]));
      // #endif
    } catch (error) {
      console.log('[subscription] load templates failed:', error);
      loadFailed.value = true;
    } finally {
      loading.value = false;
    }
  }

  function onPreferenceChange(moduleKey, event) {
    const enabled = Boolean(event.detail.value);
    preferences[moduleKey] = enabled;
    saveSubscriptionPreference(moduleKey, enabled);
  }

  async function requestModule(module) {
    const templates = moduleTemplates[module.key] || [];
    if (templates.length === 0) {
      uni.showModal({
        title: '订阅模板待配置',
        content:
          '该模块尚未在微信公众平台配置订阅消息模板。配置完成后，本页会自动读取，无需重新发布页面。',
        showCancel: false,
      });
      return;
    }

    // #ifndef MP-WEIXIN
    sheep.$helper.toast('请在微信小程序中管理订阅');
    return;
    // #endif

    try {
      const result = await sheep.$platform
        .useProvider('wechat')
        .requestSubscribeTemplateIds(templates.map((item) => item.id));
      const accepted = templates.filter((item) => result[item.id] === 'accept').length;
      const rejected = templates.filter((item) => result[item.id] === 'reject').length;
      if (accepted > 0) {
        sheep.$helper.toast(`已开启 ${accepted} 项通知`);
      } else if (rejected > 0) {
        uni.showModal({
          title: '暂未开启',
          content:
            '你已拒绝本次授权，可稍后再次点击开启；如已选择“总是拒绝”，请前往小程序设置修改。',
          showCancel: false,
        });
      }
    } catch (error) {
      sheep.$helper.toast(error?.errMsg?.includes('cancel') ? '已取消授权' : '订阅授权未完成');
    }
  }

  onMounted(loadTemplates);
</script>

<style lang="scss" scoped>
  .subscription-page {
    padding: 28rpx 24rpx 56rpx;
    color: #17131f;
  }
  .hero-card,
  .module-card,
  .state-card {
    background: #fff;
    border: 1rpx solid #e9e3f5;
    border-radius: 24rpx;
    box-shadow: 0 12rpx 32rpx rgba(76, 29, 149, 0.06);
  }
  .hero-card {
    padding: 34rpx 30rpx;
    background: linear-gradient(135deg, #fff 0%, #f4efff 100%);
  }
  .hero-eyebrow {
    color: #7c3aed;
    font-size: 24rpx;
    font-weight: 700;
    letter-spacing: 4rpx;
  }
  .hero-title {
    margin-top: 10rpx;
    font-size: 42rpx;
    line-height: 1.25;
    font-weight: 800;
  }
  .hero-desc {
    margin-top: 18rpx;
    color: #625b6f;
    font-size: 27rpx;
    line-height: 1.65;
  }
  .legend {
    display: flex;
    gap: 28rpx;
    margin-top: 24rpx;
    color: #514a5d;
    font-size: 25rpx;
  }
  .legend-item,
  .group-heading > view {
    display: flex;
    align-items: center;
  }
  .dot {
    width: 14rpx;
    height: 14rpx;
    margin-right: 12rpx;
    border-radius: 50%;
    display: inline-block;
  }
  .required-dot {
    background: #ef4444;
  }
  .recommended-dot {
    background: #7c3aed;
  }
  .state-card {
    margin-top: 22rpx;
    padding: 34rpx;
    text-align: center;
    color: #6b6475;
  }
  .error-state {
    color: #b42318;
  }
  .retry-btn {
    margin-top: 18rpx;
    width: 220rpx;
    height: 72rpx;
    line-height: 72rpx;
    border-radius: 36rpx;
    color: #fff;
    background: #18181b;
    font-size: 26rpx;
  }
  .module-card {
    margin-top: 22rpx;
    padding: 28rpx;
  }
  .module-header {
    display: flex;
    align-items: center;
    min-height: 92rpx;
  }
  .module-icon {
    width: 76rpx;
    height: 76rpx;
    flex: 0 0 76rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 22rpx;
    color: #fff;
    background: #7c3aed;
    font-size: 30rpx;
    font-weight: 800;
  }
  .module-copy {
    min-width: 0;
    flex: 1;
    margin-left: 20rpx;
  }
  .module-title {
    font-size: 31rpx;
    font-weight: 750;
  }
  .module-desc {
    margin-top: 6rpx;
    color: #81798c;
    font-size: 24rpx;
  }
  .template-count {
    margin-left: 12rpx;
    padding: 8rpx 14rpx;
    border-radius: 999rpx;
    color: #8a8392;
    background: #f2f0f4;
    font-size: 21rpx;
    white-space: nowrap;
  }
  .template-count.ready {
    color: #5b21b6;
    background: #ede9fe;
  }
  .notice-group {
    margin-top: 24rpx;
    padding: 22rpx;
    border-radius: 18rpx;
    background: #fff5f5;
  }
  .recommended-group {
    background: #f7f4ff;
  }
  .group-heading {
    min-height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 27rpx;
    font-weight: 700;
  }
  .group-heading switch {
    transform: scale(0.8);
    transform-origin: right center;
  }
  .locked-text {
    color: #b42318;
    font-size: 22rpx;
    font-weight: 600;
  }
  .notice-row {
    display: flex;
    align-items: flex-start;
    margin-top: 14rpx;
    color: #4f4659;
    font-size: 25rpx;
    line-height: 1.45;
  }
  .check-mark {
    margin-right: 12rpx;
    color: #dc2626;
    font-weight: 800;
  }
  .muted-row {
    color: #686171;
  }
  .small-mark {
    margin-right: 14rpx;
    color: #7c3aed;
    font-weight: 800;
  }
  .module-action {
    margin-top: 24rpx;
    width: 100%;
    height: 84rpx;
    line-height: 84rpx;
    border: 0;
    border-radius: 42rpx;
    color: #fff;
    background: #18181b;
    font-size: 27rpx;
    font-weight: 700;
  }
  .module-action::after {
    border: 0;
  }
  .module-action.disabled {
    color: #88818f;
    background: #eeebf1;
  }
  .footnote {
    padding: 30rpx 12rpx 0;
    color: #7b7484;
    font-size: 23rpx;
    line-height: 1.65;
  }
</style>
