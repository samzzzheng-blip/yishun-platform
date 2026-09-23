<!-- 交易市场 -->
<template>
  <s-layout
    title="交易市场"
    tabbar="/pages/index/trade"
    navbar="normal"
    onShareAppMessage
    :dark="false"
  >
    <scroll-view class="wrap" scroll-y :scroll-top="scrollTop" @scroll="onTradeScroll">
      <view class="tabs-wrap">
        <view
          class="tab"
          :class="{ active: currentTab === item.id }"
          v-for="item in tabList"
          :key="item.id"
          @tap="changeTab(item)"
          >{{ item.name }}</view
        >
      </view>
      <view class="line"></view>
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
          <view class="status-filter">
            <view class="filter-button" @tap="showSelect = !showSelect">
              <text>{{ statusName }}</text>
              <view class="dropdown-arrow" :class="{ open: showSelect }"></view>
            </view>
            <view class="status-dropdown" v-if="showSelect">
              <view
                class="status-option"
                :class="{ active: item.id === statusList }"
                v-for="item in filterList"
                :key="item.id"
                @tap="onSelect(item)"
                >{{ item.name }}</view
              >
            </view>
          </view>
          <view class="sort-filter" @tap="changeSort('price')">
            <text>价格</text>
            <view class="sort-arrows">
              <view
                class="sort-arrow up"
                :class="{ active: sortField === 'price' && sortAsc }"
              ></view>
              <view
                class="sort-arrow down"
                :class="{ active: sortField === 'price' && !sortAsc }"
              ></view>
            </view>
          </view>
          <view class="sort-filter" @tap="changeSort('createTime')">
            <text>最新</text>
            <view class="sort-arrows">
              <view
                class="sort-arrow up"
                :class="{ active: sortField === 'createTime' && sortAsc }"
              ></view>
              <view
                class="sort-arrow down"
                :class="{ active: sortField === 'createTime' && !sortAsc }"
              ></view>
            </view>
          </view>
        </view>
      </view>
      <view class="goods-page">
        <view class="preview-strip" v-if="showDemoGoods">
          <view>
            <view class="preview-strip-title">示例商品</view>
            <view class="preview-strip-desc">本地接口不可用，当前展示界面示例</view>
          </view>
          <view class="preview-strip-action" @tap="getGoodList">重试</view>
        </view>
        <view class="other-list">
          <view class="other" v-for="item in dataList" :key="item.id" @tap="goGoodDetail(item)">
            <su-image
              :width="250"
              :height="260"
              :radius="14"
              mode="aspectFit"
              :src="item.picUrl[0]"
            ></su-image>
            <view class="sold" v-if="item.status !== 3"><text class="sold-text">已成交</text></view>
            <view class="name">{{ item.name }}</view>
            <view class="amount">直购价 ￥{{ fen2yuan(item.price) }}</view>
          </view>
        </view>
        <view class="page-state" v-if="loading">加载中...</view>
        <s-empty
          v-else-if="dataList.length === 0"
          icon="/static/demo/empty-collection.jpg"
          text="暂时没有在售商品"
          description="更换筛选条件，或稍后重新加载"
          :showAction="true"
          actionText="重新加载"
          paddingTop="54"
          @clickAction="getGoodList"
        />
        <view class="pagination" v-if="!loading && totalPages > 1">
          <view
            class="page-btn page-arrow"
            :class="{ disabled: currentPage === 1 }"
            @tap="goToPage(currentPage - 1)"
            >上一页</view
          >
          <view
            class="page-btn"
            :class="{ active: item.page === currentPage, ellipsis: !item.page }"
            v-for="item in pageItems"
            :key="item.key"
            @tap="goToPage(item.page)"
            >{{ item.label }}</view
          >
          <view
            class="page-btn page-arrow"
            :class="{ disabled: currentPage === totalPages }"
            @tap="goToPage(currentPage + 1)"
            >下一页</view
          >
        </view>
        <view class="page-summary" v-if="!loading && total > 0"
          >第 {{ currentPage }} / {{ totalPages }} 页，共 {{ total }} 条</view
        >
      </view>
    </scroll-view>
  </s-layout>
</template>

<script setup>
  import { ref, computed, nextTick } from 'vue';
  import { onShow, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import SellApi from '@/sheep/api/collection/sell';
  import { DEMO_GOODS } from '@/sheep/config/demo-content';

  const filterList = ref([
    {
      id: 0,
      name: '全部',
    },
    {
      id: 1,
      name: '在售中',
    },
    {
      id: 2,
      name: '已成交',
    },
  ]);

  const statusName = ref('在售中');

  const statusList = ref(1);

  const showSelect = ref(false);

  function onSelect(item) {
    statusList.value = item.id;
    statusName.value = item.name;
    showSelect.value = false;
    reloadGoodList();
  }

  function onSearch() {
    reloadGoodList();
  }

  function clear() {
    keyword.value = '';
    reloadGoodList();
  }

  const tabList = ref([
    {
      id: 2,
      name: '直购区',
    },
    {
      id: 3,
      name: '竞价区',
    },
  ]);

  const sortField = ref('createTime');

  const keyword = ref('');

  const sortAsc = ref(false);

  function changeSort(field) {
    showSelect.value = false;
    if (sortField.value !== field) {
      if (field === 'createTime') {
        sortAsc.value = false;
      } else {
        sortAsc.value = true;
      }
    } else {
      sortAsc.value = !sortAsc.value;
    }
    sortField.value = field;

    reloadGoodList();
  }

  const currentTab = ref(2);

  function changeTab(item) {
    if (item.id === 3) {
      sheep.$router.go('/pages/auction/demo');
      return;
    }
    showSelect.value = false;
    currentTab.value = item.id;
    reloadGoodList();
  }

  onShow(() => {
    sheep.$store('app').updateMessgaeNum();
    getGoodList();
  });

  const dataList = ref([]);
  const scrollTop = ref(0);
  let currentScrollTop = 0;
  const currentPage = ref(1);
  const pageSize = 20;
  const total = ref(0);
  const loading = ref(false);
  const showDemoGoods = ref(false);
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)));
  const pageItems = computed(() => {
    const pages = totalPages.value;
    if (pages <= 7) {
      return Array.from({ length: pages }, (_, index) => ({
        key: `page-${index + 1}`,
        label: index + 1,
        page: index + 1,
      }));
    }

    const visiblePages = new Set([1, pages]);
    for (let page = currentPage.value - 1; page <= currentPage.value + 1; page += 1) {
      if (page > 1 && page < pages) visiblePages.add(page);
    }
    const sortedPages = Array.from(visiblePages).sort((a, b) => a - b);
    const items = [];
    sortedPages.forEach((page, index) => {
      if (index > 0 && page - sortedPages[index - 1] > 1) {
        items.push({ key: `ellipsis-${index}`, label: '...', page: null });
      }
      items.push({ key: `page-${page}`, label: page, page });
    });
    return items;
  });

  function reloadGoodList() {
    currentPage.value = 1;
    getGoodList();
  }

  function onTradeScroll(event) {
    currentScrollTop = event.detail.scrollTop;
  }

  function scrollToTop() {
    scrollTop.value = currentScrollTop;
    nextTick(() => {
      scrollTop.value = 0;
    });
  }

  function goToPage(page) {
    if (!page || loading.value || page < 1 || page > totalPages.value || page === currentPage.value)
      return;
    currentPage.value = page;
    scrollToTop();
    getGoodList();
  }

  function getGoodList() {
    loading.value = true;
    showDemoGoods.value = false;
    let params = { pageSize, pageNo: currentPage.value, statusList: statusList.value };
    if (sortField.value) {
      params.sortField = sortField.value;
      params.sortAsc = sortAsc.value;
    }
    if (keyword.value) {
      params.keyword = keyword.value;
    }
    SellApi.getGoodPage(params)
      .then((res) => {
        const rows = Array.isArray(res?.data?.list) ? res.data.list : [];
        if (
          rows.length === 0 &&
          !keyword.value &&
          currentPage.value === 1 &&
          statusList.value === 1
        ) {
          dataList.value = DEMO_GOODS;
          total.value = DEMO_GOODS.length;
          showDemoGoods.value = true;
          return;
        }
        dataList.value = rows;
        total.value = Number(res?.data?.total) || 0;
      })
      .catch(() => {
        dataList.value = DEMO_GOODS;
        total.value = DEMO_GOODS.length;
        showDemoGoods.value = true;
      })
      .finally(() => {
        loading.value = false;
      });
  }

  function goGoodDetail(item) {
    if (item.preview) {
      sheep.$helper.toast('当前为界面示例商品');
      return;
    }
    sheep.$router.go('/pages/collection/good?id=' + item.id);
  }

  // 隐藏原生tabBar
  uni.hideTabBar({
    fail: () => {},
  });

  // 下拉刷新
  onPullDownRefresh(() => {
    sheep.$store('app').init();
    setTimeout(function () {
      uni.stopPullDownRefresh();
    }, 800);
  });
</script>

<style lang="scss" scoped>
  .wrap {
    height: 100%;
    overflow: auto;
    padding: 20rpx 40rpx 40rpx;
    box-sizing: border-box;
    background: var(--ys-page-bg);
    color: var(--ys-text);
  }

  .line {
    background-color: var(--ys-border);
    height: 2rpx;
    margin-top: 0;
  }

  .tabs-wrap {
    display: flex;
    overflow-x: auto;
    box-sizing: border-box;
    padding-bottom: 10rpx;
    height: 80rpx;
    .tab {
      flex: 0 0 auto;
      margin-right: 10px;
      font-weight: bold;
      font-size: 30rpx;
      position: relative;
      &.active {
        color: var(--ys-text);
        &::after {
          content: '';
          position: absolute;
          width: 56rpx;
          height: 6rpx;
          border-radius: 6rpx;
          background: #18181b;
          left: calc(50% - 28rpx);
          bottom: -10rpx;
        }
      }
    }
  }

  .trade-section {
    display: flex;
    margin-top: 40rpx;
    .left {
      width: 359rpx;
      height: 627rpx;
      background: var(--ys-surface);
      box-sizing: border-box;
      padding: 30rpx 20rpx;
      border: 1rpx solid var(--ys-border);
      border-radius: 18rpx;
      color: #18181b;
      .sell,
      .buy {
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        height: 170rpx;
      }
      .sell {
        flex-direction: column-reverse;
      }
      .sell-item,
      .buy-item {
        display: flex;
        justify-content: space-between;
        font-size: 24rpx;
        color: #18181b;
        height: 34rpx;
      }
      .deal-title {
        font-size: 24rpx;
        height: 34rpx;
        color: #18181b;
      }
      .buy-item {
        color: #18181b;
      }
    }
    .right {
      padding: 10px 30rpx 0;
      :deep(.uni-numbox-btns) {
        background-color: #f3f4f6 !important;
        border-color: var(--ys-border) !important;
        border-radius: 0 !important;
      }
      :deep(.uni-numbox--text) {
        color: #000 !important;
      }
      :deep(.uni-numbox) {
        margin-bottom: 40rpx;
        border: 1rpx solid var(--ys-border);
      }
      :deep(.uni-numbox__value) {
        width: 75px;
      }

      .name {
        font-weight: bold;
        font-size: 28rpx;
        margin-bottom: 20rpx;
      }

      .buy-btn {
        width: 135px;
        box-sizing: border-box;
        background-color: #f3f4f6;
        color: #000;
        border: 1rpx solid var(--ys-border);
        height: 26px;
        line-height: 26px;
        text-align: center;
        font-weight: bold;
        font-size: 28rpx;
      }
    }
  }

  .latest-price {
    margin: 20rpx 0 0;
    color: #18181b;
    font-weight: 600;
    .latest-price-text {
      color: #18181b;
      font-weight: 700;
    }
  }

  .charts-box {
    width: 100%;
    height: 400rpx;
  }

  .trade-record-link {
    margin-top: 20px;
    color: #18181b;
    text-decoration: underline;
    text-underline-offset: 6rpx;
  }

  .category-market {
    padding: 34rpx 0 20rpx;
  }

  .category-market-title {
    color: #18181b;
    font-size: 34rpx;
    font-weight: 700;
  }

  .category-market-subtitle {
    margin-top: 8rpx;
    color: var(--ys-text-secondary);
    font-size: 24rpx;
  }

  .category-grid {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    margin-top: 28rpx;
  }

  .category-card {
    width: calc(50% - 12rpx);
    min-width: 0;
    padding: 16rpx;
    box-sizing: border-box;
    background: var(--ys-surface);
    border: 1rpx solid var(--ys-border);
    border-radius: 16rpx;
    overflow: hidden;
    margin-bottom: 24rpx;
  }

  .category-image {
    display: block;
    width: 100%;
    height: 270rpx;
    background: #f3f4f6;
    border-radius: 10rpx;
  }

  .category-name {
    margin-top: 16rpx;
    color: #18181b;
    font-size: 27rpx;
    font-weight: 600;
    line-height: 38rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .category-price {
    margin-top: 4rpx;
    color: #18181b;
    font-size: 25rpx;
    font-weight: 700;
  }

  .trade-detail {
    padding-top: 26rpx;
  }

  .detail-nav {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20rpx;
  }

  .back-category {
    padding: 12rpx 20rpx;
    color: #18181b;
    font-size: 24rpx;
    background: var(--ys-surface);
    border: 1rpx solid var(--ys-border);
    border-radius: 10rpx;
  }

  .detail-category-name {
    min-width: 0;
    color: #18181b;
    font-size: 26rpx;
    font-weight: 700;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .other-list {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    box-sizing: border-box;
    padding: 30rpx 0 0;
    .other {
      width: 305.5rpx;
      height: 405.6rpx;
      background: var(--ys-surface);
      margin-bottom: 30rpx;
      box-sizing: border-box;
      padding: 24rpx 26rpx;
      position: relative;
      border: 1rpx solid var(--ys-border);
      border-radius: 18rpx;
      box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
      overflow: hidden;
      .name {
        font-size: 20rpx;
        font-weight: bold;
        color: var(--ys-text);
        width: 250rpx;
        margin: 18rpx 0 4rpx;
        height: 30rpx;
        line-height: 30rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .amount {
        font-size: 20rpx;
        color: var(--ys-text-secondary);
        width: 250rpx;
      }

      .sold {
        position: absolute;
        width: 250rpx;
        height: 260rpx;
        background-color: rgba(24, 24, 27, 0.68);
        left: 26rpx;
        top: 24rpx;
        border-radius: 14rpx;
        display: flex;
        align-items: flex-end;
        justify-content: flex-end;
        padding: 18rpx;
        box-sizing: border-box;

        .sold-text {
          padding: 8rpx 14rpx;
          color: #ffffff;
          font-size: 22rpx;
          font-weight: 700;
          background: rgba(24, 24, 27, 0.88);
          border-radius: 9rpx;
        }
      }
    }
  }

  .goods-page {
    padding-bottom: 30rpx;
  }

  .preview-strip {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24rpx;
    margin-top: 24rpx;
    padding: 22rpx 24rpx;
    background: var(--ys-surface-muted);
    border-radius: 14rpx;
  }

  .preview-strip-title {
    color: var(--ys-text);
    font-size: 25rpx;
    font-weight: 700;
  }

  .preview-strip-desc {
    margin-top: 5rpx;
    color: var(--ys-text-secondary);
    font-size: 21rpx;
  }

  .preview-strip-action {
    flex: 0 0 auto;
    padding: 12rpx 20rpx;
    color: #ffffff;
    font-size: 22rpx;
    font-weight: 600;
    background: var(--ys-brand-color);
    border-radius: 10rpx;
  }

  .page-state {
    padding: 60rpx 0;
    text-align: center;
    color: #aaa;
    font-size: 26rpx;
  }

  .pagination {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    gap: 12rpx;
    padding-top: 10rpx;
  }

  .page-btn {
    min-width: 52rpx;
    height: 52rpx;
    padding: 0 12rpx;
    box-sizing: border-box;
    border: 2rpx solid var(--ys-brand-color);
    border-radius: 8rpx;
    color: var(--ys-text);
    background: var(--ys-surface);
    font-size: 24rpx;
    line-height: 48rpx;
    text-align: center;

    &.active {
      background-color: var(--ys-brand-color);
      border-color: var(--ys-brand-color);
      color: #fff;
    }

    &.disabled {
      opacity: 0.35;
    }

    &.ellipsis {
      border-color: transparent;
    }
  }

  .page-arrow {
    min-width: 96rpx;
  }

  .page-summary {
    padding-top: 18rpx;
    text-align: center;
    color: #aaa;
    font-size: 22rpx;
  }

  .search-wrap {
    height: 130rpx;
  }

  :deep(.uni-searchbar__box) {
    height: 30px;
    border: 1rpx solid var(--ys-border);
    background: var(--ys-surface) !important;
  }
  :deep(.uni-searchbar__box-search-input) {
    color: var(--ys-text);
  }

  .filters {
    position: relative;
    z-index: 20;
    color: var(--ys-text-secondary);
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 34rpx;
  }

  .status-filter {
    position: relative;
  }

  .filter-button {
    display: flex;
    align-items: center;
    gap: 12rpx;
    min-width: 142rpx;
    height: 56rpx;
    padding: 0 20rpx;
    box-sizing: border-box;
    color: #18181b;
    background: var(--ys-surface);
    border: 1rpx solid var(--ys-border);
    border-radius: 10rpx;
    font-size: 25rpx;
  }

  .dropdown-arrow {
    width: 12rpx;
    height: 12rpx;
    margin-top: -6rpx;
    border-right: 2rpx solid #6b7280;
    border-bottom: 2rpx solid #6b7280;
    transform: rotate(45deg);
    transition: transform 0.18s ease;

    &.open {
      margin-top: 6rpx;
      transform: rotate(225deg);
    }
  }

  .status-dropdown {
    position: absolute;
    left: 0;
    top: 66rpx;
    width: 190rpx;
    padding: 8rpx;
    box-sizing: border-box;
    background: var(--ys-surface);
    border: 1rpx solid var(--ys-border);
    border-radius: 12rpx;
    box-shadow: 0 12rpx 30rpx rgba(24, 24, 27, 0.1);
    overflow: hidden;
    animation: dropdown-slide 0.18s ease-out;
  }

  .status-option {
    height: 58rpx;
    padding: 0 18rpx;
    color: #4b5563;
    font-size: 24rpx;
    font-weight: 500;
    line-height: 58rpx;
    border-radius: 8rpx;

    &.active {
      color: #18181b;
      font-weight: 700;
      background: #f3f4f6;
    }
  }

  .sort-filter {
    display: flex;
    align-items: center;
    gap: 9rpx;
    height: 56rpx;
    color: #18181b;
    font-size: 25rpx;
  }

  .sort-arrows {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 5rpx;
  }

  .sort-arrow {
    width: 0;
    height: 0;
    border-left: 7rpx solid transparent;
    border-right: 7rpx solid transparent;

    &.up {
      border-bottom: 9rpx solid #c4c7cc;
    }

    &.down {
      border-top: 9rpx solid #c4c7cc;
    }

    &.up.active {
      border-bottom-color: #18181b;
    }

    &.down.active {
      border-top-color: #18181b;
    }
  }

  @keyframes dropdown-slide {
    from {
      opacity: 0;
      transform: translateY(-12rpx);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
</style>
