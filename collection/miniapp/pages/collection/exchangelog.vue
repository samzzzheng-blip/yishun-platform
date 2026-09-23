<template>
	<s-layout title="兑换记录" navbar="normal">
		<z-paging ref="paging" v-model="dataList" @query="getLogList" :fixed="false" height="100%">
		  <!-- 消息列表 -->
		  <view
		    class="wallet-list ss-flex border-bottom"
		    v-for="item in dataList"
		    :key="item.id"
		  >
		    <view class="list-content">
		      <view class="title-box ss-m-b-20">
		        <text class="title">{{ item.exchangeName }}</text>
				<view class="money" v-if="item.status > 0">
				  快递单号：<text class="minus">{{item.deliverCode}}</text>
				</view>
		      </view>
		      <view class="ss-flex ss-row-between ss-col-center">
		        <text class="time">
		          {{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM:ss') }}
		        </text>
		        <button
		          v-if="item.status === 1"
		          class="ss-reset-button confirm-btn ss-m-l-20"
		          @tap="confirmDeliver(item)"
		        >
		          确认收货
		        </button>
		        <text v-else class="status" :class="'status-' + item.status">{{
		          statusMap[item.status]
		        }}</text>
		      </view>
		    </view>
		  </view>
		</z-paging>
	</s-layout>
</template>

<script setup>
import { ref } from 'vue';
import exchangeApi from '@/sheep/api/collection/exchange'
import sheep from '@/sheep';

const statusMap = ['待发货', '已发货', '已收货']
	
const paging = ref(null);
const dataList = ref([])

async function getLogList(pageNo, pageSize) {
  try {
    const res = await exchangeApi.getExchangeLog({ pageNo, pageSize });
    if (res?.code !== 0 || !Array.isArray(res?.data?.list)) {
      paging.value.complete(false);
      return;
    }
    paging.value.complete(res.data.list);
  } catch {
    paging.value.complete(false);
  }
}

function confirmDeliver(item) {
	exchangeApi.confirmDeliver({
		id: item.id,
		status: 2
	}).then(res => {
		if(res.code === 0) {
			item.status = 2
		}
	})
}

  
</script>

<style lang="scss" scoped>
	.wallet-list {
	  padding: 30rpx;
	  background-color: #ffff;
	
	  .head-img {
	    width: 70rpx;
	    height: 70rpx;
	    border-radius: 50%;
	    background: $gray-c;
	  }
	
	  .list-content {
	    justify-content: space-between;
	    align-items: flex-start;
	    flex: 1;
	
	    .title {
	      font-size: 28rpx;
	      color: $dark-3;
	      // width: 400rpx;
	    }
	
	    .time {
	      color: $gray-c;
	      font-size: 22rpx;
	    }
	  }
	
	  .money {
	    font-size: 28rpx;
	    font-family: OPPOSANS;
	
	    .add {
	      color: var(--ui-BG-Main);
	    }
	
	    .minus {
	      color: $dark-3;
	    }
	  }
	
	  .confirm-btn {
	    font-size: 22rpx;
	    color: var(--ui-BG-Main);
	    background: rgba(var(--ui-BG-Main-rgb), 0.1);
	    padding: 4rpx 16rpx;
	    margin: 0;
	    line-height: 1.4;
	    border-radius: 20rpx;
	    border: 1px solid var(--ui-BG-Main);
	  }
	}
	
	
	.status {
	  font-size: 22rpx;
	  &.status-0 {
	    color: #ff9900;
	  }
	  &.status-1 {
	    color: #19be6b;
	  }
	  &.status-2 {
	    color: #fa3534;
	  }
	}
</style>