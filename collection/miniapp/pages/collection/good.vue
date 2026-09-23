<template>
  <s-layout
    title="交易市场"
    navbar="normal"
    :dark="false"
    :bgStyle="{ color: 'var(--ys-page-bg)' }"
  >
    <view class="wrap">
	  <view class="first-wrap" v-if="first">
		  <su-swiper
		    isPreview
		    :list="first.picUrl.map(i => ({type: 'image',
				src: i}))"
		    :height="640"
			:width="560"
		  />
		  <view class="name">{{first.name}}</view>
		  <view class="amount">直购价 ￥{{fen2yuan(first.price)}}</view>
	  </view>
	  <view class="desc ss-p-30" v-if="first?.introduction" v-html="first.introduction.replaceAll('\n', '<br/>')">
	  </view>
      <s-empty
        v-if="!loading && !first"
        icon="/static/demo/empty-collection.jpg"
        text="商品暂时无法显示"
        description="请检查网络后重新加载"
        :showAction="true"
        actionText="重新加载"
        paddingTop="100"
        @clickAction="loadDetail"
      />
    </view>
	<su-fixed bottom placeholder>
		<view class="bottom-btn">
			<view>
				<button class="ss-reset-button contact-btn" open-type="contact"></button>
				<view>客服</view>
			</view>
			<view v-if="first?.status === 3" class="buy-btn" @tap="buy">立即下单</view>
		</view>
	</su-fixed>
    <su-popup :show="checkoutVisible" type="bottom" @close="closeCheckout">
      <view class="checkout">
        <view class="name">请选择收货方式</view>
        <radio-group @change="fulfillmentType = $event.detail.value; checkoutError = ''">
          <label class="delivery-option"><radio value="WAREHOUSE" :checked="fulfillmentType === 'WAREHOUSE'" />入库到我的仓库</label>
          <label class="delivery-option"><radio value="SHIP" :checked="fulfillmentType === 'SHIP'" />直接寄出商品</label>
        </radio-group>
        <view class="delivery-help">{{ fulfillmentType === 'SHIP' ? '支付成功后自动进入取回，等待公司发货，无需再次申请。' : '选择入库后，支付成功的商品归入“直购物品”。' }}</view>
        <button v-if="fulfillmentType === 'SHIP'" class="address-choice" :disabled="submitting" @tap="selectAddress">
          <template v-if="addressInfo">{{ addressInfo.name }} {{ addressInfo.mobile }}<view>{{ addressInfo.areaName }} {{ addressInfo.detailAddress }}</view><view>点击更换收货地址</view></template>
          <template v-else>请选择收货地址</template>
        </button>
        <view v-if="checkoutError" class="checkout-error" role="alert">{{ checkoutError }}</view>
        <button :loading="submitting" :disabled="submitting || !fulfillmentType || (fulfillmentType === 'SHIP' && !addressInfo)" @tap="submitPurchase">确认并去支付 ￥{{ fen2yuan(first?.price || 0) }}</button>
        <button :disabled="submitting" @tap="closeCheckout">取消</button>
      </view>
    </su-popup>
  </s-layout>
</template>

<script setup>
import SellApi from '@/sheep/api/collection/sell'
import { onShow, onLoad } from '@dcloudio/uni-app';
import { ref, onUnmounted } from 'vue'
import AddressApi from '@/sheep/api/member/address'
import { fen2yuan } from '@/sheep/hooks/useGoods';
import sheep from '@/sheep';

const first = ref(null)
const id = ref(null)
const loading = ref(false)
const checkoutVisible = ref(false)
const fulfillmentType = ref('')
const addressInfo = ref(null)
const submitting = ref(false)
const checkoutError = ref('')
function closeCheckout() { if (!submitting.value) checkoutVisible.value = false }
function receiveAddress(event) { addressInfo.value = event.addressInfo; checkoutError.value = '' }
function selectAddress() {
  uni.$off('SELECT_ADDRESS', receiveAddress)
  uni.$once('SELECT_ADDRESS', receiveAddress)
  sheep.$router.go('/pages/user/address/list?type=select')
}
onUnmounted(() => uni.$off('SELECT_ADDRESS', receiveAddress))

onLoad((options) => {
  id.value = options.id
  loadDetail()
})

function loadDetail() {
  if (!id.value) return
  loading.value = true
  first.value = null
  SellApi.getGoodDetail({id: id.value}).then(res => {
	  first.value = res?.data || null
  }).catch(() => {
	  first.value = null
  }).finally(() => {
	  loading.value = false
  })
}

function buy() {
	const isPlatformSelfOperated = !first.value.userId && !first.value.collectionId
	if (first.value.collectionId || isPlatformSelfOperated) {
		checkoutVisible.value = true
        if (!addressInfo.value) AddressApi.getDefaultAddress().then(res => {
          if (!addressInfo.value) addressInfo.value = res.data || null
        }).catch(() => {})
		} 
	else {
		const url = 'https://uni.yishunqianming.com/app/index.html?id=' + first.value.productId
		sheep.$helper.copyText(url,'链接已经复制，请粘贴到浏览器打开')
	}
}

async function submitPurchase() {
  if (submitting.value) return
  checkoutError.value = ''
  if (!fulfillmentType.value) { checkoutError.value = '请选择收货方式'; return }
  const address = addressInfo.value
  if (fulfillmentType.value === 'SHIP' && (!address?.name || !address?.mobile || !address?.areaName || !address?.detailAddress)) {
    checkoutError.value = '请先选择完整的收货地址'; return
  }
  const data = { ykjId: id.value, fulfillmentType: fulfillmentType.value }
  if (fulfillmentType.value === 'SHIP') Object.assign(data, {
    receiverName: address.name, receiverMobile: address.mobile,
    receiverAreaName: address.areaName, receiverDetailAddress: address.detailAddress
  })
  submitting.value = true
  try {
    const res = await SellApi.createYkjOrder(data)
    if (res.code !== 0) { checkoutError.value = res.msg || '下单失败，请重试'; return }
    checkoutVisible.value = false
    sheep.$router.go('/pages/pay/index', { id: res.data })
  } catch (error) { checkoutError.value = '下单失败，请检查网络后重试' }
  finally { submitting.value = false }
}
	
</script>

<style lang="scss" scoped>
.checkout { padding: 32rpx 30rpx calc(32rpx + env(safe-area-inset-bottom)); background: var(--ys-surface); color: var(--ys-text); max-height: 80vh; overflow-y: auto; }
.checkout .name { margin: 0 0 24rpx; font-size: 36rpx; }
.delivery-option { display: flex; align-items: center; min-height: 96rpx; gap: 16rpx; }
.delivery-help { color: var(--ys-text-secondary); font-size: 28rpx; line-height: 1.6; margin: 16rpx 0; }
.checkout button { margin-top: 20rpx; min-height: 96rpx; font-size: 30rpx; }
.address-choice { text-align: left; line-height: 1.6; padding: 24rpx; }
.checkout-error { color: #b42318; padding: 16rpx 0; }
.wrap {
  height: 100%;
  box-sizing: border-box;
  overflow: scroll;
  padding: 30rpx 30rpx 150rpx;
  background: var(--ys-page-bg);
  color: var(--ys-text);
}

.first-wrap {
	width: 100%;
	min-height: 820rpx;
	margin: 0 auto;
	box-sizing: border-box;
	padding: 50rpx 65rpx;
	background: var(--ys-surface);
	border: 1rpx solid var(--ys-border);
	border-radius: 24rpx;
	box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
}



.name {
	margin-top: 30rpx;
	font-size: 40rpx;
	font-weight: bold;
	color: var(--ys-text);
}

.amount {
	margin-top: 10rpx;
	color: var(--ys-text-secondary);
}

.desc {
	margin-top: 30rpx;
	background: var(--ys-surface);
	border: 1rpx solid var(--ys-border);
	border-radius: 18rpx;
	color: var(--ys-text-secondary);
	line-height: 1.8;
}

.bottom-btn {
	height: 100rpx;
	background-color: var(--ys-surface);
	border-top: 1rpx solid var(--ys-border);
	color: var(--ys-text-secondary);
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 0 30rpx;
	
	.contact-btn {
		width: 50rpx;
		height: 50rpx;
		background: url('/static/project/kefu.png') no-repeat center;
		background-size: 100%;
		filter: grayscale(1) brightness(0.25);
	}
	
	.buy-btn {
		height: 60rpx;
		background-color: var(--ys-brand-color);
		padding: 0 30rpx;
		border-radius: 18rpx;
		line-height: 60rpx;
		color: #ffffff;
		font-weight: bold;
	}
}
</style>
