<template>
	<s-layout title="消息" navbar="normal">
		<z-paging ref="paging" v-model="dataList" @query="getLogList" :fixed="false" height="100%">
		  <!-- 消息列表 -->
		  <view
		    class="wallet-list ss-flex border-bottom"
		    v-for="item in dataList"
		    :key="item.id"
		  >
		    <view class="list-content">
		      <view class="title-box ss-flex ss-row-between ss-m-b-20">
		        <text class="title">{{ item.templateContent }}</text>
		      </view>
		      <view class="ss-flex ss-row-between ss-col-center">
		        <text class="time">
		          {{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM:ss') }}
		        </text>
		        <!-- <button
		          v-if="item.status === 10 && item.type === 5 && item.payTransferId > 0"
		          class="ss-reset-button confirm-btn ss-m-l-20"
		          @tap="onRequestMerchantTransfer(item)"
		        >
		          确认收款
		        </button>
		        <text v-else class="status" :class="'status-' + item.status">{{
		          item.statusName
		        }}</text> -->
		      </view>
		    </view>
		  </view>
		</z-paging>
	</s-layout>
</template>

<script setup>
import { reactive, ref } from 'vue';
import tradeApi from '@/sheep/api/collection/trade'
import { onLoad, onReachBottom } from '@dcloudio/uni-app';
import _ from 'lodash-es';
import sheep from '@/sheep';
	
const state = reactive({
	templateType: null
})

const paging = ref(null);
const dataList = ref([])

function getLogList(pageNo, pageSize) {
    tradeApi.getNotify({templateType: state.templateType, pageSize,
          pageNo}).then(res => {
            paging.value.complete(res.data.list);
            const unreadIds = res.data.list.filter(item => !item.readStatus).map(item => item.id);
            if (unreadIds.length) {
                tradeApi.readMessages(unreadIds).then(() => {
                    sheep.$store('app').updateMessgaeNum();
                }).catch(() => {});
            }
        }).catch(res => {
			paging.value.complete(false);
		})
}

onLoad(async (options) => {
	state.templateType = options.id
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
	    font-weight: bold;
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
