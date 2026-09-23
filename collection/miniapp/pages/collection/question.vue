<template>
	<s-layout
	  title="兑换信息"
	  navbar="normal"
	  :dark="false"
	  :bgStyle="{ color: 'var(--ys-page-bg)' }"
	>
	<view class="question-wrap">
		<view class="question-item" v-for="(item, index) in items" :key="index">
			<view class="question">{{item.question}}</view>
			<uni-easyinput
			  v-model="item.answer"
			  :styles="inputStyles1"
			  paddingLeft="2"
			>
			</uni-easyinput>
		</view>
	</view>
	<view class="btn" @tap="submit">确认</view>
	</s-layout>
</template>

<script setup>
import { ref, computed } from 'vue';
import exchangeApi from '@/sheep/api/collection/exchange'
import { onLoad } from '@dcloudio/uni-app';
 import sheep from '@/sheep';
const inputStyles1 = {
  borderColor: '#e5e7eb',
  color: '#18181b',
}
const items = ref([])

const exchangeInfo=ref(null)

const userInfo = computed(() => sheep.$store('user').userInfo);

onLoad((options) => {
	exchangeApi.getExchange({id: options.id}).then(res => {
		exchangeInfo.value = res.data
		items.value = res.data.questions.map(q => ({
			question: q.question,
			answer: ''
		}))
	})
})

function submit() {
	const validateEmpty = items.value.some(i => i.answer.trim() === '')
	if (validateEmpty) {
		sheep.$helper.toast('请填写完整信息')
		return
	}
	exchangeApi.exchange({
		exchangeId: exchangeInfo.value.id,
		exchangeName: exchangeInfo.value.name,
		mobile: userInfo.value.mobile,
		answer: JSON.stringify(items.value)
	}).then(res => {
		if(res.code === 0) {
			sheep.$router.back();
		}
	})
}
</script>

<style lang="scss" scoped>
.question-wrap {
	padding: 40rpx;
	min-height: 100%;
	box-sizing: border-box;
	background: var(--ys-page-bg);
	.question-item {
		margin-bottom: 20rpx;
		padding: 30rpx;
		background: var(--ys-surface);
		border: 1rpx solid var(--ys-border);
		border-radius: 18rpx;
		box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.05);
		.question {
			font-size: 32rpx;
			margin-bottom: 10rpx;
			color: var(--ys-text);
		}
	}
}

:deep(.uni-easyinput__content) {
	color: var(--ys-text);
	background: var(--ys-surface);
}

.btn{
    border-radius: 18rpx;
    border: 1rpx solid var(--ys-brand-color);
    background-color: var(--ys-brand-color);
    color: #ffffff;
    height: 84rpx;
    width: calc(100% - 80rpx);
    margin: 30rpx auto 0;
    line-height: 84rpx;
    text-align: center;
    font-weight: bold;
    font-size: 30rpx;
    box-shadow: 0 10rpx 24rpx rgba(24, 24, 27, 0.14);

  }
</style>
