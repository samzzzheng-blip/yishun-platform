<!-- 收件地址列表 -->
<template>
  <s-layout :bgStyle="{ color: 'var(--ys-page-bg)' }" title="收货地址" :dark="false">
    <view class="wrap">
      <view class="list-wrap">
        <view v-if="state.list.length">
          <s-address-item
            hasBorderBottom
            v-for="item in state.list"
            :key="item.id"
            :item="item"
            @tap="onSelect(item)"
          />
        </view>
        <s-empty
          v-if="state.list.length === 0 && !state.loading"
          text="暂无收货地址"
          icon="/static/data-empty.png"
        />
      </view>
  
      <view class="footer-box">
        <view
          class="add-btn"
          @tap="sheep.$router.go('/pages/user/address/edit')"
        >
          新增地址
        </view>
        <view
          class="getback-btn"
          @tap="sheep.$router.go('/pages/collection/getbacklog')"
        >
          取回记录
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { onBeforeMount, reactive } from 'vue';
  import { onLoad, onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { isEmpty } from 'lodash-es';
  import AreaApi from '@/sheep/api/system/area';
  import AddressApi from '@/sheep/api/member/address';

  const state = reactive({
    list: [], // 地址列表
    loading: true,
    openType: '', // 页面打开类型
  });

  // 选择收货地址
  const onSelect = (addressInfo) => {
    if (state.openType !== 'select'){ // 不作为选择组件时阻断操作
      return
    }
    uni.$emit('SELECT_ADDRESS', {
      addressInfo,
    });
    sheep.$router.back();
  };

  // 导入微信地址
  function importWechatAddress() {
    let wechatAddress = {};
    // #ifdef MP
    uni.chooseAddress({
      success: (res) => {
        wechatAddress = {
          consignee: res.userName,
          mobile: res.telNumber,
          province_name: res.provinceName,
          city_name: res.cityName,
          district_name: res.countyName,
          address: res.detailInfo,
          region: '',
          is_default: false,
        };
        if (!isEmpty(wechatAddress)) {
          sheep.$router.go('/pages/user/address/edit', {
            data: JSON.stringify(wechatAddress),
          });
        }
      },
      fail: (err) => {
        console.log('%cuni.chooseAddress,调用失败', 'color:green;background:yellow');
      },
    });
    // #endif
    // #ifdef H5
    sheep.$platform.useProvider('wechat').jssdk.openAddress({
      success: (res) => {
        wechatAddress = {
          consignee: res.userName,
          mobile: res.telNumber,
          province_name: res.provinceName,
          city_name: res.cityName,
          district_name: res.countryName,
          address: res.detailInfo,
          region: '',
          is_default: false,
        };
        if (!isEmpty(wechatAddress)) {
          sheep.$router.go('/pages/user/address/edit', {
            data: JSON.stringify(wechatAddress),
          });
        }
      },
    });
    // #endif
  }

  onLoad((option) => {
    if (option.type) {
      state.openType = option.type;
    }
  });

  onShow(async () => {
    state.list = (await AddressApi.getAddressList()).data;
    state.loading = false;
  });

  onBeforeMount(() => {
    if (!!uni.getStorageSync('areaData')) {
      return;
    }
    // 提前加载省市区数据
    AreaApi.getAreaTree().then((res) => {
      if (res.code === 0) {
        uni.setStorageSync('areaData', res.data);
      }
    });
  });
</script>

<style lang="scss" scoped>
  .wrap {
    background: var(--ys-page-bg);
    height: 100%;
    box-sizing: border-box;
    padding: 36rpx 30rpx 50rpx;
    display: flex;
    flex-direction: column;
  }
  .list-wrap {
    flex: 1;
    margin-bottom: 30rpx;
    overflow: auto;
  }
  .footer-box {
    display: flex;
    gap: 20rpx;
  }
  .add-btn {
    flex: 1;
    border-radius: 18rpx;
    border: 1rpx solid var(--ys-brand-color);
    background-color: var(--ys-brand-color);
    color: #ffffff;
    height: 84rpx;
    line-height: 84rpx;
    text-align: center;
    font-weight: bold;
    font-size: 30rpx;
    box-shadow: 0 10rpx 24rpx rgba(24, 24, 27, 0.14);
  }
  .getback-btn {
    flex: 1;
    border-radius: 18rpx;
    border: 1rpx solid var(--ys-border);
    background-color: var(--ys-surface);
    height: 84rpx;
    line-height: 84rpx;
    text-align: center;
    font-weight: bold;
    font-size: 30rpx;
    color: var(--ys-text);
  }
</style>
