<template>
  <s-layout
    title="兑换中心"
    navbar="normal"
    :dark="false"
    :bgStyle="{ color: 'var(--ys-page-bg)' }"
  >
    <view class="wrap">
	  <view class="exchange-log" @tap="goExchangeLog">兑换记录</view>
	  <view class="first-wrap">
		  <su-image :height="520" :width="560" :radius="16" mode="aspectFill" :src="first.picUrl" isPreview></su-image>
		  <view class="name">{{first.name}}</view>
		  <view class="amount" v-if="first.id">兑换需求：{{first.amount}}能量石</view>
		  <view class="add-btn" v-if="first.id" @tap="exchangeFirst">确认兑换</view>
	  </view>
	  <view class="other-list">
		  <view class="other" v-for="item in exchangeList" :key="item.id">
			  <su-image :width="250" :height="220" :radius="12" mode="aspectFill" :src="item.picUrl" isPreview></su-image>
			  <view class="name">{{item.name}}</view>
			  <view class="amount">兑换需求：{{item.amount}}能量石</view>
			  <view class="add-btn" @tap="exchange(item)">确认兑换</view>
		  </view>
	  </view>
    </view>
  </s-layout>
</template>

<script setup>
import { ref, computed } from 'vue'
import exchangeApi from '@/sheep/api/collection/exchange'
import { onShow } from '@dcloudio/uni-app';
 import sheep from '@/sheep';

const exchangeList = ref([])

const first = ref({})

function getList() {
	exchangeApi.getExchangeList().then(res => {
		let list = res.data.filter(i => i.stock > 0)
		if (list.length === 0) {
			return sheep.$helper.toast('暂无可兑换商品')
		}
		first.value = list.shift()
		
		exchangeList.value = list
	})
}

function onChange(cur) {
	curreentIndex.value = cur
	currentExchange.value = exchangeList.value[cur]
}

function exchangeFirst() {
	exchange(first.value)
}

function exchange(item) {
	sheep.$router.go(`/pages/collection/question?id=${item.id}`)
}

function goExchangeLog() {
	sheep.$router.go('/pages/order/list', { tab: 'exchange' })
}

onShow(() => {
	getList()
})




</script>


<style lang="scss" scoped>
.exchange-log {
	text-align: right;
	height: 100rpx;
	box-sizing: border-box;
	padding: 30rpx 30rpx 0 0;
	color: var(--ys-text);
	font-size: 26rpx;
	font-weight: 600;
} 	

.wrap {
  height: 100%;
  box-sizing: border-box;
  overflow: scroll;
  background: var(--ys-page-bg);
  color: var(--ys-text);
}

.first-wrap {
	width: 705rpx;
	min-height: 840rpx;
	margin: 0 auto;
	box-sizing: border-box;
	padding: 50rpx 71.5rpx 50rpx;
	background: var(--ys-surface);
	border: 1rpx solid var(--ys-border);
	border-radius: 24rpx;
	box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
}

.add-btn {
  margin: 50rpx auto 0;
  border-radius: 18rpx;
  border: 1rpx solid var(--ys-brand-color);
  background-color: var(--ys-brand-color);
  color: #ffffff;
  height: 76rpx;
  width: 300rpx;
  line-height: 76rpx;
  text-align: center;
  font-weight: bold;
  font-size: 30rpx;
  box-shadow: 0 10rpx 24rpx rgba(24, 24, 27, 0.14);
}

.name {
	margin-top: 24rpx;
	font-size: 40rpx;
	font-weight: bold;
	color: var(--ys-text);
}

.amount {
	margin-top: 8rpx;
	color: var(--ys-text-secondary);
}

.other-list {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
	box-sizing: border-box;
	padding: 30rpx 40rpx 0;
	.other {
		width: 305.5rpx;
		min-height: 420rpx;
		background: var(--ys-surface);
		border: 1rpx solid var(--ys-border);
		border-radius: 18rpx;
		box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
		margin-bottom: 30rpx;
		box-sizing: border-box;
		padding: 24rpx 26rpx 30rpx;
		.name {
			font-size: 20rpx;
			font-weight: bold;
			width: 250rpx;
			margin: 14rpx 0 4rpx;
			height: 30rpx;
			line-height: 30rpx;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
			color: var(--ys-text);
		}
		
		.amount {
			font-size: 20rpx;
			color: var(--ys-text-secondary);
			width: 250rpx;
			margin: 0;
			letter-spacing: -1px;
		}
		.add-btn {
		  margin: 14rpx auto 0;
		  border-radius: 18rpx;
		  border: 1rpx solid var(--ys-brand-color);
		  background-color: var(--ys-brand-color);
		  color: #ffffff;
		  height: 48rpx;
		  width: 180rpx;
		  line-height: 48rpx;
		  text-align: center;
		  font-weight: bold;
		  font-size: 22rpx;
		  box-shadow: none;
		}
	}
}
</style>
