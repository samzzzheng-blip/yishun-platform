<!-- 藏品 -->
<template>
  <s-layout
    title="藏品"
    tabbar="/pages/index/store"
    navbar="normal"
    onShareAppMessage
    :dark="false"
  >
    <view class="wrap">
      <GetbackEntry />
      <view class="tabs-wrap">
        <view class="tab" :class="{active: currentTab === 1 }" @tap="changeTab(1)">在库</view>
        <view class="tab" :class="{active: currentTab === 2 }" @tap="changeTab(2)">登记中</view>
        <view class="tab" :class="{active: currentTab === 3 }" @tap="changeTab(3)">我的在售</view>
        <view
          class="add"
          aria-label="登记藏品"
          role="button"
          hover-class="add--pressed"
          :hover-stay-time="80"
          @tap="addStore"
        >
          <image class="add-icon" src="/static/ui-icons/add-collection.png" mode="aspectFit"></image>
        </view>
      </view>
      <!-- 我的在售仍不发起搜索请求；类型筛选在三个页签中都是前端本地过滤。 -->
      <view v-if="currentTab === 2 && parcelEnabled" class="ss-p-30">
        <button @tap="sheep.$router.go('/pages/collection/parcel')">邮寄 / 线下送达 · 查看登记</button>
        <view class="ss-m-t-10">一次可交付多件藏品；线下送达无需单号，收货核对后审核入库。</view>
      </view>
      <view class="search-wrap">
        <uni-search-bar
          class="search-box"
          :radius="5"
          cancelButton="none"
          v-model="keyword"
          bgColor="transparent"
          placeholder=" "
          @confirm="onSearch"
		  @clear="clear"
        />
        <view class="filters">
          <view class="type-filter">
            <view class="filter-button" @tap="showTypeSelect = !showTypeSelect">
              <text>{{ selectedCategoryName }}</text>
              <view class="arrow" :class="{ open: showTypeSelect }"></view>
            </view>
            <view class="type-dropdown" v-if="showTypeSelect">
              <view
                class="type-option"
                :class="{ active: item.name === selectedCategoryName }"
                v-for="item in categoryOptions"
                :key="item.id"
                @tap="selectCategory(item.name)"
              >
                {{ item.name }}
              </view>
            </view>
          </view>
          <view class="filter-button upload-time" @tap="showTypeSelect = false">
            <text>上传时间</text>
            <view class="time-arrows" aria-hidden="true">
              <view class="time-arrow time-arrow-up"></view>
              <view class="time-arrow time-arrow-down"></view>
            </view>
          </view>
        </view>
      </view>
      <template v-if="currentTab !== 3">
      <view class="list-wrap">
        <view class="loading-state" v-if="listLoading">正在加载藏品…</view>
        <view class="list-item" v-for="(item, index) in filteredDataList" :key="item.id" @tap="goDetail(item)">
          <image class="photo" :src="getCollectionCover(item)" mode="aspectFit"></image>
          <view class="right">
            <view class="name">{{item.name}}</view>
            <view class="stock">库存{{item.stock}}</view>
            <view class="id" v-if="currentTab === 2 || showingProducts">ID:{{String(item.id).padStart(10, '0')}}</view>
          </view>
		  <view class="status-wrap">
			  <view class="status" v-if="currentTab === 2" :class="{red: item.status===2}">{{ registrationStatus(item) }}</view>
        <view v-if="currentTab === 2 && parcelStates[item.id]" class="blue" @tap.stop="sheep.$router.go('/pages/collection/parcel', { id: parcelStates[item.id].parcel_id })">包裹详情</view>
        <view class="blue" v-if="currentTab === 2 && item.status === 2 && !parcelStates[item.id]" @tap.stop="edit(item.id)">编辑</view>
        <view v-if="currentTab === 2 && item.status === 2 && parcelStates[item.id]">请查看包裹说明，由工作人员重新核对</view>
        <button v-if="currentTab === 2 && canDeleteRegistration(item, parcelStates, parcelStatesReady)"
          class="registration-delete" :disabled="deletingId !== null" @tap.stop="deleteRegistration(item)">{{ deletingId === item.id ? '删除中…' : '删除登记' }}</button>
			  <view class="status" v-if="showingProducts">{{ warehouseStatus(item) }}</view>
			  <view class="status red" v-if="currentTab === 1 && !showingProducts && item.tradeStatus === 1">变现中</view>
			  <view class="status blue" v-if="currentTab === 1 && !showingProducts && item.tradeStatus === 2">出售中</view>
			  <view class="status green" v-if="currentTab === 1 && !showingProducts && item.getbackStatus !== 0">在途</view>
		  </view>
          <!-- <uni-swipe-action class="swipe" v-if="currentTab === 1 && item.tradeStatus ===0 && item.getbackStatus === 0">
            <uni-swipe-action-item :autoClose="false" :show="item.show" @change="(show) => handleChange(show, index)">
              <view class="swipe-center">
                <view class="arrow-left"></view>
              </view>
              <template v-slot:right>
                <view class="oprate">
				  <template v-if="item.userId !== 0 && !item.copyId">
					  <view class="op-item" @tap.stop="yikoujia(item)">一口价</view>
					  <view class="op-item middle" @tap.stop="fastTrade(item)">快速变现</view>
				  </template>
                </view>
              </template>
            </uni-swipe-action-item>
          </uni-swipe-action> -->
        </view>
        <s-empty
          v-if="!listLoading && filteredDataList.length === 0"
          icon="/static/demo/empty-collection.jpg"
          :text="listError ? '藏品加载失败' : showingProducts ? '没有找到相关藏品' : '这里还没有藏品'"
          :description="listError ? '请检查网络后重新加载' : showingProducts ? '试试其他产品名称，或清空搜索查看全部分类' : '登记并完成审核后，藏品会出现在这里'"
          :showAction="true"
          :actionText="listError ? '重新加载' : showingProducts ? '清空搜索' : '登记藏品'"
          paddingTop="54"
          @clickAction="handleEmptyAction"
        />
      </view>
      </template>
      <SMySellList v-else :key="sellListKey" :categoryName="selectedCategoryName" />
    </view>
    <su-popup :show="showFast" type="center" @close="showFast= false">
		<view class="box">
			<view class="tip">
				快速变现以各种形式为客户寻找下家促成交易（一瞬官方不回收物品，但会以拍卖或者变卖等形式为客户变现，该行为可能以极低价格成交）
			</view>
			<view class="btn" @tap="fastTradeConfirm">确认</view>
		</view>
	</su-popup>
    <su-popup :show="showYikoujia" type="center" @close="onClsoeYkj">
		<view class="box">
			<view class="transfer-box">
			  <view class="left">
				  <view class="photo">
					  <image class="cover" :src="currentCategory.picUrl"></image>
				  </view>
				  <view class="name">{{currentCategory.name}}</view>
			  </view>
			  <view class="right" style="padding-top: 40rpx;">
			    <uni-number-box v-model="price" placeholder="一口价" :step="0.01" :min="0" :max="10000000" />
				<view class="batch-btn" @tap="yikoujiaConfirm">确定</view>
			  </view>
			</view>
		</view>
	</su-popup>
  </s-layout>
</template>

<script setup>
  import GetbackEntry from '@/sheep/components/s-getback-entry/s-getback-entry.vue';
  import { computed, reactive, ref } from 'vue'
  import { onLoad, onPageScroll, onPullDownRefresh, onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import CollectuinCategoryApi from '@/sheep/api/collection/category';
  import sellApi from '@/sheep/api/collection/sell';
  import collectionApi from '@/sheep/api/collection/collection';
  import parcelApi from '@/sheep/api/collection/parcel';
  import { warehouseStatus } from '@/sheep/helper/warehouse-state.mjs';
  import { canDeleteRegistration } from './registration-delete.mjs';
  const parcelStatesReady = ref(false), deletingId = ref(null);
  function deleteRegistration(item) {
    if (deletingId.value !== null || !canDeleteRegistration(item, parcelStates.value, parcelStatesReady.value)) return;
    uni.showModal({
      title: '删除藏品登记',
      content: `确定删除“${item.name}”（编号${item.id}）？请确认实物尚未寄出或送达公司。删除后不再显示，需要时请重新登记。已交付的实物请勿删除。`,
      confirmText: '确认删除',
      success: async ({ confirm }) => {
        if (!confirm || deletingId.value !== null) return;
        deletingId.value = item.id;
        try {
          const res = await collectionApi.deleteRegistration(item.id);
          if (res?.code === 0) {
            dataList.value = dataList.value.filter(row => row.id !== item.id);
            sheep.$helper.toast('登记已删除');
          }
        } catch { sheep.$helper.toast('删除未完成，请刷新状态后重试'); }
        finally { deletingId.value = null; }
      },
    });
  }
  const parcelEnabled = ref(false);
  const parcelStates = ref({});
  function registrationStatus(item) {
    if (item.status === 2) return '驳回';
    if (!parcelEnabled.value) return '审核中';
    const p = parcelStates.value[item.id];
    if (!p) return '待登记交付方式';
    if (p.item_status === 2) return '异常待处理';
    return p.parcel_status === 0 ? '已登记，待收货' : '已收货，待审核';
  }
  import SMySellList from '@/sheep/components/s-my-sell-list/s-my-sell-list.vue';
  import { COLLECTION_CATEGORY_OPTIONS } from '@/sheep/config/collection-category';

  const currentTab = ref(1)
  const sellListKey = ref(0)
  const keyword = ref('')
  const dataList = ref([])
  const listLoading = ref(false)
  const listError = ref(false)
  const showingProducts = ref(false)
  let listRequestId = 0
  const showYikoujia = ref(false)
  const showFast = ref(false)
  const showTypeSelect = ref(false)
  const categoryOptions = COLLECTION_CATEGORY_OPTIONS
  const selectedCategories = reactive({
    1: '全部',
    2: '全部',
    3: '全部',
  })
  const selectedCategoryName = computed(() => selectedCategories[currentTab.value])
  const filteredDataList = computed(() => {
    if (selectedCategoryName.value === '全部') return dataList.value
    return dataList.value.filter((item) => {
      const categoryName = currentTab.value === 1 && !showingProducts.value ? item.name : item.categoryName
      return categoryName === selectedCategoryName.value
    })
  })

  const collectionPlaceholder = '/static/demo/enamel-badge.jpg'

  function getCollectionCover(item) {
    const pictures = Array.isArray(item?.picUrls) ? item.picUrls : []
    const cover = pictures.find((url) => typeof url === 'string' && url.trim()) || item?.picUrl
    return sheep.$url.cdn(cover || collectionPlaceholder)
  }
  
	  const price = ref('')

  const currentCategoryId = ref('')
	  const currentCategory = ref({})

  function onClsoeYkj() {
    showYikoujia.value = false
    price.value = ''
  }

  onShow(() => {
      parcelStatesReady.value = false;
      parcelApi.config().then(async r => {
        parcelEnabled.value = r?.code === 0 && r.data.enabled;
        if (parcelEnabled.value) {
          const states = await parcelApi.states();
          if (states?.code === 0 && Array.isArray(states.data)) {
            parcelStates.value = Object.fromEntries(states.data.map(p => [p.collection_id, p]));
            parcelStatesReady.value = true;
          }
        }
      }).catch(() => { parcelEnabled.value = false; });
	  const appStore = sheep.$store('app')
	  if (appStore.collectionTabTarget) {
	    currentTab.value = appStore.collectionTabTarget
	    appStore.collectionTabTarget = null
	  }
	  if (currentTab.value === 3) {
	    sellListKey.value += 1
	  }
	  getList()
	  sheep.$store('app').updateMessgaeNum()
  })
  
  function clear() {
	  keyword.value = '';
	  if (currentTab.value === 3) return
	  getList()
  }
  
  
  async function getList() {
    const requestId = ++listRequestId
    const tab = currentTab.value
    const search = keyword.value.trim()
    showingProducts.value = tab === 1 && search.length > 0
    dataList.value = []
    listLoading.value = tab !== 3
    listError.value = false
    if (tab === 3) return
    try {
      const res = tab === 1
        ? await (search
          ? collectionApi.getCollection({ status: 1, collectionName: search })
          : CollectuinCategoryApi.getMyCollectionCategory({ categoryName: '' }))
        : await collectionApi.getCollection({ noEqStatus: 1, collectionName: search })
      if (requestId !== listRequestId) return
      if (res?.code !== 0 || !Array.isArray(res.data)) throw new Error('藏品加载失败')
      dataList.value = res.data.map(item => ({ ...item, show: 'none' }))
    } catch {
      if (requestId === listRequestId) listError.value = true
    } finally {
      if (requestId === listRequestId) listLoading.value = false
    }
  }

  function handleEmptyAction() {
    if (listError.value) {
      getList()
      return
    }
    if (showingProducts.value) {
      clear()
      return
    }
    addStore()
  }

  function handleChange(show, index) {
    if (show === 'right') {
      dataList.value[index].show = 'right'
      dataList.value.forEach((item, i) => {
        if (i !== index) {
          item.show = 'none'
        }
      })
    }
  }

  function onSearch() {
	if (currentTab.value === 3) return
	getList()
  }

  function selectCategory(categoryName) {
    selectedCategories[currentTab.value] = categoryName
    showTypeSelect.value = false
  }

  function changeTab(v) {
    if (v === 3 && !sheep.$store('user').isLogin) {
      sheep.$router.go('/pages/collection/sell-list')
      return
    }
    currentTab.value = v
	showTypeSelect.value = false
	keyword.value= ''
	getList()
  }

  function addStore() {
    sheep.$router.go('/pages/collection/add')
  }

  function goDetail(item) {
    if (currentTab.value === 2) return
    if (showingProducts.value) {
      sheep.$router.go('/pages/collection/detail', {
        id: item.categoryId,
        collectionId: item.id,
        type: (item.userId === 0 || item.copyId) ? 1 : 2,
      })
      return
    }
    let type = (item.userId === 0 || item.copyId) ? 1: 2;
    sheep.$router.go(`/pages/collection/detail?id=${item.id}&type=${type}&name=${encodeURIComponent(keyword.value)}` )
  }

  function yikoujia(item) {
    currentCategoryId.value = item.id
	currentCategory.value = item
	  showYikoujia.value = true
  }

  function yikoujiaConfirm() {
    if (!price.value) {
      return sheep.$helper.toast('请填写价格')
    }
    sellApi.createYikoujia({
      categoryId: currentCategoryId.value,
      price: price.value * 100
    }).then(res => {
      onClsoeYkj()
	  getList()
    })
  }

  function fastTrade(item) {
    currentCategoryId.value = item.id
	currentCategory.value = item
	  showFast.value = true
  }

  function fastTradeConfirm() {
    sellApi.createFastTrade({
      categoryId: currentCategoryId.value
    }).then(res => {
      showFast.value = false
	  getList()
    })
  }

  function edit(id) {
    sheep.$router.go('/pages/collection/add?id=' + id )
  }

  // 隐藏原生tabBar
  uni.hideTabBar({
    fail: () => {},
  });

  onLoad((options) => {
    
  });

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
  .registration-delete { min-height: 96rpx; margin: 16rpx 0 0; padding: 16rpx; font-size: 26rpx; line-height: 1.5; color: var(--ys-text); background: var(--ys-surface); border: 1rpx solid var(--ys-border); }
.wrap {
  background: var(--ys-page-bg);
  height: 100%;
  min-height: 100%;
  box-sizing: border-box;
  color: var(--ys-text);
  .tabs-wrap {
    height: 112rpx;
    display: flex;
    align-items: center;
    box-sizing: border-box;
    padding: 0 30rpx;
    background: var(--ys-surface);
    border-bottom: 1rpx solid var(--ys-border);
    .tab {
      height: 78rpx;
      box-sizing: border-box;
      margin-right: 24rpx;
      flex-shrink: 0;
      white-space: nowrap;
      font-weight: bold;
      color: var(--ys-text-secondary);
      font-size: 16px;
      border-bottom: 6rpx solid transparent;
      text-align: center;
      line-height: 78rpx;
      &.active {
        color: var(--ys-text);
        font-size: 20px;
        border-bottom-color: var(--ys-brand-color);
      }
    }
    .add {
      margin-left: auto;
      width: 104rpx;
      height: 104rpx;
      min-width: 48px;
      min-height: 48px;
      flex: 0 0 auto;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      transition: opacity 120ms ease-out;
    }
    .add--pressed {
      opacity: 0.76;
    }
    .add-icon {
      width: 96rpx;
      height: 96rpx;
    }
  }
  .search-wrap {
    height: 180rpx;
    box-sizing: border-box;
    padding: 20rpx 0 0;
  }
  .list-wrap {
    width: auto;
    height: calc(100% - 292rpx);
    margin: 0 30rpx;
    overflow-y: scroll;
  }
}

:deep(.uni-searchbar__box) {
  height: 30px;
  border: 1rpx solid var(--ys-border);
  background: var(--ys-surface) !important;
}
:deep(.uni-searchbar__box-search-input) {
  color: var(--ys-text);
}
:deep(.uni-searchbar) {
  width: auto;
  margin: 0 30rpx;
}

.filters {
  color: var(--ys-text-secondary);
  font-weight: 600;
  display: flex;
  padding-left: 34rpx;
  align-items: center;
  gap: 30rpx;
  position: relative;
  z-index: 20;
}

.type-filter {
  position: relative;
}

.filter-button {
  display: flex;
  align-items: center;
  min-height: 52rpx;
}

.type-dropdown {
  position: absolute;
  top: 58rpx;
  left: 0;
  width: 240rpx;
  max-height: 540rpx;
  overflow-y: auto;
  box-sizing: border-box;
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 14rpx;
  box-shadow: 0 12rpx 30rpx rgba(24, 24, 27, 0.12);
}

.type-option {
  min-height: 66rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  color: var(--ys-text-secondary);
  font-size: 24rpx;
  font-weight: 500;
  border-bottom: 1rpx solid var(--ys-border);

  &:last-child {
    border-bottom: 0;
  }

  &.active {
    color: var(--ys-text);
    font-weight: 700;
    background: #f3f4f6;
  }
}

.arrow {
  width: 12rpx;
  height: 12rpx;
  border-right: 2rpx solid #9ca3af;
  border-bottom: 2rpx solid #9ca3af;
  transform: rotate(45deg);
  margin-left: 10rpx;
  transition: transform 0.2s ease;

  &.open {
    transform: rotate(225deg);
  }
}

.time-arrows {
  width: 18rpx;
  height: 30rpx;
  margin-left: 10rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
}

.time-arrow {
  width: 0;
  height: 0;
  border-left: 7rpx solid transparent;
  border-right: 7rpx solid transparent;
}

.time-arrow-up {
  border-bottom: 8rpx solid #9ca3af;
}

.time-arrow-down {
  border-top: 8rpx solid #9ca3af;
}

.list-item {
  background: var(--ys-surface);
  width: 100%;
  min-height: 178rpx;
  margin-bottom: 20rpx;
  box-sizing: border-box;
  position: relative;
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  border: 1rpx solid var(--ys-border);
  border-radius: 18rpx;
  box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
  .right {
    flex: 1 1 0;
    min-width: 0;
  }
  .photo {
    flex-shrink: 0;
    width: 110rpx;
    height: 130rpx;
    margin-right: 30rpx;
    background: #f3f4f6;
    border-radius: 12rpx;
  }
  .name {
    font-weight: bold;
    font-size: 30rpx;
    white-space: normal;
    overflow-wrap: anywhere;
    word-break: break-word;
    line-height: 1.5;
  }
  .stock {
    font-size: 24rpx;
    color: var(--ys-text-secondary);
  }
  .id {
    font-size: 20rpx;
    margin-top: 30rpx;
    color: var(--ys-text-secondary);
  }
  .swipe {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
  }
  .swipe-center {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    height: 178rpx;
    .arrow-left {
      background: url('https://ysyz2025.oss-cn-hangzhou.aliyuncs.com/static/arrow.png') no-repeat;
      width: 20rpx;
      height: 12rpx;
      background-size: 100%;
      transform: rotate(90deg);
      margin-right: 10rpx;
    }
  }
  .oprate {
    width: 580rpx;
    height: 178rpx;
    display: flex;
    box-sizing: border-box;
    align-items: center;
    background-color: rgba($color: #000000, $alpha: 0.7);
    .op-item {
      height: 30rpx;
      font-size: 24rpx;
      color: var(--ui-BG-Main);
      flex: 1;
      text-align: center;
      &.middle {
        border-left: 2px solid var(--ui-BG-Main);
      }
    }
  }
}

.loading-state {
  padding: 80rpx 0;
  color: var(--ys-text-secondary);
  font-size: 24rpx;
  text-align: center;
}

.box {
	width: 600rpx;
	background: var(--ys-surface);
	border: 1rpx solid var(--ys-border);
	border-radius: 24rpx;
	box-sizing: border-box;
	padding: 50rpx 30rpx;
	color: var(--ys-text);
	box-shadow: 0 18rpx 48rpx rgba(24, 24, 27, 0.18);
}

.tip {
	text-align: justify;
	color: var(--ys-text);
	font-weight: bold;
	letter-spacing: -1px;
	line-height: 1.5;
}

:deep(.uni-popup__wrapper) {
	background-color: transparent !important;
}

.btn{
    border-radius: 18rpx;
    border: 1rpx solid var(--ys-brand-color);
    background-color: var(--ys-brand-color);
    color: #ffffff;
    height: 76rpx;
    width: 300rpx;
    margin: 30rpx auto 0;
    line-height: 76rpx;
    text-align: center;
    font-weight: bold;
    font-size: 30rpx;

  }
  
.cover {
  width: 100%;
  height: 100%;
}
  
.transfer-box {
  display: flex;
  width: 100%;
  padding-top: 30rpx;
  .left {
	  .photo {
	    width: 199rpx;
	    height: 220rpx;
	    background: #f3f4f6;
	    border-radius: 16rpx;
	    margin-right: 40rpx;
	    margin-bottom: 10rpx;
	    padding: 12rpx;
	    box-sizing: border-box;
	  }
	  .name {
		  font-weight: bold;
		  text-align: center;
		  color: var(--ys-text);
	  }
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
      margin: 10rpx 0;
      border: 1rpx solid var(--ys-border);
      box-sizing: border-box;
    }
    :deep(.uni-numbox__value) {
      width: 75px;
    }
	.desc {
		font-size: 20rpx;
		color: var(--ys-text-secondary);
	}
	.batch-btn {
		width: 135px;
		background-color: var(--ys-brand-color);
		color: #ffffff;
		height: 26px;
		line-height: 26px;
		text-align: center;
		font-weight: bold;
		font-size: 28rpx;
		margin-top: 20rpx;
		border-radius: 14rpx;
	}
  }
}

.status-wrap {
	flex: 0 0 224rpx;
	width: 224rpx;
	min-width: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	margin-left: 16rpx;
	font-size: 28rpx;
	line-height: 1.5;
	text-align: center;
	white-space: normal;
	overflow-wrap: anywhere;
	> view { max-width: 100%; }
	&:empty { display: none; }
}

.status {
	
	&.red {
		color: red;
	}
	&.blue {
		color: var(--ys-brand-color);
	}
	&.green {
		color: green;
	}
}
</style>
