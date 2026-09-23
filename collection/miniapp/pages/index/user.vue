<!-- 个人中心：支持装修 -->
<template>
  <s-layout
    title="我的"
    tabbar="/pages/index/user"
    navbar="normal"
    onShareAppMessage
    :dark="false"
  >
    <view class="top">
      <SUserCard :stoneAmount="tradeInfo?.stoneAmount" @clickLevel="onProgressClick"></SUserCard>
      <view class="storage-progress" @tap="onProgressClick">
        <view class="progress-left">当前空间容量 已使用{{ getProgressPercent() }}%</view>
        <view class="progress-right">{{ storageCapacity.realAmount || 0 }}/{{ storageCapacity.capacity || 0 }}</view>
        <view class="progress-bar">
          <view
            class="progress-fill"
            :style="{ transform: `scaleX(${getProgressPercent() / 100})` }"
          ></view>
        </view>
      </view>
	  <view class="menus">
        <view class="menu-item" hover-class="menu-item--pressed" @tap="sheep.$router.go('/pages/index/store')">
          <view class="menu-icon menu-icon--warehouse" aria-hidden="true"><view class="menu-icon-shape"></view><view class="menu-icon-detail"></view></view>
          <text class="menu-label">我的仓库</text>
        </view>
        <view class="menu-item" hover-class="menu-item--pressed" @tap="sheep.$router.go('/pages/order/list')">
          <view class="menu-icon menu-icon--orders" aria-hidden="true"><view class="menu-icon-shape"></view><view class="menu-icon-detail"></view></view>
          <text class="menu-label">我的订单</text>
        </view>
        <view class="menu-item" hover-class="menu-item--pressed" @tap="sheep.$router.go('/pages/user/address/list')">
          <view class="menu-icon menu-icon--address" aria-hidden="true"><view class="menu-icon-shape"></view><view class="menu-icon-detail"></view></view>
          <text class="menu-label">我的地址</text>
        </view>
        <view class="menu-item" hover-class="menu-item--pressed" @tap="sheep.$helper.toast('暂未开放')">
          <view class="menu-icon menu-icon--rights" aria-hidden="true"><view class="menu-icon-shape"></view><view class="menu-icon-detail"></view></view>
          <text class="menu-label">我的权益</text>
        </view>
        <view class="menu-item" hover-class="menu-item--pressed" @tap="sheep.$router.go('/pages/collection/exchange')">
          <view class="menu-icon menu-icon--exchange" aria-hidden="true"><view class="menu-icon-shape"></view><view class="menu-icon-detail"></view></view>
          <text class="menu-label">兑换中心</text>
        </view>
	  </view>
    </view>
    <view class="pay-popup-mask" v-if="showPayPopup" @tap="closePayPopup">
      <view class="pay-popup" @tap.stop>
        <view class="pay-header">
          <view class="level-info">当前寄存等级 {{ storageCapacity.storageId || 0 }} 
		  <text v-if="storageCapacity.expireTime">有效期至 {{sheep.$helper.timeFormat(storageCapacity.expireTime, 'yyyy-mm-dd')}}</text>
		  </view>
          <view class="rule-link" @tap="goStorageRule">寄存规则</view>
        </view>
        <view class="pay-card-list">
          <view 
            class="pay-card" 
            :class="{ active: payType === 'month' }"
            @tap="selectPayType('month')"
          >
            <view class="card-title">1个月</view>
            <view class="card-price">{{ monthlyPrice }}</view>
            <view class="card-unit">能量石</view>
          </view>
          <view 
            class="pay-card" 
            :class="{ active: payType === 'year' }"
            @tap="selectPayType('year')"
          >
            <view class="card-title">1年</view>
            <view class="card-price">{{ yearlyPrice }}</view>
            <view class="card-unit">能量石</view>
          </view>
        </view>
        <view class="pay-btn" @tap="onPayClick">立即支付</view>
      </view>
    </view>
    <view class="notice-mask" v-if="showNoticePopup" @tap="closeNoticePopup">
      <view class="notice-popup" @tap.stop>
        <view class="notice-title">购买须知</view>
        <scroll-view
          class="notice-content"
          scroll-y
        >
          <view class="notice-text" v-html="noticeContent"></view>
          <view class="notice-btn" @tap="onNoticeConfirm">我已知晓</view>
        </scroll-view>
      </view>
    </view>
    <view class="ss-p-40">
      <view class="user-info">
        
        <view class="assets-box">
          <view class="assets-top">资产概览</view>
          <view class="assets-bottom">
            <view class="assets-item assets-item-left" @tap="sheep.$router.go('/pages/user/wallet/money')">
              <view class="assets-name">我的资产</view>
              <view class="assets-amount">{{ fen2yuan(userWallet.balance) }}</view>
            </view>
            <view class="assets-item assets-item-center">
              <view class="assets-name">昨日收益</view>
              <view class="assets-amount">{{ fen2yuan(tradeInfo?.income || 0)}}</view>
            </view>
            <view class="assets-item assets-item-right" @tap="goMySell">
              <view class="assets-name">在售物品</view>
              <view class="assets-amount">{{ tradeInfo?.sellAmount || 0 }}</view>
            </view>
          </view>
		  <view class="wallet">
		    <view class="wtool" @tap="sheep.$router.go('/pages/pay/recharge')">充值</view>
		    <view class="wtool middle" @tap="sheep.$router.go('/pages/commission/withdraw')">提现</view>
		  </view>
        </view>
        
        
        <view class="tool" hover-class="tool--pressed" @tap.stop="openSettings">
          <text>设置</text><view class="arrow"></view>
        </view>
        <view class="tool" @tap="goArticle('关于')"><text>关于</text><view class="arrow"></view></view>
        <view class="tool"><button class="ss-reset-button contact-btn" open-type="contact">联系客服</button><view class="arrow"></view></view>
        <button
          class="loginout-btn ss-reset-button"
          hover-class="loginout-btn--pressed"
          @tap="onLogout"
          v-if="isLogin"
        >
          退出登录
        </button>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { onLoad, onShow, onPageScroll, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import SUserCard from '@/sheep/components/s-user-card/s-user-card.vue';
  import { computed, ref } from 'vue'
  import AuthUtil from '@/sheep/api/member/auth';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import sellApi from '@/sheep/api/collection/sell'
  import StoragePlanApi from '@/sheep/api/collection/storage-plan';
  import ArticleApi from '@/sheep/api/promotion/article';
  const userWallet = computed(() => sheep.$store('user').userWallet);
  
  const isLogin = computed(() => sheep.$store('user').isLogin);

  const currentTab = ref(1)

  function changeTab(v) {
    currentTab.value = v
  }

  function goMySell() {
    if (!isLogin.value) {
      sheep.$router.go('/pages/collection/sell-list')
      return
    }
    sheep.$store('app').collectionTabTarget = 3
    sheep.$router.go('/pages/index/store')
  }
  
  function goArticle(title) {
    sheep.$router.go('/pages/public/richtext', {
      title,
    });
  }

  function openSettings() {
    uni.navigateTo({
      url: '/pages/public/setting',
      fail: (error) => {
        console.error('[个人中心] 打开系统设置失败', error)
        uni.showToast({
          title: '设置页面打开失败，请重新编译小程序',
          icon: 'none',
        })
      },
    })
  }
  
  function handleContact (e) {
	  console.log(e.detail.path)
	  console.log(e.detail.query)
  }

  const tradeInfo = computed(() => sheep.$store('user').tradeInfo);
const storageCapacity = computed(() => sheep.$store('user').storageCapacity);

  function getProgressPercent() {
    const capacity = storageCapacity.value.capacity || 0;
    const realAmount = storageCapacity.value.realAmount || 0;
    if (capacity === 0) return 0;
    return Math.min(100, Math.round((realAmount / capacity) * 100));
  }

  const showPayPopup = ref(false)
  const payType = ref('month')
  const storagePlanId = ref(0)
  const storageId = ref(0)
  const monthlyPrice = ref(0)
  const yearlyPrice = ref(0)

  const showNoticePopup = ref(false)
  const noticeContent = ref('')

  function onProgressClick() {
    // let planId = storageCapacity.value.storageId || 0
    // if (planId === 0) {
    //   planId = 1
    // }
    // StoragePlanApi.getStoragePlan({ id: planId }).then(res => {
    //   if (res.code === 0) {
    //     storagePlanId.value = res.data.id
    //     storageId.value = res.data.id
    //     monthlyPrice.value = res.data.monthlyPrice || 0
    //     yearlyPrice.value = res.data.yearlyPrice || 0
    //     showPayPopup.value = true
    //   }
    // })
  }

  function selectPayType(type) {
    payType.value = type
  }

  function closePayPopup() {
    showPayPopup.value = false
  }

  function goStorageRule() {
    sheep.$router.go('/pages/collection/storage-rule')
  }

  async function onPayClick() {
    showNoticePopup.value = true
    const { data } = await ArticleApi.getArticle(null, '购买须知')
    noticeContent.value = data ? data.content : ''
  }

  function closeNoticePopup() {
    showNoticePopup.value = false
  }

  function onNoticeConfirm() {
    showNoticePopup.value = false
    confirmPay()
  }

  async function confirmPay() {
    const type = payType.value === 'month' ? 1 : 2
    const { code } = await StoragePlanApi.purchaseStoragePlan({
      planId: storagePlanId.value,
      type: type,
    })
    if (code === 0) {
      showPayPopup.value = false
      sheep.$store('user').getStorageCapacity()
    }
  }

  // 退出账号
  function onLogout() {
    uni.showModal({
      title: '提示',
      content: '确认退出账号？',
      success: async function (res) {
        if (!res.confirm) {
          return;
        }
        const { code } = await AuthUtil.logout();
        if (code !== 0) {
          return;
        }
        sheep.$store('user').logout();
        sheep.$store('user').getTradeInfo(true);
        sheep.$store('app').updateMessgaeNum(true)
      },
    });
  }

  // 隐藏原生tabBar
  uni.hideTabBar({
    fail: () => {},
  });

  onLoad((options) => {
    
  });

  onShow(() => {
  sheep.$store('user').getTradeInfo();
	sheep.$store('user').getWallet();
	sheep.$store('user').getStorageCapacity();
	sheep.$store('app').updateMessgaeNum()
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
.tabs-wrap {
  height: 140rpx;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  padding: 0 30rpx 0 30rpx;
  .tab {
    height: 82rpx;
    box-sizing: border-box;
    margin-right: 30rpx;
    font-weight: bold;
    color: var(--ys-text-secondary);
    font-size: 16px;
    border-bottom: 10rpx solid transparent;
    text-align: center;
    line-height: 82rpx;
    &.active {
      color: var(--ys-text);
      font-size: 20px;
      border-bottom: 10rpx solid var(--ys-brand-color);
    }
  }
}

.top {
  box-sizing: border-box;
  height: 480rpx;
  background: var(--ys-surface);
  border-bottom: 1rpx solid var(--ys-border);
  color: var(--ys-text);
}

.top :deep(.nick-name) {
  color: var(--ys-text);
  font-weight: 600;
}

.top :deep(.avatar-box) {
  border-color: var(--ys-border);
}

.storage-progress {
  margin: 0 30rpx;
  padding: 20rpx;
  background-color: var(--ys-page-bg);
  border-radius: 12rpx;
  border: 1rpx solid var(--ys-border);
  
  .progress-left {
    font-size: 24rpx;
    color: var(--ys-text);
    float: left;
  }
  
  .progress-right {
    font-size: 24rpx;
    color: var(--ys-text);
    float: right;
  }
  
  .progress-bar {
    clear: both;
    height: 12rpx;
    background-color: var(--ys-border);
    border-radius: 6rpx;
    margin-top: 40rpx;
    overflow: hidden;
    
    .progress-fill {
      width: 100%;
      height: 100%;
      background-color: var(--ys-brand-color);
      border-radius: 6rpx;
      transform-origin: left center;
      transition: transform 0.3s ease-out;
    }
  }
  
  .progress-text {
    font-size: 20rpx;
    color: #999;
    margin-top: 10rpx;
    text-align: right;
  }
}

.menus {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin: 28rpx 18rpx 0;
  gap: 4rpx;
}

.menu-item {
  flex-direction: column;
  gap: 12rpx;
  width: 0;
  min-width: 0;
  flex: 1 1 0;
  min-height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  transition: transform 120ms ease-out, background-color 120ms ease-out;
}

.menu-item--pressed {
  background: #f1ecff;
  transform: scale(0.94);
}

.menu-icon {
  width: 64rpx;
  height: 64rpx;
  position: relative;
  flex-shrink: 0;
  color: #7044c5;
}
.menu-label {
  font-size: 24rpx;
  line-height: 34rpx;
  font-weight: 600;
  color: #463066;
  white-space: nowrap;
}
.menu-icon-shape, .menu-icon-detail { position: absolute; box-sizing: border-box; }
.menu-icon--warehouse .menu-icon-shape {
  width: 46rpx; height: 40rpx; left: 9rpx; bottom: 2rpx;
  background: #7044c5; border-radius: 3rpx;
}
.menu-icon--warehouse .menu-icon-shape::before {
  content: ''; position: absolute; width: 38rpx; height: 38rpx;
  top: -18rpx; left: 4rpx; background: #7044c5;
  border-top: 6rpx solid #b799ef; border-left: 6rpx solid #b799ef;
  box-sizing: border-box; border-radius: 4rpx; transform: rotate(45deg);
}
.menu-icon--warehouse .menu-icon-detail {
  width: 12rpx; height: 12rpx; left: 18rpx; top: 29rpx;
  background: #dfd0ff; border-radius: 1rpx;
  box-shadow: 16rpx 0 #dfd0ff, 0 16rpx #dfd0ff, 16rpx 16rpx #dfd0ff;
}
.menu-icon--orders .menu-icon-shape {
  width: 48rpx; height: 58rpx; left: 8rpx; top: 5rpx;
  background: #7044c5; border: 4rpx solid #b799ef; border-radius: 7rpx;
}
.menu-icon--orders .menu-icon-shape::before {
  content: ''; position: absolute; width: 28rpx; height: 7rpx;
  left: 6rpx; top: -7rpx; background: #b799ef; border-radius: 3rpx;
}
.menu-icon--orders .menu-icon-detail {
  left: 19rpx; top: 29rpx; width: 14rpx; height: 5rpx;
  background: #fff; border-radius: 3rpx;
}
.menu-icon--orders .menu-icon-detail::after {
  content: ''; position: absolute; left: 0; top: 15rpx;
  width: 26rpx; height: 5rpx; border-radius: 3rpx; background: #c9afff;
}
.menu-icon--address .menu-icon-shape {
  width: 48rpx; height: 48rpx; left: 8rpx; top: 3rpx;
  background: #7044c5; border: 4rpx solid #b799ef;
  border-radius: 50% 50% 50% 6rpx; transform: rotate(-45deg);
}
.menu-icon--address .menu-icon-detail {
  width: 17rpx; height: 17rpx; left: 24rpx; top: 18rpx;
  background: #fff; border: 3rpx solid #ccb2ff; border-radius: 50%;
}
.menu-icon--rights .menu-icon-shape {
  width: 58rpx; height: 58rpx; left: 3rpx; top: 3rpx;
  border: 6rpx solid #a17de8; border-radius: 50%;
}
.menu-icon--rights .menu-icon-detail {
  width: 30rpx; height: 30rpx; left: 17rpx; top: 17rpx;
  background: #7044c5; border: 6rpx solid #c7a8ff; border-radius: 50%;
}
.menu-icon--exchange .menu-icon-shape {
  width: 47rpx; height: 47rpx; left: 2rpx; top: 2rpx;
  border: 6rpx solid #a17de8; border-radius: 8rpx;
}
.menu-icon--exchange .menu-icon-detail {
  width: 36rpx; height: 36rpx; right: 1rpx; bottom: 1rpx;
  border: 6rpx solid #a17de8; border-radius: 8rpx;
  background: #7044c5;
}

.loginout-btn {
  width: 100%;
  height: 88rpx;
  margin-top: 28rpx;
  border: 1rpx solid var(--ys-border);
  border-radius: 14rpx;
  color: #52525b;
  font-size: 28rpx;
  font-weight: 500;
  background-color: var(--ys-surface);
  transition: background-color 0.16s ease-out, color 0.16s ease-out;
}

.loginout-btn--pressed {
  color: #18181b;
  background-color: #f3f4f6;
}

.tool {
  background-color: var(--ys-surface);
  height: 80rpx;
  line-height: 80rpx;
  margin-top: 20rpx;
  box-sizing: border-box;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  color: var(--ys-text);
  border: 1rpx solid var(--ys-border);
  border-radius: 14rpx;
}

.arrow {
  width: 14rpx;
  height: 14rpx;
  border-right: 2rpx solid #9ca3af;
  border-bottom: 2rpx solid #9ca3af;
  margin-left: auto;
  transform: rotate(-45deg);
}
.assets-box {
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 20rpx;
  overflow: hidden;
  .assets-top {
    background-color: var(--ys-surface);
    color: var(--ys-text);
    font-weight: bold;
    height: 80rpx;
    line-height: 80rpx;
    font-size: 32rpx;
    padding-left: 20rpx;
    border-bottom: 1rpx solid var(--ys-border);
  }
  .assets-bottom {
    display: flex;
    box-sizing: border-box;
    padding: 0 20rpx;
    justify-content: space-between;
  }

  .assets-item {
    flex: 1;
    min-width: 0;
  }
  .assets-item-center {
    text-align: center;
  }
  .assets-item-right {
    text-align: right;
  }
  .assets-name {
    height: 30rpx;
    font-size: 24rpx;
    padding-top: 20rpx;
    color: #18181b;
  }
  .assets-amount {
    height: 80rpx;
    line-height: 80rpx;
    font-size: 32rpx;
    color: #18181b;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.wallet {
  display: flex;
  padding: 0 0 20rpx;
  justify-content: space-around;
	  .wtool {
	    height: 60rpx;
		line-height: 60rpx;
		width: 200rpx;
	    color: #18181b;
		text-align: center;
		background-color: #f8f9fb;
		border: 1rpx solid #dfe3e8;
		border-radius: 30rpx;
	  }
	  .middle {
	    background-color: #f8f9fb;
		border-color: #dfe3e8;
		color: #18181b;
	  }

  .last {
    color: #fff;
    text-align: right;
  }
}

.contact-btn {
	flex: 1;
	justify-content: flex-start;
}

.pay-popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 160rpx;
  background: rgba(0, 0, 0, 0.6);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.pay-popup {
  width: 100%;
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 30rpx 30rpx 0 0;
  padding: 40rpx 30rpx 60rpx;
  box-sizing: border-box;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.pay-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.level-info {
  color: var(--ys-text);
  font-size: 28rpx;
  font-weight: bold;
}

.rule-link {
  color: var(--ys-brand-color);
  font-size: 26rpx;
}

.pay-card-list {
  display: flex;
  justify-content: space-between;
  gap: 30rpx;
  margin-bottom: 60rpx;
}

.pay-card {
  flex: 1;
  height: 240rpx;
  border: 1rpx solid var(--ys-border);
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  position: relative;
  transition: all 0.3s;
}

.pay-card.active {
  border-color: var(--ys-brand-color);
  background: var(--ys-surface);
  box-shadow: 0 8rpx 22rpx rgba(24, 24, 27, 0.12);
}

.card-title {
  color: var(--ys-text);
  font-size: 26rpx;
  margin-bottom: 20rpx;
}

.card-price {
  color: var(--ys-text);
  font-size: 56rpx;
  font-weight: bold;
  font-family: OPPOSANS;
}

.card-unit {
  color: var(--ys-text-secondary);
  font-size: 24rpx;
  margin-top: 10rpx;
}

.pay-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  border: 1rpx solid var(--ys-brand-color);
  border-radius: 18rpx;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: bold;
  background: var(--ys-brand-color);
  box-shadow: 0 10rpx 24rpx rgba(24, 24, 27, 0.14);
}

.notice-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice-popup {
  width: 600rpx;
  background: var(--ys-surface);
  border-radius: 24rpx;
  border: 1rpx solid var(--ys-border);
  box-shadow: 0 18rpx 48rpx rgba(24, 24, 27, 0.18);
  overflow: hidden;
}

.notice-title {
  text-align: center;
  color: var(--ys-text);
  font-size: 34rpx;
  font-weight: bold;
  padding: 40rpx 0 20rpx;
}

.notice-content {
  max-height: 600rpx;
  padding: 0 40rpx;
  box-sizing: border-box;
}

.notice-text {
  padding-bottom: 20rpx;
  color: var(--ys-text-secondary);
}

.notice-btn {
  height: 100rpx;
  line-height: 100rpx;
  text-align: center;
  color: var(--ys-text);
  font-size: 32rpx;
  font-weight: bold;
  border-top: 1rpx solid var(--ys-border);
  margin-top: 20rpx;
}
</style>
