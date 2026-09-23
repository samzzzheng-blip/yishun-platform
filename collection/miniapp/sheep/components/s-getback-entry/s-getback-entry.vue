<template>
  <button class="getback-entry" hover-class="getback-entry--pressed" @tap="sheep.$router.go('/pages/collection/getbacklog')">取回记录 · 商品与物流详情</button>
</template>
<script setup>
import sheep from '@/sheep';
</script>
<style scoped>
.getback-entry { margin: 24rpx; padding: 20rpx 24rpx; min-height: 48px; line-height: 1.5; font-size: 28rpx; background: var(--ys-surface, #fff); color: var(--ys-text, #18181b); border: 1px solid var(--ys-border, #dedee6); border-radius: 16rpx; }
.getback-entry--pressed { opacity: .7; }
</style>
