<template>
  <s-layout
    title="藏品转移"
    navbar="normal"
    :dark="false"
    :bgStyle="{ color: 'var(--ys-page-bg)' }"
  >
    <view class="wrap">
      <view class="user-box">
        <view class="user-info" v-if="userInfo">
          <image class="avatar" :src="userInfo.avatar || sheep.$url.static('/static/img/shop/default_avatar.png')"></image>
          <view class="user-name">{{ `${userInfo.nickname}（ID:${userInfo.mobile}）` }}</view>
        </view>
        <uni-easyinput
            v-else
            v-model="uid"
            :styles="inputStyles"
            paddingLeft="20"
            placeholder="请输入用户ID"
          >
          <template v-slot:right>
            <view class="search" @tap="searchUser">搜索</view>
          </template>
        </uni-easyinput>
      </view>
      <view class="transfer-box">
        <view class="left">
			<image class="cover" :src="item.pic" mode="aspectFit"></image>
		</view>
        <view class="right">
          <view class="name">{{decodeURIComponent(item.name)}}</view>
          <view class="stock">已选{{JSON.parse(item.ids).length}}件</view>
        </view>
      </view>
      <view class="add-btn" @tap="submit">确认转移</view>
    </view>
  </s-layout>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app';
import sheep from '@/sheep';
import transferApi from '@/sheep/api/collection/transfer'
const inputStyles = {
  borderColor: '#e5e7eb',
  color: '#18181b',
}

const item = ref({})
const uid = ref('')
const userInfo = ref(null)

onLoad(options => {
	item.value = options
	item.value.pic = options.pic ? decodeURIComponent(options.pic) : ''
})

function searchUser() {
	transferApi.searchUser({uid: uid.value}).then(res => {
		if(res.code === 0) {
			userInfo.value = res.data
		}
	})
}

function submit() {
	if(userInfo.value && userInfo.value.id) {
		transferApi.submit({toUserId: userInfo.value.id, collectionIds: JSON.parse(item.value.ids) }).then(res => {
      if(res.code === 0) {
        sheep.$router.back()
      }
    })
	} else {
		sheep.$helper.toast('请先搜索用户')
	}
}


</script>

<style lang="scss" scoped>

:deep(.is-input-border) {
  border-radius: 18rpx;
  border-width: 1rpx;
}
:deep(.uni-easyinput) {
  border-radius: 18rpx;
  background-color: var(--ys-surface);
}
:deep(.uni-easyinput__content) {
  color: var(--ys-text);
  background-color: var(--ys-surface);
}

.wrap {
  background: var(--ys-page-bg);
  height: 100%;
  box-sizing: border-box;
  padding: 40rpx 30rpx 60rpx;
  color: var(--ys-text);
}

.search {
  margin-right: 20rpx;
  color: var(--ys-text);
  font-weight: 700;
}

.user-box {
  min-height: 120rpx;
  padding: 30rpx;
  box-sizing: border-box;
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
}
.cover {
  width: 100%;
  height: 100%;
}

.transfer-box {
  display: flex;
  width: 100%;
  align-items: center;
  margin-top: 30rpx;
  padding: 30rpx;
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

.avatar {
  width: 130rpx;
  height: 130rpx;
  border-radius: 50%;
  border: solid 1px #e5e7eb;
}

.user-info {
  text-align: center;
  color: var(--ys-text);
}

</style>
