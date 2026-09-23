<!-- 消息 -->
<template>
  <s-layout
    title="消息"
    tabbar="/pages/index/message"
    navbar="normal"
    onShareAppMessage
    :dark="false"
  >
    <view class="wrap">
	  <view class="section-title">消息中心</view>
      <view class="msg-item" @tap="sheep.$router.go('/pages/collection/msglist?id=3')">
		  <view class="msg-copy"><view class="msg-title">活动消息</view><view class="msg-desc">平台活动与福利提醒</view></view>
		  <uni-badge
			v-if="msgNum3"
		    size="small"
		    :text="msgNum3"
		  >
		  </uni-badge><view class="msg-arrow"></view>
	  </view>
      <view class="msg-item" @tap="sheep.$router.go('/pages/collection/msglist?id=1')">
		  <view class="msg-copy"><view class="msg-title">系统消息</view><view class="msg-desc">审核、寄存与服务通知</view></view>
		  <uni-badge
			v-if="msgNum1"
		    size="small"
		    :text="msgNum1"
		  >
		  </uni-badge><view class="msg-arrow"></view>
		</view>
      <view class="msg-item" @tap="sheep.$router.go('/pages/collection/msglist?id=2')">
		  <view class="msg-copy"><view class="msg-title">交易消息</view><view class="msg-desc">买卖订单与成交进度</view></view>
		  <uni-badge
			v-if="msgNum2"
		    size="small"
		    :text="msgNum2"
		  >
		  </uni-badge><view class="msg-arrow"></view>
		</view>
    </view>
  </s-layout>
</template>

<script setup>
  import { onLoad, onPageScroll, onPullDownRefresh, onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { ref } from 'vue';
  import tradeApi from '@/sheep/api/collection/trade'
  
  const msgNum1 = ref(0)
  const msgNum2 = ref(0)
  const msgNum3 = ref(0)
  // 隐藏原生tabBar
  uni.hideTabBar({
    fail: () => {},
  });

  onLoad((options) => {
    
  });
  
  onShow(() => {
  	  sheep.$store('app').updateMessgaeNum()
	  tradeApi.getNotify({readStatus: false,templateType: 1,pageSize: 1,
	    pageNo: 1,}).then(res => {
			msgNum1.value = Number(res?.data?.total) || 0
		}).catch(() => { msgNum1.value = 0 })
		tradeApi.getNotify({readStatus: false,templateType: 2,pageSize: 1,
		  pageNo: 1,}).then(res => {
					msgNum2.value = Number(res?.data?.total) || 0
				}).catch(() => { msgNum2.value = 0 })
				tradeApi.getNotify({readStatus: false,templateType: 3,pageSize: 1,
				  pageNo: 1,}).then(res => {
							msgNum3.value = Number(res?.data?.total) || 0
						}).catch(() => { msgNum3.value = 0 })
  })

  // 下拉刷新
  onPullDownRefresh(() => {
    sheep.$store('app').init();
    setTimeout(function () {
      uni.stopPullDownRefresh();
    }, 800);
  });

  onPageScroll(() => {});
</script>

<style lang="scss" scoped>
.wrap {
  min-height: 100%;
  padding: 30rpx;
  box-sizing: border-box;
  background: var(--ys-page-bg);
  color: var(--ys-text);

  .section-title {
    margin: 2rpx 0 24rpx;
    color: var(--ys-text);
    font-size: 32rpx;
    font-weight: 700;
  }

  .msg-item {
    background: var(--ys-surface);
    min-height: 112rpx;
    width: 100%;
    margin: 0 auto 20rpx;
    padding: 0 28rpx;
    color: var(--ys-text);
    box-sizing: border-box;
    display:flex;
    align-items: center;
    justify-content: flex-start;
    border: 1rpx solid var(--ys-border);
    border-radius: 18rpx;
  }

  .msg-copy {
    flex: 1;
    min-width: 0;
  }

  .msg-title {
    color: var(--ys-text);
    font-size: 28rpx;
    font-weight: 700;
  }

  .msg-desc {
    margin-top: 7rpx;
    color: var(--ys-text-secondary);
    font-size: 22rpx;
  }

  .msg-arrow {
    width: 12rpx;
    height: 12rpx;
    margin-left: 18rpx;
    border-top: 2rpx solid #9ca3af;
    border-right: 2rpx solid #9ca3af;
    transform: rotate(45deg);
  }
}
</style>
