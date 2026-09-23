<!-- 分销 - 佣金明细 -->
<template>
  <s-layout class="wallet-wrap" title="提现记录" navbar="normal">
    

    <su-sticky>
      <!-- 统计 -->
      <view class="filter-box ss-p-x-30 ss-flex ss-col-center ss-row-between">
        <uni-datetime-picker
          v-model="state.date"
          type="daterange"
          @change="onChangeTime"
          :end="state.today"
        >
          <button class="ss-reset-button date-btn">
            <text>{{ dateFilterText }}</text>
            <text class="cicon-drop-down ss-seldate-icon" />
          </button>
        </uni-datetime-picker>

        <view class="total-box">
        </view>
      </view>
    </su-sticky>

    <!-- 转余额弹框 -->
    

    <!-- 钱包记录 -->
    <view style="height: calc(100% - 120rpx);">
		<z-paging ref="paging" v-model="dataList" @query="getLogList" :fixed="false">
		  <!-- 提现列表 -->
		  <view>
		    <view
		      class="wallet-list ss-flex border-bottom"
		      v-for="item in dataList"
		      :key="item.id"
		    >
		      <view class="list-content">
		        <view class="title-box ss-flex ss-row-between ss-m-b-20">
		          <text class="title ss-line-1">{{ item.typeName }}</text>
		          <view class="money">
		            <text class="minus">{{ fen2yuan(item.price) }}</text>
		          </view>
		        </view>
		        <view class="ss-flex ss-row-between ss-col-center">
		          <text class="time">
		            {{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM:ss') }}
		          </text>
		          <button
		            v-if="item.status === 10 && item.type === 5 && item.payTransferId > 0"
		            class="ss-reset-button confirm-btn ss-m-l-20"
		            @tap="onRequestMerchantTransfer(item)"
		          >
		            确认收款
		          </button>
		          <text v-else class="status" :class="'status-' + item.status">{{
		            item.status === 10 ? '转账处理中' : item.statusName
		          }}</text>
		        </view>
                <view v-if="item.auctionId" class="auction-detail">
                  <view>竞拍商品：{{ item.sourceCollectionName }}</view>
                  <view>成交 ¥{{ fen2yuan(item.sourceGrossAmount) }} · 手续费 {{ item.sourceFeeRate }}%（¥{{ fen2yuan(item.sourceFeeAmount) }}）</view>
                  <view v-if="item.status === 0" class="audit-tip">待平台核对收款账户、金额与商品后创建转账单</view>
                </view>
		      </view>
		    </view>
		  </view>
		</z-paging>
	</view>

  </s-layout>
</template>

<script setup>
  import { computed, reactive, ref } from 'vue';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import dayjs from 'dayjs';
  import _ from 'lodash-es';
  import BrokerageApi from '@/sheep/api/trade/brokerage';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import PayTransferApi from '@/sheep/api/pay/transfer';

  const state = reactive({
    showMoney: false,
    summary: {}, // 分销信息

    today: '',
    date: [],
    currentTab: 0,

    price: undefined,
    showModal: false,
  });
  
  

  const tabMaps = [
    {
      name: '提现',
      value: '2',
    },
  ];

  const dateFilterText = computed(() => {
    if (state.date[0] === state.date[1]) {
      return state.date[0];
    } else {
      return state.date.join('~');
    }
  });
  
  const paging = ref(null);
  const dataList = ref([])

  function getLogList(pageNo, pageSize) {
    state.loadStatus = 'loading';
    BrokerageApi.getBrokerageWithdrawPage({
        pageSize,
        pageNo,
        'createTime[0]': state.date[0] + ' 00:00:00',
        'createTime[1]': state.date[1] + ' 23:59:59',
      }).then(res => {
            paging.value.complete(res.data.list);
        }).catch(res => {
			paging.value.complete(false);
		})
  }

  function onChangeTab(e) {
    state.currentTab = e.index;
    paging.value.reload()
  }

  function onChangeTime(e) {
    state.date[0] = e[0];
    state.date[1] = e[e.length - 1];
    paging.value.reload()
  }

  // 确认操作（转账到余额）
  async function onConfirm() {
    if (state.price <= 0) {
      sheep.$helper.toast('请输入正确的金额');
      return;
    }
    uni.showModal({
      title: '提示',
      content: '确认把您的佣金转入到余额钱包中？',
      success: async function (res) {
        if (!res.confirm) {
          return;
        }
        const { code } = await BrokerageApi.createBrokerageWithdraw({
          type: 1, // 钱包
          price: state.price * 100,
        });
        if (code === 0) {
          state.showModal = false;
          onChangeTab({
            index: 1,
          });
        }
      },
    });
  }


  // 微信场景下：用户确认收款
  // 可见 https://pay.weixin.qq.com/doc/v3/merchant/4012716430 文档
  async function onRequestMerchantTransfer(item) {
    const requestMerchantTransfer = sheep.$platform.useProvider()
      ? sheep.$platform.useProvider().requestMerchantTransfer
      : undefined;
    if (!requestMerchantTransfer) {
      sheep.$helper.toast('仅微信平台支持该功能');
      return;
    }
    // 获取提现详情
    const { code, data } = await BrokerageApi.getBrokerageWithdraw(item.id);
    if (code !== 0) {
      return;
    }
    if (data.status === 11) {
      sheep.$helper.toast('该提现单已确认收款');
      item.status = 11;
      return;
    }
    if (!data.transferChannelMchId || !data.transferChannelPackageInfo) {
      sheep.$helper.toast('提现信息异常，请稍后再试');
      return;
    }
    // 调用微信确认收款
    const payTransferId = data.payTransferId;
    await requestMerchantTransfer(
      data.transferChannelMchId,
      data.transferChannelPackageInfo,
      async (res) => {
        if (res.result !== 'success') {
          sheep.$helper.toast(res.errMsg);
          return;
        }
        // 同步转账单状态
        try {
          const syncTransferResult = await PayTransferApi.syncTransfer(payTransferId);
          console.log('syncTransferResult 结果', syncTransferResult);
        } catch (e) {
          console.error('syncTransferResult 异常', e);
        }
        // 查询提现单最新状态
        const { data } = await BrokerageApi.getBrokerageWithdraw(item.id);
        if (data && data.status !== 11) {
          sheep.$helper.toast('确认收款成功，但数据存在延迟，请以实际【微信支付】到账为准');
          return;
        }
        sheep.$helper.toast('确认收款成功');
        // 更新到列表中
        item.status = 11;
      },
    );
  }

  onLoad(async (options) => {
    state.today = dayjs().format('YYYY-MM-DD');
    state.date = [state.today, state.today];
    if (options.type === '2') {
      // 切换到"提现" tab 下
      state.currentTab = 1;
    }
  });
</script>

<style lang="scss" scoped>
  .auction-detail {
    margin-top: 16rpx;
    padding: 14rpx;
    border-radius: 12rpx;
    color: #52525b;
    font-size: 22rpx;
    line-height: 1.5;
    background: #f5f3ff;
  }
  .audit-tip {
    color: #7c3aed;
  }
  // 钱包
  .header-box {
    background-color: $white;
    padding: 30rpx;

    .card-box {
      width: 100%;
      min-height: 300rpx;
      padding: 40rpx;
      background-size: 100% 100%;
      border-radius: 30rpx;
      overflow: hidden;
      position: relative;
      z-index: 1;
      box-sizing: border-box;
      background: var(--ys-brand-color);

      .card-head {
        color: $white;
        font-size: 24rpx;
      }

      .ss-eye-icon {
        font-size: 40rpx;
        color: $white;
      }

      .money-num {
        font-size: 40rpx;
        line-height: normal;
        font-weight: 500;
        color: $white;
        font-family: OPPOSANS;
      }

      .reduce-num {
        font-size: 26rpx;
        font-weight: 400;
        color: $white;
      }

      .withdraw-btn {
        width: 120rpx;
        height: 60rpx;
        line-height: 60rpx;
        border-radius: 30px;
        font-size: 24rpx;
        font-weight: 500;
        background-color: $white;
        color: var(--ui-BG-Main);
      }

      .balance-btn {
        width: 120rpx;
        height: 60rpx;
        line-height: 60rpx;
        border-radius: 30px;
        font-size: 24rpx;
        font-weight: 500;
        color: $white;
        border: 1px solid $white;
      }
    }
  }

  .loading-money {
    margin-top: 56rpx;

    .loading-money-title {
      font-size: 24rpx;
      font-weight: 400;
      color: #ffffff;
      line-height: normal;
      margin-bottom: 30rpx;
    }

    .loading-money-num {
      font-size: 30rpx;
      font-family: OPPOSANS;
      font-weight: 500;
      color: #fefefe;
    }
  }

  // 筛选

  .filter-box {
    height: 120rpx;
    padding: 0 30rpx;
    background-color: $bg-page;

    .total-box {
      font-size: 24rpx;
      font-weight: 500;
      color: $dark-9;
    }

    .date-btn {
      background-color: $white;
      line-height: 54rpx;
      border-radius: 27rpx;
      padding: 0 20rpx;
      font-size: 24rpx;
      font-weight: 500;
      color: $dark-6;

      .ss-seldate-icon {
        font-size: 50rpx;
        color: $dark-9;
      }
    }
  }

  // tab
  .wallet-tab-card {
    .tab-item {
      height: 80rpx;
      position: relative;

      .tab-title {
        font-size: 30rpx;
      }

      .cur-tab-title {
        font-weight: $font-weight-bold;
      }

      .tab-line {
        width: 60rpx;
        height: 6rpx;
        border-radius: 6rpx;
        position: absolute;
        left: 50%;
        transform: translateX(-50%);
        bottom: 2rpx;
        background-color: var(--ui-BG-Main);
      }
    }
  }

  // 钱包记录
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
        width: 400rpx;
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

  .model-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333333;
  }

  .model-subtitle {
    font-size: 26rpx;
    color: #c2c7cf;
  }

  .model-btn {
    width: 100%;
    height: 80rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
    font-weight: 500;
    color: #ffffff;
    line-height: normal;
  }

  .input-box {
    height: 100rpx;

    .unit {
      font-size: 48rpx;
      color: #333;
      font-weight: 500;
      line-height: normal;
    }

    .uni-easyinput__placeholder-class {
      font-size: 30rpx;
      height: 40rpx;
      line-height: normal;
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
