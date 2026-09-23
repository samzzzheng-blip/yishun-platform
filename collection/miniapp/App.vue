<script setup>
  import { onLaunch, onShow, onError } from '@dcloudio/uni-app';
  import { ShoproInit } from './sheep';
  import { maybeShowSubscriptionOnboarding } from '@/sheep/helper/subscription';

  onLaunch(() => {
    // 隐藏原生导航栏 使用自定义底部导航
    uni.hideTabBar({
      fail: () => {},
    });

    // 加载Shopro底层依赖
    ShoproInit().then(() => {
      maybeShowSubscriptionOnboarding();
    });
  });

  onShow(() => {
    // #ifdef APP-PLUS
    // 获取urlSchemes参数
    const args = plus.runtime.arguments;
    if (args) {
    }

    // 获取剪贴板
    uni.getClipboardData({
      success: (res) => {},
    });
    // #endif
  });
</script>

<style lang="scss">
  @import '@/sheep/scss/index.scss';

  page {
    --ys-brand-color: #18181b;
    --ys-page-bg: #f6f7fb;
    --ys-surface: #ffffff;
    --ys-text: #18181b;
    --ys-text-secondary: #6b7280;
    --ys-border: #e5e7eb;
    --ys-border-strong: #d1d5db;
    --ys-surface-muted: #f3f4f6;
    --ys-shadow: 0 12rpx 32rpx rgba(24, 24, 27, 0.06);
    --ys-radius: 20rpx;
    --ys-radius-sm: 14rpx;
  }
</style>
