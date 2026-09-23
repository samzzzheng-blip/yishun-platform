<!-- 地址卡片 -->
<template>
  <view
    class="address-item ss-flex ss-row-between ss-col-center"
    :class="[{ 'border-bottom': props.hasBorderBottom }]"
  >
    <view class="item-left" v-if="!isEmpty(props.item)">
      <view class="area-text ss-flex ss-col-center">
        <uni-tag
          class="ss-m-r-10"
          size="small"
          custom-style="background-color: #18181b; border-color: #18181b; color: #fff;"
          v-if="props.item.defaultStatus"
          text="默认"
        />
        {{ props.item.areaName }}
      </view>
      <view class="address-text">
        {{ props.item.detailAddress }}
      </view>
      <view class="person-text"> {{ props.item.name }} {{ props.item.mobile }} </view>
    </view>
    <view v-else>
      <view class="address-text ss-m-b-10">请选择收货地址</view>
    </view>
    <slot>
      <button class="ss-reset-button edit-btn" @tap.stop="onEdit">
        <view class="edit-icon ss-flex ss-row-center ss-col-center">
          编辑
        </view>
      </button>
    </slot>
  </view>
</template>

<script setup>
  /**
   * 基础组件 - 地址卡片
   *
   * @param {String}  icon = _icon-edit    - icon
   *
   * @event {Function()} click			 - 点击
   * @event {Function()} actionClick		 - 点击工具栏
   *
   * @slot 								 - 默认插槽
   */
  import sheep from '@/sheep';
  import { isEmpty } from 'lodash-es';
  const props = defineProps({
    item: {
      type: Object,
      default() {},
    },
    hasBorderBottom: {
      type: Boolean,
      defult: true,
    },
  });

  const onEdit = () => {
    sheep.$router.go('/pages/user/address/edit', {
      id: props.item.id,
    });
  };
</script>

<style lang="scss" scoped>
  .address-item {
    padding: 24rpx 30rpx;
    background-color: var(--ys-surface);
    margin-bottom: 20rpx;
    border: 1rpx solid var(--ys-border);
    border-radius: 18rpx;
    box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);

    .item-left {
      flex: 1;
      min-width: 0;
    }

    .area-text {
      font-size: 26rpx;
      font-weight: 400;
      color: var(--ys-text-secondary);
    }

    .address-text {
      font-size: 32rpx;
      font-weight: 500;
      color: var(--ys-text);
      line-height: 48rpx;
    }

    .person-text {
      font-size: 28rpx;
      font-weight: 400;
      color: var(--ys-text-secondary);
    }
  }

  .edit-btn {
    flex-shrink: 0;
    width: 100rpx;
    height: 44rpx;
    border-radius: 0;
    border-left: 1rpx solid var(--ys-border);
    padding-left: 20rpx;
    color: var(--ys-text);
    font-size: 26rpx;
  }
  image {
    width: 100%;
    height: 100%;
  }
</style>
