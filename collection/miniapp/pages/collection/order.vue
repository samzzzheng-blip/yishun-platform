<template>
	<s-layout title="交易记录" navbar="normal">
		<su-fixed placeholder>
			<su-tabs
			  :list="state.tabList"
			  :scrollable="false"
			  @change="onTabsChange"
			  :current="state.currentTab"
			/>
		</su-fixed>
		<view style="height: calc(100% - 90rpx);">
		  <!-- 消息列表 -->
		  <z-paging ref="paging" v-model="dataList" @query="getOrderList" :fixed="false">
		  <view
		    class="wallet-list ss-flex border-bottom"
		    v-for="item in dataList"
		    :key="item.id"
		  >
		    <view class="list-content">
		      <view class="title-box ss-m-b-20">
		        <text class="title">{{ categoryMap[item.categoryId] }} * {{item.dealAmount}}</text>
		      </view>
		      <view class="ss-flex ss-row-between ss-col-center">
		        <text class="time">
		          {{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM:ss') }}
		        </text>
		        <button
		          class="ss-reset-button confirm-btn ss-m-l-20"
		          @tap="cancel(item)"
				  v-if="item.amount > 0"
		        >
		          撤单
		        </button>
				<text v-else class="status status-1">
				  已成交</text>
		      </view>
		    </view>
		  </view>
		  </z-paging>
		</view>
		
		
	</s-layout>
</template>

<script setup>
import { reactive, ref } from 'vue';
import exchangeApi from '@/sheep/api/collection/exchange'
import { onLoad, onReachBottom } from '@dcloudio/uni-app';
import CollectionCategoryApi from '@/sheep/api/collection/category';
import _ from 'lodash-es';
import sheep from '@/sheep';

const statusMap = ['待发货', '已发货', '已收货']
	
const state = reactive({
	tabList: [
		{
		  name: '卖单',
		  value: '0',
		},
		{
		  name: '买单',
		  value: '1',
		},
	],
	currentTab: 0
})

function onTabsChange(e) {
	state.currentTab = e.index;
	paging.value.reload()
}

const paging = ref(null);
const dataList = ref([])

function getOrderList(pageNo, pageSize) {
	const api = state.currentTab === 0
	  ? exchangeApi.getSellOrder
	  : exchangeApi.getBuyOrder
    api({pageSize,pageNo}).then(res => {
            paging.value.complete(res.data.list);
        }).catch(res => {
			paging.value.complete(false);
		})
}

function cancel(item) {
	if (state.currentTab === 0) {
		CollectionCategoryApi.cancelSell({
			id: item.id
		}).then(res => {
			paging.value.reload()
		})
	} else {
		CollectionCategoryApi.cancelBuy({
			id: item.id
		}).then(res => {
			paging.value.reload()
		})
	}
}

const categoryMap = ref({})

onLoad(async (options) => {
	CollectionCategoryApi.getCollectionCategory().then(res => {
	  let categorys = res.data.filter(i => i.userId === 0)
	  categorys.forEach(item => {
		  categoryMap.value[item.id] = item.name
	  })
	})
});

  
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