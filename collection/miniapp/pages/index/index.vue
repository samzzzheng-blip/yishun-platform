<!-- 首页，支持店铺装修 -->
<template>
  <view>
    <s-layout
      title="首页"
      navbar="normal"
      tabbar="/pages/index/index"
      onShareAppMessage
      :dark="false"
    >
      <view class="wrap">
        <view class="ad">
          <su-swiper
            :isPreview="false"
            :list="bannerList"
            :height="313"
          />
          <view class="demo-label" v-if="usingDemoBanner">示例展示</view>
        </view>
        <view class="home-actions">
          <view
            class="home-action-card home-action-card--warehouse"
            hover-class="home-action-card--pressed"
            :hover-stay-time="80"
            @tap="sheep.$router.go('/pages/index/store')"
          >
            <image class="home-action-image" src="/static/ui-icons/home-warehouse-card.jpg" mode="aspectFill" aria-hidden="true"></image>
              <view class="home-action-copy">
                <text class="home-action-title">我的仓库</text>
                <text class="home-action-description">管理你的专属藏品</text>
              </view>
              <view class="home-action-arrow" aria-hidden="true"><view class="home-action-chevron"></view></view>
          </view>
          <view class="home-actions-right">
            <view
              class="home-action-card"
              hover-class="home-action-card--pressed"
              :hover-stay-time="80"
              @tap="sheep.$router.go('/pages/collection/add')"
            >
              <image class="home-action-image" src="/static/ui-icons/home-upload-card.jpg" mode="aspectFill" aria-hidden="true"></image>
              <view class="home-action-copy">
                <text class="home-action-title">上传藏品</text>
                <text class="home-action-description">记录每一份
珍贵收藏</text>
              </view>
              <view class="home-action-arrow" aria-hidden="true"><view class="home-action-chevron"></view></view>
            </view>
            <view
              class="home-action-card"
              hover-class="home-action-card--pressed"
              :hover-stay-time="80"
              @tap="sheep.$router.go('/pages/collection/exchange')"
            >
              <image class="home-action-image" src="/static/ui-icons/home-exchange-card.jpg" mode="aspectFill" aria-hidden="true"></image>
              <view class="home-action-copy">
                <text class="home-action-title">兑换中心</text>
                <text class="home-action-description">兑换收藏
专属好礼</text>
              </view>
              <view class="home-action-arrow" aria-hidden="true"><view class="home-action-chevron"></view></view>
            </view>
          </view>
        </view>
        <view
          class="notify"
          hover-class="notify--pressed"
          :hover-stay-time="80"
          @tap="goArticle('首页公告')"
        >
          <image class="notify-bg" src="/static/ui-icons/home-notice-bg.png" mode="scaleToFill" aria-hidden="true"></image>
          <view class="notify-title">{{ noticeName }}</view>
          <image class="notify-arrow" src="/static/ui-icons/notice-arrow.png" mode="aspectFit" aria-hidden="true"></image>
        </view>
        <!-- 藏品发现入口：按钮图形为纯前端 UI，不使用商品图片 -->
        <view class="discovery-panel">
          <view class="discovery-heading">
            <view class="discovery-title">藏品发现</view>
            <view class="discovery-hint">轻触切换</view>
          </view>
          <view class="home-search" :class="{ 'home-search--focused': searchFocused }">
            <image class="home-search-icon" src="/static/ui-icons/search.png" mode="aspectFit" aria-hidden="true"></image>
            <input
              class="home-search-input"
              v-model="searchKeyword"
              type="text"
              confirm-type="search"
              maxlength="40"
              placeholder="搜索商品名称"
              placeholder-style="color:#7b818c"
              :disabled="discoveryLoading"
              @focus="searchFocused = true"
              @blur="searchFocused = false"
              @confirm="submitSearch"
            />
            <view
              class="home-search-clear"
              v-if="searchKeyword"
              aria-label="清空搜索"
              hover-class="home-search-clear--pressed"
              :hover-stay-time="60"
              @tap="clearSearch"
            ></view>
            <view
              class="home-search-submit"
              :class="{ 'home-search-submit--disabled': discoveryLoading }"
              hover-class="home-search-submit--pressed"
              :hover-stay-time="70"
              @tap="submitSearch"
            >搜索</view>
          </view>
          <view class="discovery-list">
            <view
              v-for="item in discoveryTabs"
              :key="item.id"
              class="discovery-item"
              :class="{ 'discovery-item--active': activeDiscovery === item.id }"
              :aria-selected="activeDiscovery === item.id"
              hover-class="discovery-item--pressed"
              :hover-stay-time="80"
              @tap="changeDiscovery(item.id)"
            >
              <view class="discovery-visual" aria-hidden="true">
                <view class="deal-tag" v-if="item.icon === 'deal'">
                  <view class="deal-tag-hole"></view>
                  <view class="deal-tag-line"></view>
                </view>
                <view class="latest-calendar" v-else></view>
              </view>
              <view class="discovery-copy">
                <view class="discovery-name">{{ item.name }}</view>
                <view class="discovery-desc">{{ item.description }}</view>
              </view>
            </view>
          </view>
        </view>
        <!-- 切换内容区：读取真实在售商品数据 -->
        <view class="follow-section">
          <view class="follow-header">
            <view class="follow-heading-copy">
              <view class="follow-title" v-if="activeDiscovery === 'deals'">好物好价</view>
              <view class="follow-title" v-else>最新发售</view>
              <view class="search-summary" v-if="appliedKeyword">“{{ appliedKeyword }}”的搜索结果</view>
            </view>
            <view class="price-sort" v-if="activeDiscovery === 'deals' && !discoveryError">
              <view
                class="price-sort-btn"
                :class="{ 'price-sort-btn--active': dealSortAsc, 'price-sort-btn--disabled': discoveryLoading }"
                :aria-pressed="dealSortAsc"
                :aria-disabled="discoveryLoading"
                aria-label="按价格从低到高排序"
                hover-class="price-sort-btn--pressed"
                :hover-stay-time="70"
                @tap="changePriceSort(true)"
              >
                <text>价格</text><view class="sort-arrow sort-arrow--up" aria-hidden="true"></view>
              </view>
              <view
                class="price-sort-btn"
                :class="{ 'price-sort-btn--active': !dealSortAsc, 'price-sort-btn--disabled': discoveryLoading }"
                :aria-pressed="!dealSortAsc"
                :aria-disabled="discoveryLoading"
                aria-label="按价格从高到低排序"
                hover-class="price-sort-btn--pressed"
                :hover-stay-time="70"
                @tap="changePriceSort(false)"
              >
                <text>价格</text><view class="sort-arrow sort-arrow--down" aria-hidden="true"></view>
              </view>
            </view>
            <view class="discovery-retry" v-if="discoveryError" @tap="loadDiscovery(activeDiscovery, currentDiscoveryPage)">重新加载</view>
          </view>
          <view class="discovery-state discovery-state--loading" v-if="discoveryLoading">
            <view class="loading-track" aria-hidden="true"><view class="loading-line"></view></view>
            <text>正在加载商品…</text>
          </view>
          <view
            class="follow-grid"
            :key="`${activeDiscovery}-${currentDiscoveryPage}`"
            v-else-if="currentDiscoveryItems.length"
          >
            <view
              class="follow-card"
              v-for="(item, index) in currentDiscoveryItems"
              :key="item.id"
              :style="{ animationDelay: `${Math.min(index, 4) * 45}ms` }"
              hover-class="follow-card--pressed"
              :hover-stay-time="80"
              @tap="goProductDetail(item)"
            >
              <view class="follow-cover-wrap">
                <image class="follow-cover" :src="item.cover" mode="aspectFill"></image>
                <view class="follow-badge" v-if="item.badge">{{ item.badge }}</view>
              </view>
              <view class="follow-name">{{ item.name }}</view>
              <view class="deal-meta" v-if="activeDiscovery === 'deals'">
                <text class="deal-price">{{ item.meta }}</text>
                <text class="deal-note">好价在售</text>
              </view>
              <view class="follow-meta" v-else>{{ item.meta }}</view>
            </view>
          </view>
          <view class="discovery-state" v-else>{{ discoveryEmptyText }}</view>
          <view class="discovery-pagination" v-if="!discoveryLoading && !discoveryError && totalDiscoveryPages > 1">
            <view
              class="pagination-btn pagination-nav"
              :class="{ disabled: currentDiscoveryPage === 1 }"
              hover-class="pagination-btn--pressed"
              :hover-stay-time="60"
              @tap="goDiscoveryPage(currentDiscoveryPage - 1)"
            >
              上一页
            </view>
            <view
              class="pagination-btn"
              :class="{ active: item.page === currentDiscoveryPage, disabled: !item.page }"
              v-for="item in discoveryPageItems"
              :key="item.key"
              hover-class="pagination-btn--pressed"
              :hover-stay-time="60"
              @tap="goDiscoveryPage(item.page)"
            >
              {{ item.label }}
            </view>
            <view
              class="pagination-btn pagination-nav"
              :class="{ disabled: currentDiscoveryPage === totalDiscoveryPages }"
              hover-class="pagination-btn--pressed"
              :hover-stay-time="60"
              @tap="goDiscoveryPage(currentDiscoveryPage + 1)"
            >
              下一页
            </view>
          </view>
        </view>
      </view>
    </s-layout>
  </view>
</template>

<script setup>
  import { getAdProductUrl } from './ad-link.mjs'
  import { computed, reactive, ref } from 'vue'
  import { onLoad, onPageScroll, onPullDownRefresh, onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import collectionApi from '@/sheep/api/collection/collection'
  import SellApi from '@/sheep/api/collection/sell'
  import ArticleApi from '@/sheep/api/promotion/article';
  import { DEMO_ASSETS } from '@/sheep/config/demo-content';
  import { fen2yuan } from '@/sheep/hooks/useGoods';

  // TODO 广告区
  const list = ref([])
  const bannerList = computed(() => list.value.length
    ? list.value
    : [{ type: 'image', src: DEMO_ASSETS.banner }],
  )
  const usingDemoBanner = computed(() => list.value.length === 0)
  const noticeName = ref('首页公告')
  // 首页藏品发现按钮仅保存选中状态，按钮图形不依赖商品数据。
  const activeDiscovery = ref('latest')
  const searchKeyword = ref('')
  const appliedKeyword = ref('')
  const searchFocused = ref(false)
  const discoveryTabs = [
    {
      id: 'deals',
      name: '好物好价',
      description: '按价格挑选',
      icon: 'deal',
    },
    {
      id: 'latest',
      name: '最新发售',
      description: '发现新上架',
      icon: 'latest',
    },
  ]
  const discoveryPageSize = 8
  const discoveryPages = reactive({
    deals: 1,
    latest: 1,
  })
  const discoveryItems = reactive({ deals: [], latest: [] })
  const discoveryTotals = reactive({ deals: 0, latest: 0 })
  const discoveryLoading = ref(false)
  const discoveryError = ref(false)
  const dealSortAsc = ref(true)

  const currentDiscoveryPage = computed(() => discoveryPages[activeDiscovery.value])
  const totalDiscoveryPages = computed(() =>
    Math.max(1, Math.ceil(discoveryTotals[activeDiscovery.value] / discoveryPageSize)),
  )
  const currentDiscoveryItems = computed(() => discoveryItems[activeDiscovery.value])
  const discoveryEmptyText = computed(() => {
    if (discoveryError.value) return '商品加载失败，请稍后重试'
    if (appliedKeyword.value) return `未找到“${appliedKeyword.value}”相关商品`
    return '暂无在售商品'
  })
  const discoveryPageItems = computed(() => {
    const current = currentDiscoveryPage.value
    const pages = totalDiscoveryPages.value
    const visible = new Set([1, pages])
    for (let page = current - 1; page <= current + 1; page += 1) {
      if (page > 1 && page < pages) visible.add(page)
    }
    const sorted = Array.from(visible).sort((a, b) => a - b)
    const items = []
    sorted.forEach((page, index) => {
      if (index > 0 && page - sorted[index - 1] > 1) {
        items.push({ key: `ellipsis-${index}`, label: '…', page: null })
      }
      items.push({ key: `page-${page}`, label: page, page })
    })
    return items
  })

  function normalizeProduct(item, index, type, page) {
    const pictures = Array.isArray(item.picUrl) ? item.picUrl : [item.picUrl]
    return {
      id: item.id,
      cover: pictures.find(Boolean) || '',
      name: item.name || item.collectionName || '未命名商品',
      badge: type === 'latest' ? '新上架' : '',
      meta: `￥${fen2yuan(item.price || 0)}`,
    }
  }

  async function loadDiscovery(type, page = 1) {
    discoveryLoading.value = true
    discoveryError.value = false
    try {
      const params = {
        pageNo: page,
        pageSize: discoveryPageSize,
        statusList: 1,
        sortField: type === 'deals' ? 'price' : 'createTime',
        sortAsc: type === 'deals' ? dealSortAsc.value : false,
      }
      if (appliedKeyword.value) params.keyword = appliedKeyword.value
      const res = await SellApi.getGoodPage(params)
      const rows = Array.isArray(res?.data?.list) ? res.data.list : []
      const normalized = rows
        .map((item, index) => normalizeProduct(item, index, type, page))
        .filter((item) => item.cover)
      discoveryItems[type] = normalized
      discoveryTotals[type] = Number(res?.data?.total) || 0
    } catch (error) {
      discoveryItems[type] = []
      discoveryTotals[type] = 0
      discoveryError.value = true
    } finally {
      discoveryLoading.value = false
    }
  }

  function changeDiscovery(type) {
    if (discoveryLoading.value || activeDiscovery.value === type) return
    activeDiscovery.value = type
    loadDiscovery(type, discoveryPages[type])
  }

  function changePriceSort(sortAsc) {
    if (discoveryLoading.value || dealSortAsc.value === sortAsc) return
    dealSortAsc.value = sortAsc
    discoveryPages.deals = 1
    loadDiscovery('deals', 1)
  }

  function submitSearch() {
    if (discoveryLoading.value) return
    const keyword = searchKeyword.value.trim().slice(0, 40)
    searchKeyword.value = keyword
    if (keyword === appliedKeyword.value && currentDiscoveryPage.value === 1) return
    appliedKeyword.value = keyword
    discoveryPages.deals = 1
    discoveryPages.latest = 1
    loadDiscovery(activeDiscovery.value, 1)
  }

  function clearSearch() {
    if (discoveryLoading.value) return
    searchKeyword.value = ''
    if (!appliedKeyword.value) return
    appliedKeyword.value = ''
    discoveryPages.deals = 1
    discoveryPages.latest = 1
    loadDiscovery(activeDiscovery.value, 1)
  }

  function goDiscoveryPage(page) {
    if (!page || discoveryLoading.value || page < 1 || page > totalDiscoveryPages.value || page === currentDiscoveryPage.value) return
    discoveryPages[activeDiscovery.value] = page
    loadDiscovery(activeDiscovery.value, page)
  }

  function goProductDetail(item) {
    sheep.$router.go(`/pages/collection/good?id=${item.id}`)
  }

  function goArticle(title) {
    sheep.$router.go('/pages/public/richtext', {
      title,
    });
  }
  
  ArticleApi.getArticle(null, '首页公告').then((res) => {
    noticeName.value = res?.data?.title || '首页公告'
  }).catch(() => {})

  // 隐藏原生tabBar
  uni.hideTabBar({
    fail: () => {},
  });

  onLoad(() => {
    loadDiscovery(activeDiscovery.value, currentDiscoveryPage.value)
    collectionApi.getAdsList().then((res) => {
      const ads = Array.isArray(res?.data) ? res.data : []
      list.value = ads
        .filter((item) => item?.picUrl)
        .map((item) => ({
          type: 'image',
          src: item.picUrl,
          url: getAdProductUrl(item),
        }))
    }).catch(() => {})
  });
  
  onShow(() => {
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
.wrap {
  min-height: 100%;
  background: var(--ys-page-bg);
  padding: 24rpx 30rpx 48rpx;
  box-sizing: border-box;
  color: var(--ys-text);
}

.ad {
  width: 100%;
  height: 353rpx;
  padding: 12rpx;
  box-sizing: border-box;
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 24rpx;
  overflow: hidden;
  position: relative;
}

.ad-empty {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--ys-surface-muted);
  border-radius: var(--ys-radius-sm);
}

.ad-empty-title {
  color: var(--ys-text);
  font-size: 28rpx;
  font-weight: 600;
}

.ad-empty-desc {
  margin-top: 10rpx;
  color: var(--ys-text-secondary);
  font-size: 22rpx;
}

.demo-label {
  position: absolute;
  top: 28rpx;
  left: 28rpx;
  padding: 7rpx 14rpx;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 600;
  background: rgba(24, 24, 27, 0.78);
  border-radius: 10rpx;
}

.home-actions {
  width: calc(100% - 14rpx);
  margin-top: 24rpx;
  margin-left: 22rpx;
  height: 390rpx;
  display: flex;
  align-items: stretch;
  box-sizing: border-box;
  gap: 14rpx;
}

.home-actions-right {
  width: 0;
  min-width: 0;
  flex: 1 1 0;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.home-action-card {
  position: relative;
  background: #ffffff;
  min-width: 0;
  min-height: 0;
  flex: 1;
  border-radius: 18rpx;
  overflow: hidden;
  transition: transform 120ms ease-out, opacity 120ms ease-out;
}

.home-action-card--warehouse {
  width: 0;
  height: 100%;
  flex: 1 1 0;
}

.home-action-card--pressed {
  opacity: 0.86;
  transform: scale(0.975);
}

.home-action-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
}

.home-action-card::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255,255,255,.64), rgba(255,255,255,.1) 65%);
  pointer-events: none;
}
.home-action-copy {
  position: relative;
  z-index: 1;
  padding: 22rpx 0 0 22rpx;
  width: 178rpx;
  box-sizing: border-box;
}
.home-action-title {
  display: block;
  font-size: 32rpx;
  line-height: 44rpx;
  font-weight: 700;
  color: #202333;
  white-space: nowrap;
}
.home-action-description {
  white-space: pre-line;
  display: block;
  margin-top: 4rpx;
  font-size: 24rpx;
  line-height: 32rpx;
  font-weight: 500;
  color: #343d50;
}
.home-action-card--warehouse .home-action-copy {
  width: 100%;
  padding-right: 16rpx;
}
.home-action-card--warehouse .home-action-title { font-size: 36rpx; line-height: 48rpx; }
.home-action-arrow {
  z-index: 1;
  position: absolute;
  bottom: 16rpx;
  left: 22rpx;
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: rgba(255,255,255,.85);
  display: flex;
  align-items: center;
  justify-content: center;
}
.home-action-card--warehouse .home-action-arrow { top: 122rpx; bottom: auto; }
.home-action-chevron {
  width: 10rpx;
  height: 10rpx;
  border-top: 3rpx solid #4b4569;
  border-right: 3rpx solid #4b4569;
  transform: rotate(45deg);
  margin-left: -4rpx;
}

.notify {
  width: 100%;
  min-height: 58rpx;
  margin-top: 14rpx;
  padding: 0 10rpx 0 22rpx;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  overflow: hidden;
  position: relative;
  transition: transform 120ms ease-out, opacity 120ms ease-out;
}

.notify--pressed {
  opacity: 0.88;
  transform: scale(0.985);
}

.notify-bg {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.notify-title {
  min-width: 0;
  flex: 1;
  color: #ffffff;
  font-size: 22rpx;
  position: relative;
  z-index: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notify-arrow {
  width: 34rpx;
  height: 34rpx;
  margin-left: 12rpx;
  position: relative;
  z-index: 1;
}

.discovery-panel {
  width: 100%;
  margin-top: 24rpx;
  padding: 28rpx;
  box-sizing: border-box;
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 20rpx;
}

.discovery-title {
  color: var(--ys-text);
  font-size: 30rpx;
  font-weight: 700;
}

.discovery-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.discovery-hint {
  color: var(--ys-text-secondary);
  font-size: 21rpx;
}

.home-search {
  width: 100%;
  height: 82rpx;
  margin-top: 24rpx;
  padding-left: 24rpx;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  background: var(--ys-surface-muted);
  border: 1rpx solid var(--ys-border);
  border-radius: 16rpx;
  transition: background-color 180ms ease-out, border-color 180ms ease-out;
}

.home-search--focused {
  background: var(--ys-surface);
  border-color: var(--ys-brand-color);
}

.home-search-icon {
  width: 30rpx;
  height: 30rpx;
  flex: 0 0 auto;
}

.home-search-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  padding: 0 18rpx;
  color: var(--ys-text);
  font-size: 26rpx;
}

.home-search-clear {
  width: 38rpx;
  height: 38rpx;
  position: relative;
  flex: 0 0 auto;
  border-radius: 50%;

  &::before,
  &::after {
    content: '';
    position: absolute;
    width: 20rpx;
    height: 2rpx;
    left: 9rpx;
    top: 18rpx;
    background: var(--ys-text-secondary);
    border-radius: 2rpx;
  }

  &::before {
    transform: rotate(45deg);
  }

  &::after {
    transform: rotate(-45deg);
  }
}

.home-search-clear--pressed {
  background: var(--ys-border);
}

.home-search-submit {
  height: 64rpx;
  min-width: 106rpx;
  margin: 0 8rpx 0 10rpx;
  padding: 0 20rpx;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 600;
  background: var(--ys-brand-color);
  border-radius: 12rpx;
  transition: transform 120ms ease-out, opacity 120ms ease-out;
}

.home-search-submit--pressed {
  opacity: 0.76;
  transform: scale(0.94);
}

.home-search-submit--disabled {
  opacity: 0.52;
}

.discovery-list {
  display: flex;
  gap: 18rpx;
  margin-top: 24rpx;
}

.discovery-item {
  flex: 1;
  min-width: 0;
  height: 168rpx;
  padding: 22rpx;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 18rpx;
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 20rpx;
  overflow: hidden;
  transition: transform 140ms ease-out, background-color 220ms ease-out,
    border-color 220ms ease-out, box-shadow 220ms ease-out;
}

.discovery-item--active {
  color: #ffffff;
  background: var(--ys-brand-color);
  border-color: var(--ys-brand-color);
  box-shadow: 0 10rpx 24rpx rgba(24, 24, 27, 0.14);
}

.discovery-item--pressed {
  transform: scale(0.965);
}

.discovery-visual {
  width: 72rpx;
  height: 72rpx;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ys-text);
  background: var(--ys-surface-muted);
  border: 1rpx solid var(--ys-border);
  border-radius: 18rpx;
}

.discovery-item--active .discovery-visual {
  color: var(--ys-brand-color);
  background: #ffffff;
  border-color: #ffffff;
}

.deal-tag {
  width: 40rpx;
  height: 32rpx;
  position: relative;
  box-sizing: border-box;
  border: 3rpx solid currentColor;
  border-radius: 7rpx;
  transform: rotate(-9deg);
}

.deal-tag-hole {
  width: 6rpx;
  height: 6rpx;
  position: absolute;
  left: 6rpx;
  top: 10rpx;
  box-sizing: border-box;
  border: 2rpx solid currentColor;
  border-radius: 50%;
}

.deal-tag-line {
  width: 13rpx;
  height: 3rpx;
  position: absolute;
  right: 5rpx;
  top: 13rpx;
  background: currentColor;
  border-radius: 3rpx;
}

.discovery-item--active .deal-tag {
  animation: ys-deal-tag 340ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.latest-calendar {
  width: 40rpx;
  height: 38rpx;
  position: relative;
  box-sizing: border-box;
  border: 3rpx solid currentColor;
  border-radius: 8rpx;

  &::before {
    content: '';
    position: absolute;
    left: -3rpx;
    right: -3rpx;
    top: 9rpx;
    border-top: 3rpx solid currentColor;
  }

  &::after {
    content: '';
    position: absolute;
    width: 9rpx;
    height: 9rpx;
    right: 6rpx;
    bottom: 6rpx;
    background: currentColor;
    border-radius: 50%;
  }
}

.discovery-item--active .latest-calendar {
  animation: ys-calendar-arrive 300ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.discovery-item--active .latest-calendar::after {
  animation: ys-calendar-mark 420ms 90ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.discovery-copy {
  flex: 1;
  min-width: 0;
}

.discovery-name {
  color: var(--ys-text);
  font-size: 27rpx;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.discovery-desc {
  margin-top: 8rpx;
  color: var(--ys-text-secondary);
  font-size: 20rpx;
  white-space: nowrap;
}

.discovery-item--active .discovery-name,
.discovery-item--active .discovery-desc {
  color: #ffffff;
}

.follow-section {
  width: 100%;
  margin-top: 24rpx;
  padding: 28rpx;
  box-sizing: border-box;
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 20rpx;
}

.follow-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.follow-heading-copy {
  min-width: 0;
}

.price-sort {
  display: flex;
  gap: 10rpx;
  flex: 0 0 auto;
}

.price-sort-btn {
  min-width: 112rpx;
  height: 88rpx;
  padding: 0 17rpx;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  color: var(--ys-text-secondary);
  font-size: 22rpx;
  font-weight: 600;
  background: var(--ys-surface-muted);
  border: 1rpx solid var(--ys-border);
  border-radius: 14rpx;
  transition: background-color 160ms ease-out, border-color 160ms ease-out,
    color 160ms ease-out, transform 100ms ease-out;
}

.price-sort-btn--active {
  color: #ffffff;
  background: var(--ys-brand-color);
  border-color: var(--ys-brand-color);
}

.price-sort-btn--pressed {
  transform: scale(0.96);
}

.price-sort-btn--disabled {
  opacity: 0.5;
}

.price-sort-btn--disabled.price-sort-btn--pressed {
  transform: none;
}

.sort-arrow {
  width: 0;
  height: 0;
  border-left: 7rpx solid transparent;
  border-right: 7rpx solid transparent;
}

.sort-arrow--up {
  border-bottom: 10rpx solid currentColor;
}

.sort-arrow--down {
  border-top: 10rpx solid currentColor;
}

.follow-title {
  color: var(--ys-text);
  font-size: 30rpx;
  font-weight: 700;
}

.search-summary {
  max-width: 450rpx;
  margin-top: 8rpx;
  overflow: hidden;
  color: var(--ys-text-secondary);
  font-size: 21rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.discovery-retry {
  color: var(--ys-text-secondary);
  font-size: 21rpx;
  padding: 10rpx 0 10rpx 20rpx;
}

.discovery-state {
  padding: 72rpx 0;
  color: var(--ys-text-secondary);
  font-size: 24rpx;
  text-align: center;
}

.discovery-state--loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 22rpx;
}

.loading-track {
  width: 152rpx;
  height: 4rpx;
  overflow: hidden;
  background: var(--ys-border);
  border-radius: 4rpx;
}

.loading-line {
  width: 64rpx;
  height: 100%;
  background: var(--ys-brand-color);
  border-radius: 4rpx;
  animation: ys-loading-line 900ms ease-in-out infinite;
}

.follow-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  margin-top: 24rpx;
}

.follow-card {
  width: calc(50% - 10rpx);
  padding: 16rpx;
  margin-bottom: 20rpx;
  box-sizing: border-box;
  background: #fafafa;
  border: 1rpx solid var(--ys-border);
  border-radius: 18rpx;
  animation: ys-product-arrive 320ms cubic-bezier(0.16, 1, 0.3, 1) both;
  transition: transform 120ms ease-out, background-color 160ms ease-out;
}

.follow-card--pressed {
  background: var(--ys-surface-muted);
  transform: scale(0.975);
}

.follow-cover-wrap {
  width: 100%;
  height: 250rpx;
  position: relative;
  overflow: hidden;
  background: #f1f1f3;
  border-radius: 14rpx;
}

.follow-cover {
  width: 100%;
  height: 100%;
}

.follow-badge {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  padding: 6rpx 12rpx;
  color: var(--ys-brand-color);
  font-size: 20rpx;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 20rpx;
}

.follow-name {
  margin-top: 16rpx;
  color: var(--ys-text);
  font-size: 26rpx;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.follow-meta {
  margin-top: 8rpx;
  color: var(--ys-text-secondary);
  font-size: 22rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.deal-meta {
  height: 34rpx;
  margin-top: 8rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--ys-text);
  font-size: 23rpx;
  font-weight: 600;
}

.deal-price {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--ys-brand-color);
}

.deal-note {
  flex: 0 0 auto;
  margin-left: 12rpx;
  color: var(--ys-text-secondary);
  font-size: 20rpx;
}

.discovery-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12rpx;
  padding-top: 12rpx;
}

.pagination-btn {
  min-width: 54rpx;
  height: 54rpx;
  padding: 0 14rpx;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ys-text);
  font-size: 24rpx;
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 10rpx;
  transition: transform 100ms ease-out, background-color 160ms ease-out;

  &.active {
    color: #ffffff;
    background: var(--ys-brand-color);
    border-color: var(--ys-brand-color);
  }

  &.disabled {
    color: #9ca3af;
    background: #f3f4f6;
  }
}

.pagination-btn--pressed {
  transform: scale(0.92);
}

.pagination-btn.disabled.pagination-btn--pressed {
  transform: none;
}

.pagination-nav {
  min-width: 112rpx;
}

@keyframes ys-deal-tag {
  from {
    opacity: 0.4;
    transform: rotate(-20deg) scale(0.78);
  }
  to {
    opacity: 1;
    transform: rotate(-9deg) scale(1);
  }
}

@keyframes ys-calendar-arrive {
  from {
    opacity: 0.4;
    transform: translateY(6rpx) scale(0.88);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes ys-calendar-mark {
  from {
    opacity: 0;
    transform: scale(0);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes ys-product-arrive {
  from {
    opacity: 0.72;
    transform: translateY(12rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes ys-loading-line {
  0% {
    transform: translateX(-72rpx);
  }
  50% {
    transform: translateX(80rpx);
  }
  100% {
    transform: translateX(160rpx);
  }
}
</style>
