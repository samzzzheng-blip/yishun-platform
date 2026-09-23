<template>
  <s-layout
    title="藏品取回"
    navbar="normal"
    :dark="false"
    :bgStyle="{ color: 'var(--ys-page-bg)' }"
  >
    <view class="wrap">
      <view class="transfer-box">
        <view class="left">
			<image class="cover" :src="item.pic" mode="aspectFit"></image>
		</view>
        <view class="right">
          <view class="name">{{decodeURIComponent(item.name)}}</view>
          <view class="stock">已选{{JSON.parse(item.ids).length}}件</view>
        </view>
      </view>
      <view
        class="address"
        @tap="onSelectAddress"
      >
        <s-address-item
          v-if="addressInfo"
          hasBorderBottom
          :item="addressInfo"
        >
        <view></view>
        </s-address-item>
        <view class="set-address" v-else>
          设置收货地址
        </view>
	      </view>
	      <view class="auto-delist-tip" v-if="item.autoDelist === '1'">
	        已选藏品正在出售，确认取回后系统会先自动下架，再生成取回申请。
	      </view>
	      <view class="add-btn" @tap="submit">确认取回</view>
    </view>
  </s-layout>
</template>

<script setup>
import { ref } from 'vue'
import AddressApi from '@/sheep/api/member/address'
import sheep from '@/sheep';
import { isEmpty } from 'lodash-es';
import { onShow, onLoad } from '@dcloudio/uni-app';
import transferApi from '@/sheep/api/collection/transfer'

const addressInfo = ref(null)

const item = ref({})

onLoad(options => {
	item.value = options
	item.value.pic = options.pic ? decodeURIComponent(options.pic) : ''
})

function submit() {
	if (addressInfo.value){
		transferApi.getBack({
      collectionIds: JSON.parse(item.value.ids),
      receiverName: addressInfo.value.name,
      receiverMobile: addressInfo.value.mobile,
      receiverAreaName: addressInfo.value.areaName,
      receiverDetailAddress: addressInfo.value.detailAddress,
    }).then(res => {
      if(res.code === 0) {
        sheep.$router.back()
      }
    })
	} else {
		sheep.$helper.toast('请填写地址')
	}
	
	
}

// 选择地址
function onSelectAddress() {
  let emitName = 'SELECT_ADDRESS';
  let addressPage = '/pages/user/address/list?type=select';
  uni.$once(emitName, (e) => {
    changeConsignee(e.addressInfo);
  });
  sheep.$router.go(addressPage);
}

// 更改收货人地址&计算订单信息
async function changeConsignee(address = {}) {
  if (!isEmpty(address)) {
    addressInfo.value = address
  }
}

AddressApi.getDefaultAddress().then(res => {
  addressInfo.value = res.data
})

</script>

<style lang="scss" scoped>
.wrap {
  background: var(--ys-page-bg);
  height: 100%;
  box-sizing: border-box;
  padding: 40rpx 30rpx 60rpx;
  color: var(--ys-text);
}

.set-address {
  text-align: center;
  color: var(--ys-text-secondary);
  background: var(--ys-surface);
  border: 1rpx dashed #cbd5e1;
  border-radius: 18rpx;
  padding: 50rpx 20rpx;
}
.cover {
  width: 100%;
  height: 100%;
}

.transfer-box {
  display: flex;
  width: 100%;
  align-items: center;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-sizing: border-box;
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
  .left {
    width: 199rpx;
    height: 220rpx;
    flex-shrink: 0;
    margin-right: 40rpx;
    padding: 12rpx;
    box-sizing: border-box;
    background: #f3f4f6;
    border-radius: 16rpx;
    overflow: hidden;
  }
  .right {
    :deep(.uni-numbox-btns) {
      background-color: #f3f4f6 !important;
      border-radius: 0 !important;
    }
    :deep(.uni-numbox--text) {
      color: var(--ys-text) !important;
    }
    :deep(.uni-numbox) {
      margin-bottom: 30rpx;
      border: 1rpx solid var(--ys-border);
      box-sizing: border-box;
    }
    :deep(.uni-numbox__value) {
      width: 75px;
    }
    .name {
      color: var(--ys-text);
      margin-bottom: 20rpx;
      font-weight: bold;
      font-size: 30rpx;
    }
    .stock {
      color: var(--ys-text-secondary);
      font-size: 24rpx;
    }
  }
}

.address { margin-top: 20rpx; }

.auto-delist-tip {
  margin-top: 24rpx;
  padding: 22rpx 24rpx;
  color: #6d28d9;
  font-size: 24rpx;
  line-height: 1.6;
  background: #f3e8ff;
  border: 1rpx solid #d8b4fe;
  border-radius: 16rpx;
}

.add-btn {
  margin: 50rpx auto 0;
  border-radius: 18rpx;
  border: 1rpx solid var(--ys-brand-color);
  background-color: var(--ys-brand-color);
  color: #ffffff;
  height: 84rpx;
  width: 100%;
  line-height: 84rpx;
  text-align: center;
  font-weight: bold;
  font-size: 30rpx;
  box-shadow: 0 10rpx 24rpx rgba(24, 24, 27, 0.14);
}

</style>
