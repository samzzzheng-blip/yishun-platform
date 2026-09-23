<template>
  <s-layout title="我的仓库" navbar="normal" :dark="false" :bgStyle="{ color: '#f2efff' }">
    <GetbackEntry />
    <view class="wrap" v-if="collectionList.length > 0">
      <view class="warehouse-intro">管理你的专属藏品</view>

      <view class="showcase">
        <view class="showcase-arrow-button" hover-class="showcase-arrow-button--pressed" @tap="switchCurrent(-1)">
          <image
            class="showcase-arrow"
            src="/static/project/warehouse/previous.png"
            mode="aspectFit"
          />
        </view>
        <view class="showcase-photo" @tap="previewCurrent">
          <image
            class="showcase-photo-bg"
            src="/static/project/warehouse/photo-background.png"
            mode="scaleToFill"
          />
          <image
            class="showcase-photo-frame"
            src="/static/project/warehouse/photo-frame.png"
            mode="scaleToFill"
          />
          <image class="showcase-image" :src="currentImage" mode="aspectFill" />
        </view>
        <view class="showcase-arrow-button" hover-class="showcase-arrow-button--pressed" @tap="switchCurrent(1)">
          <image
            class="showcase-arrow"
            src="/static/project/warehouse/next.png"
            mode="aspectFit"
          />
        </view>
      </view>

      <view class="showcase-dots" v-if="collectionList.length > 1">
        <view
          v-for="(_, index) in collectionList"
          :key="index"
          class="showcase-dot"
          :class="{ active: index === currentIndex }"
        />
      </view>

      <view class="collection-summary">
        <view class="summary-title-row">
          <view class="summary-title">{{ currentCollection.name }}</view>
          <view
            class="summary-id"
            @tap="sheep.$helper.copyText(String(currentCollection.id).padStart(10, '0'))"
          >
            ID：{{ String(currentCollection.id).padStart(10, '0') }}
            <image class="copy-btn" :src="sheep.$url.static('/static/project/copy.png')" />
          </view>
        </view>
        <view class="summary-divider" />
        <view class="summary-meta">
          <view class="meta-item">
            <view class="meta-label">数量</view>
            <view class="meta-value">{{ currentCollection.stock || 0 }}</view>
          </view>
          <view class="meta-item">
            <view class="meta-label">入库时间</view>
            <view class="meta-value">{{ currentCreateTime }}</view>
          </view>
          <view class="meta-item">
            <view class="meta-label">状态</view>
            <view class="meta-value status-value">{{ currentStatus }}</view>
          </view>
          <view class="meta-item category-item">
            <view class="meta-label">类型</view>
            <view class="category-value-row">
              <view class="category-badge">{{ currentCategory }}</view>
              <view
                v-if="type != 1"
                class="category-edit-button"
                hover-class="category-edit-button--pressed"
                @tap.stop="editCurrentCategory"
              >
                类型编辑
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="inventory-panel">
        <view class="list-header">
          <view class="list-name">
            在库管理
            <text class="list-count">({{ collectionList.length }})</text>
          </view>
          <view class="check-all-control" @tap="checkAll(!isAllChecked)">
            <SuCheckBox class="check-all" :checked="isAllChecked" @click="checkAll" />
            <view class="check-all-text">全选</view>
          </view>
        </view>
        <view class="list">
          <view
            class="card"
            :class="{ current: currentCollection.id === item.id }"
            @tap="clickItem(item)"
            v-for="item in collectionList"
            :key="item.id"
          >
            <view class="thumb-wrap">
              <image
                class="cover"
                :src="collectionCover(item)"
                mode="aspectFill"
                lazy-load
              />
              <view
                class="check-wrap single-check-box"
                v-if="isGetbackSelectable(item)"
                @tap.stop="singleCheck(!item.checked, item)"
              >
                <SuCheckBox :checked="item.checked" @click="singleCheck($event, item)" />
              </view>
            </view>
            <view class="card-name">{{ item.name }}</view>
            <view class="warehouse-status">{{ warehouseStatus(item) }}</view>
            <button v-if="warehouseAction(item)" class="warehouse-action" :disabled="warehouseBusy !== null" @tap.stop="manageWarehouseItem(item)">{{ warehouseBusy === item.id ? '处理中…' : warehouseAction(item) }}</button>
            <view
              class="deliver-btn"
              v-if="item.getbackStatus === 2"
              @tap.stop="getDeliver(item.id)"
            >
              查看物流
            </view>
          </view>
        </view>
      </view>

      <view class="selection-dock">
        <view class="selected-copy">
          已选择 <text>{{ selectedCount }}</text> 件藏品
        </view>
        <view class="dock-actions">
          <view v-if="type == 1" class="dock-btn dock-btn-primary" @tap="exchangeStone">
            转换能量石
          </view>
          <view v-else class="dock-btn dock-btn-primary" @tap="openSellActions">挂售</view>
          <view class="dock-btn" @tap="goGetBack">取回</view>
        </view>
      </view>
    </view>

    <su-popup :show="showSellActions" type="center" @close="showSellActions = false">
      <view class="box action-box">
        <view class="action-title">选择挂售类型</view>
        <view class="action-subtitle">请选择已勾选藏品的挂售方式</view>
        <view class="action-list">
          <view class="action-item" v-if="canShowAuction" @tap="runSellAction(createAuction)">
            <view class="action-item-title">竞价挂售</view>
            <view class="action-item-desc">设置起拍价、最低加价和竞价时长</view>
          </view>
          <view class="action-item secondary" @tap="runSellAction(yikoujia)">
            <view class="action-item-title">一口价挂售</view>
            <view class="action-item-desc">设置固定售价，买家可直接购买</view>
          </view>
        </view>
      </view>
    </su-popup>
    <su-popup :show="showDeliverCode" type="center" @close="showDeliverCode = false">
      <view class="box">
        <view class="tip">
          {{ deliverCode }}
        </view>
        <view class="btn" @tap="confirmDeilver">确认收货</view>
      </view>
    </su-popup>
    <su-popup :show="exchangeStoneTip" type="center" @close="exchangeStoneTip = false">
      <view class="box">
        <view class="tip"> 是否确认将{{ ids?.length }}个物品转化成能量石？
          <view v-if="Number(categoryId) === 680">签名卡砖每个可兑换 5 个能量石</view> </view>
        <view class="btn" @tap="exchangeStoneConfirm">确认</view>
      </view>
    </su-popup>
    <su-popup :show="showFast" type="center" @close="showFast = false">
      <view class="box">
        <view class="tip">
          快速变现以各种形式为客户寻找下家促成交易（一瞬官方不回收物品，但会以拍卖或者变卖等形式为客户变现，该行为可能以极低价格成交）
        </view>
        <view>手续费：{{ fastRate }}%</view>
        <view class="btn" @tap="fastTradeConfirm">确认</view>
      </view>
    </su-popup>
    <su-popup :show="showYikoujia" type="center" @close="onClsoeYkj">
      <view class="box">
        <view class="transfer-box">
          <view class="left">
            <view class="photo">
              <image v-if="ykjinfo?.picUrl && !sellPhotoFailed" class="cover" :src="ykjinfo.picUrl" mode="aspectFit" @error="sellPhotoFailed = true"></image>
              <view v-else class="photo-fallback">{{ sellPhotoFailed ? '图片加载失败' : '暂无照片' }}</view>
            </view>
            <view class="name">{{ ykjinfo?.name }}</view>
            <view class="selection-hint">{{ ids?.length > 1 ? '展示首件已选藏品' : '已选藏品' }}</view>
          </view>
          <view class="right">
            <view>手续费：{{ ykjRate }}%</view>
            <view class="stock" v-if="ids" style="padding-top: 40rpx; text-align: center"
              >已选{{ ids.length }}件</view
            >
            <uni-number-box
              v-model="price"
              placeholder="一口价"
              :step="0.01"
              :min="0"
              :max="10000000"
            />
            <view class="batch-btn" @tap="yikoujiaConfirm">确定</view>
          </view>
        </view>
      </view>
    </su-popup>
    <uni-popup ref="popup" type="bottom" @maskClick="onCloseModal">
      <scroll-view class="select-wrap" scroll-y="true" :scroll-top="scrollTop">
        <view class="select-item" v-for="item in categoryList" :key="item.id">
          <view class="name" @tap="onSelect(item)">{{ item.name }}</view>
        </view>
        <view class="select-item" v-if="showCustom">
          <view class="custom-input">
            <uni-easyinput v-model="customName" :styles="inputStyles" paddingLeft="20" />
          </view>
          <text class="cicon-check-round-o" @tap="saveCustom" />
        </view>
      </scroll-view>
      <view class="custom" @tap="addCustom">自定义</view>
    </uni-popup>
  </s-layout>
</template>

<script setup>
  import GetbackEntry from '@/sheep/components/s-getback-entry/s-getback-entry.vue';
  import sheep from '@/sheep';
  import { collectionCover } from '@/sheep/api/collection/pictures';
  import { computed, ref } from 'vue';
  const sellPhotoFailed = ref(false);
  import { onShow, onLoad } from '@dcloudio/uni-app';
  import SuCheckBox from '../components/su-check-box.vue';
  import transferApi from '@/sheep/api/collection/transfer';
  import sellApi from '@/sheep/api/collection/sell';
  import AuctionApi from '@/sheep/api/collection/auction';
  import { warehouseStatus, warehouseAction, isGetbackSelectable } from '@/sheep/helper/warehouse-state.mjs';

  const warehouseBusy = ref(null);
  function manageWarehouseItem(item) {
    const action = warehouseAction(item);
    if (!action || warehouseBusy.value !== null) return;
    if (action === '管理竞拍') return sheep.$router.go('/pages/auction/my');
    uni.showModal({
      title: action,
      content: '确认后藏品仍保留在仓库，可重新挂售、修改类型或申请取回。',
      success: async ({ confirm }) => {
        if (!confirm || warehouseBusy.value !== null) return;
        warehouseBusy.value = item.id;
        try {
          const response = item.saleType === 'auction' ? await AuctionApi.cancel(item.listingId) : await sellApi.delistYikoujia(item.listingId);
          if (response?.code !== 0) return;
          await getCollections();
          sheep.$helper.toast('操作成功，藏品已恢复在库');
        } catch {
          sheep.$helper.toast('操作未完成，请刷新后重试');
        } finally {
          warehouseBusy.value = null;
        }
      },
    });
  }

  import collectionApi from '@/sheep/api/collection/collection';
  import CollectionCategoryApi from '@/sheep/api/collection/category';

  const showCustom = ref(false);
  const customName = ref('自定义命名');

  const collectionList = ref([]);
  const currentCollection = ref({});
  const categoryId = ref('');
  const targetCollectionId = ref('');
  const showDeliverCode = ref(false);
  const deliverCode = ref('');
  const deliverId = ref(null);
  const type = ref(null);
  const exchangeStoneTip = ref(false);
  const isAllChecked = ref(false);
  const showSellActions = ref(false);

  const showYikoujia = ref(false);
  const showFast = ref(false);
  const price = ref('');
  const ids = ref(null);
  const ykjinfo = ref(null);

  const popup = ref();

  const selectedCount = computed(() => collectionList.value.filter((item) => item.checked).length);
  const currentIndex = computed(() =>
    collectionList.value.findIndex((item) => item.id === currentCollection.value.id),
  );
  const currentImage = computed(() => {
    const pictures = currentCollection.value?.picUrls;
    return pictures?.length ? pictures[0] : currentCollection.value?.picUrl;
  });
  const currentCreateTime = computed(() =>
    currentCollection.value?.createTime
      ? sheep.$helper.timeFormat(currentCollection.value.createTime, 'yyyy.mm.dd')
      : '--',
  );
  const currentStatus = computed(() => warehouseStatus(currentCollection.value));
  const currentCategory = computed(
    () =>
      currentCollection.value?.categoryName || (Number(type.value) === 2 ? '自定义' : '普通藏品'),
  );

  const inputStyles = {
    borderColor: '#07f5f5',
    color: '#fff',
  };

  function onClsoeYkj() {
    showYikoujia.value = false;
    price.value = '';
    ids.value = null;
    ykjinfo.value = null;
  }

  function checkAll(val) {
    if (collectionList.value.every((e) => !isGetbackSelectable(e))) {
      sheep.$helper.toast('没有可勾选的藏品');
      return;
    }
    isAllChecked.value = val;
    collectionList.value.forEach((e) => {
      if (isGetbackSelectable(e)) {
        e.checked = val;
      }
    });
  }


  function switchCurrent(step) {
    if (collectionList.value.length < 2) return;
    const index = currentIndex.value < 0 ? 0 : currentIndex.value;
    const nextIndex = (index + step + collectionList.value.length) % collectionList.value.length;
    currentCollection.value = collectionList.value[nextIndex];
  }

  function previewCurrent() {
    const pictures = currentCollection.value?.picUrls?.length
      ? currentCollection.value.picUrls
      : [currentCollection.value?.picUrl].filter(Boolean);
    if (!pictures.length) return;
    uni.previewImage({ urls: pictures, current: pictures[0] });
  }

  function openSellActions() {
    if (!getCheckedIds()) return;
    showSellActions.value = true;
  }

  function runSellAction(action) {
    showSellActions.value = false;
    action();
  }

  function singleCheck(val, item) {
    item.checked = val;
    const selectable = collectionList.value.filter(isGetbackSelectable);
    isAllChecked.value =
      selectable.length > 0 && selectable.every((collection) => collection.checked);
  }

  function getCheckedIds(options = {}) {
    let checkedList = collectionList.value.filter((e) => e.checked);
    if (checkedList.length === 0) {
      sheep.$helper.toast('请先勾选藏品');
      return false;
    }
    if (!options.allowListed && checkedList.some((item) => item.tradeStatus !== 0)) {
      sheep.$helper.toast('请先取消上架或下架，再进行此操作');
      return false;
    }
    return {
      name: checkedList[0].name,
      picUrl: collectionCover(checkedList[0]),
      ids: JSON.stringify(checkedList.map((i) => i.id)),
      hasListed: checkedList.some((item) => item.tradeStatus === 2),
    };
  }

  function goTransfer() {
    let info = getCheckedIds();
    if (!info) return;
    sheep.$router.go(
      `/pages/collection/transfer?ids=${info.ids}&name=${encodeURIComponent(
        info.name,
      )}&pic=${encodeURIComponent(info.picUrl)}`,
    );
  }

  function goGetBack() {
    let info = getCheckedIds({ allowListed: true });
    if (!info) return;
    sheep.$router.go(
      `/pages/collection/withdraw?ids=${info.ids}&name=${encodeURIComponent(
        info.name,
      )}&pic=${encodeURIComponent(info.picUrl)}&autoDelist=${info.hasListed ? 1 : 0}`,
    );
  }

  function exchangeStone() {
    let info = getCheckedIds();
    if (!info) return;
    ids.value = JSON.parse(info.ids);
    exchangeStoneTip.value = true;
  }

  function exchangeStoneConfirm() {
    collectionApi
      .exchangeStone({
        collectionIds: ids.value,
      })
      .then((res) => {
        if (res.code === 0) {
          exchangeStoneTip.value = false;
          getCollections();
          ids.value = null;
        }
      });
  }

  function fastTrade() {
    let info = getCheckedIds();
    if (!info) return;
    showFast.value = true;
    ids.value = JSON.parse(info.ids);
  }

  function fastTradeConfirm() {
    sellApi
      .createFastTrade({
        collectionIds: ids.value,
      })
      .then((res) => {
        showFast.value = false;
        getCollections();
        ids.value = null;
      });
  }

  function yikoujia() {
    let info = getCheckedIds();
    if (!info) return;
    sellPhotoFailed.value = false;
    showYikoujia.value = true;
    ids.value = JSON.parse(info.ids);
    ykjinfo.value = info;
  }

  function yikoujiaConfirm() {
    if (!price.value) {
      return sheep.$helper.toast('请填写价格');
    }
    sellApi
      .createYikoujia({
        collectionIds: ids.value,
        price: price.value * 100,
      })
      .then((res) => {
        onClsoeYkj();
        getCollections();
      });
  }

  function getDeliver(collectionId) {
    transferApi.getDeliver({ collectionId }).then((res) => {
      deliverCode.value = res.data.deliverCode;
      deliverId.value = res.data.id;
      showDeliverCode.value = true;
    });
  }

  function confirmDeilver() {
    transferApi
      .updateDeliver({
        id: deliverId.value,
        status: 2,
      })
      .then((res) => {
        if (res.code === 0) {
          showDeliverCode.value = false;
          getCollections();
        }
      });
  }

  const ykjRate = ref(0);
  const fastRate = ref(0);
  const name = ref('');

  const categoryList = ref([]);

  function getCategoryList() {
    CollectionCategoryApi.getMyCollectionCategory().then((res) => {
      categoryList.value = res.data.filter((i) => i.userId !== 0 && !i.copyId);
    });
  }

  function editCurrentCategory() {
    const collection = currentCollection.value;
    if (!collection?.id) return sheep.$helper.toast('当前藏品信息不存在');
    if (collection.getbackStatus !== 0 || collection.tradeStatus !== 0) {
      return sheep.$helper.toast('当前状态暂不支持编辑类型');
    }
    ids.value = [collection.id];
    popup.value.open();
  }

  function onSelect(item) {
    CollectionCategoryApi.changeCategory({ categoryId: item.id, collectionIds: ids.value }).then(
      (res) => {
        if (res.code === 0) {
          getCollections();
          ids.value = null;
          popup.value.close();
        }
      },
    );
  }

  const scrollTop = ref(0);

  function onCloseModal() {
    customName.value = '自定义命名';
    showCustom.value = false;
  }

  function addCustom() {
    showCustom.value = true;
    goBottom();
  }

  function saveCustom() {
    if (!customName.value) {
      sheep.$helper.toast('分类名称不能为空');
      return;
    }
    CollectionCategoryApi.createCategory({ name: customName.value }).then((res) => {
      if (res.code === 0) {
        showCustom.value = false;
        categoryList.value.push({
          id: res.data,
          name: customName.value,
        });
        customName.value = '自定义命名';
      }
    });
  }

  function goBottom() {
    scrollTop.value = scrollTop.value + 1000;
  }

  onLoad((options) => {
    categoryId.value = options.id;
    targetCollectionId.value = options.collectionId || '';
    type.value = options.type;
    name.value = decodeURIComponent(options.name || '');
    collectionApi.getConfigs().then((res) => {
      ykjRate.value = res.data.list[2].value;
      fastRate.value = res.data.list[1].value;
    });
    getCategoryList();
  });

  function getCollections() {
    return collectionApi.getCollection({ categoryId: categoryId.value, status: 1 }).then((res) => {
      if (res?.code !== 0 || !Array.isArray(res.data)) return;
      isAllChecked.value = false;
      collectionList.value = res.data
        .filter(item => !targetCollectionId.value || String(item.id) === String(targetCollectionId.value))
        .filter(item => !name.value || item.name?.includes(name.value))
        .map(item => ({ ...item, checked: false }));
      if (collectionList.value.length > 0) {
        currentCollection.value = collectionList.value[0];
      } else {
        currentCollection.value = {};
        sheep.$router.back();
      }
    });
  }

  onShow((options) => {
    getCollections();
    isAllChecked.value = false;
  });

  function clickItem(item) {
    if (currentCollection.value.id === item.id) {
      uni.previewImage({
        urls: item.picUrls ? item.picUrls : [item.picUrl],
      });
    } else {
      currentCollection.value = item;
    }
  }

  function createAuction() {
    const checked = collectionList.value.filter((item) => item.checked);
    if (checked.length !== 1) return sheep.$helper.toast('请只选择一件藏品上架竞价');
    if (!isAuctionEligible(checked[0])) {
      return sheep.$helper.toast('仅自定义类型或签名商品可以上架竞价');
    }
    sheep.$router.go(`/pages/auction/create?collectionId=${checked[0].id}`);
  }

  function isAuctionEligible(item) {
    if (Number(type.value) === 2) return true;
    return [item?.name, item?.categoryName].some((text) => String(text || '').includes('签名'));
  }

  const canShowAuction = computed(
    () => Number(type.value) === 2 || collectionList.value.some((item) => isAuctionEligible(item)),
  );
</script>

<style lang="scss" scoped>
  .warehouse-status { font-size: 24rpx; color: #6d4b9c; margin: 8rpx 0; }
  .warehouse-action { margin: 8rpx 0 0; padding: 0 8rpx; min-height: 88rpx; line-height: 88rpx; font-size: 24rpx; background: #f2efff; color: #60439a; border-radius: 12rpx; }
  .warehouse-action[disabled] { opacity: .5; }
  .photo-fallback { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; font-size: 24rpx; color: var(--ys-text-secondary); }
  .selection-hint { margin-top: 8rpx; text-align: center; font-size: 24rpx; color: var(--ys-text-secondary); }
  .wrap {
    height: 100%;
    padding: 30rpx 20rpx 190rpx;
    box-sizing: border-box;
    background: var(--ys-page-bg);
    color: var(--ys-text);
  }

  .top {
    display: flex;
    gap: 24rpx;
    padding: 30rpx;
    background: var(--ys-surface);
    border: 1rpx solid var(--ys-border);
    border-radius: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
    .left {
      width: 300rpx;
      height: 400rpx;
      flex-shrink: 0;
      box-sizing: border-box;
      padding: 12rpx;
      background: #f3f4f6;
      border-radius: 18rpx;
      overflow: hidden;
    }
    .right {
      box-sizing: border-box;
      min-width: 0;
      flex: 1;
      min-height: 400rpx;
      padding: 20rpx 0;
      display: flex;
      flex-direction: column;
      .name {
        margin-bottom: 16rpx;
        color: var(--ys-text);
        font-size: 26rpx;
        line-height: 1.5;
        &.red {
          color: #dc2626;
        }
        &.blue {
          color: var(--ys-brand-color);
        }
        &.green {
          color: #15803d;
        }
      }
      .bottom {
        margin-top: auto;
        display: flex;
        align-items: flex-end;
        color: var(--ys-text-secondary);
        .uid-wrap {
          .uid-title {
            font-weight: bold;
            color: var(--ys-text);
          }
        }
        .copy-btn {
          width: 28rpx;
          height: 34rpx;
          margin-left: 24rpx;
          filter: grayscale(1) brightness(0.25);
        }
      }
    }
  }

  .cover {
    width: 100%;
    height: 100%;
    border-radius: 12rpx;
  }

  .list-name {
    color: var(--ys-text);
    font-weight: bold;
    font-size: 32rpx;
  }

  .list {
    height: calc(100% - 560rpx);
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    padding-bottom: 140rpx;
    box-sizing: border-box;
    .card {
      width: 216rpx;
      height: 250rpx;
      background: var(--ys-surface);
      border: 1rpx solid var(--ys-border);
      border-radius: 18rpx;
      box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.05);
      padding: 14rpx;
      box-sizing: border-box;
      position: relative;
      .single-check-box {
        position: absolute;
        right: 6rpx;
        top: 6rpx;
      }
    }
  }

  .btn-wrap {
    display: flex;
    position: fixed;
    bottom: 120rpx;
    justify-content: center;
    left: 0;
    width: 100%;
    gap: 24rpx;
    .btn {
      min-width: 190rpx;
      height: 64rpx;
      padding: 0 20rpx;
      line-height: 64rpx;
      border-radius: 18rpx;
      border: 1rpx solid var(--ys-brand-color);
      color: #ffffff;
      text-align: center;
      font-weight: bold;
      background-color: var(--ys-brand-color);
      box-shadow: 0 8rpx 18rpx rgba(24, 24, 27, 0.12);
    }
  }

  .deliver-btn {
    width: 160rpx;
    height: 48rpx;
    line-height: 48rpx;
    border-radius: 14rpx;
    color: #ffffff;
    text-align: center;
    font-weight: bold;
    background-color: var(--ys-brand-color);
    font-size: 22rpx;
    margin: 16rpx auto 0;
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
    .btn {
      border-radius: 18rpx;
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
  }

  :deep(.uni-popup__wrapper) {
    background-color: transparent !important;
  }

  .tip {
    text-align: center;
    color: var(--ys-text);
    font-weight: bold;
    line-height: 1.5;
  }

  .bottom-btn-wrap {
    display: flex;
    position: fixed;
    bottom: 0;
    width: 100%;
    left: 0;
    padding: 16rpx 30rpx 24rpx;
    box-sizing: border-box;
    background-color: var(--ys-surface);
    border-top: 1rpx solid var(--ys-border);
    .bottom-btn {
      flex: 1;
      text-align: center;
      height: 80rpx;
      line-height: 80rpx;
      font-weight: bold;
      color: #ffffff;
      font-size: 30rpx;
      background: var(--ys-brand-color);
      border-radius: 18rpx;
      &.left-btn {
        border-right: 1rpx solid var(--ys-border);
      }
    }
  }

  .list-header {
    padding: 30rpx 20rpx 20rpx 20rpx;
    display: flex;
    align-items: center;
    .check-all {
      margin-left: auto;
      margin-right: 10rpx;
    }
    .check-all-text {
      color: var(--ys-text-secondary);
      font-size: 28rpx;
      line-height: 30rpx;
    }
  }

  .check-wrap {
    padding: 8rpx;
    background-color: rgba(255, 255, 255, 0.92);
    border-radius: 12rpx;
  }

  .transfer-box {
    display: flex;
    width: 100%;
    padding-top: 20rpx;
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
        height: 56rpx;
        line-height: 56rpx;
        text-align: center;
        font-weight: bold;
        font-size: 26rpx;
        margin-top: 20rpx;
        border-radius: 14rpx;
      }
    }
  }
  :deep(.uni-popup__wrapper) {
    background-color: transparent !important;
  }

  .select-wrap {
    background-color: var(--ys-surface);
    border-radius: 30rpx 30rpx 0 0;
    padding: 20rpx 0 30rpx;
    max-height: 50vh;
  }

  .select-item {
    display: flex;
    align-items: center;
    margin-top: 30rpx;
    .name {
      width: 650rpx;
      height: 60rpx;
      line-height: 60rpx;
      text-align: center;
      border: 1rpx solid var(--ys-border);
      border-radius: 30rpx;
      margin: 0 50rpx;
      color: var(--ys-text);
      background: #f8f9fb;
    }
  }

  .custom {
    height: 100rpx;
    border-top: 1rpx solid var(--ys-border);
    color: var(--ys-text);
    line-height: 100rpx;
    text-align: center;
    font-size: 32rpx;
    background-color: var(--ys-surface);
  }

  .cicon-check-round-o {
    color: var(--ys-brand-color);
    font-size: 60rpx;
  }

  .custom-input {
    width: 550rpx;
    height: 60rpx;
    margin: 0 30rpx 0 50rpx;
  }

  :deep(.is-input-border) {
    border-radius: 18rpx;
    border-width: 1rpx;
  }
  :deep(.uni-easyinput) {
    border-radius: 18rpx;
    background: var(--ys-surface);
  }

  .wrap {
    min-height: 100%;
    height: auto;
    padding: 10rpx 24rpx 150rpx;
    background: #f2efff;
    color: #11153f;
  }

  .warehouse-intro {
    color: #8b60ed;
    font-size: 22rpx;
    line-height: 34rpx;
    text-align: center;
    letter-spacing: 1rpx;

    &::before,
    &::after {
      content: '✦';
      margin: 0 10rpx;
      font-size: 16rpx;
    }
  }

  .showcase {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 42rpx;
    min-height: 390rpx;
    padding: 16rpx 0 10rpx;
  }

  .showcase-arrow-button {
    width: 88rpx;
    height: 88rpx;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 120ms ease-out;
  }

  .showcase-arrow-button--pressed {
    transform: scale(0.9);
  }

  .showcase-arrow {
    width: 64rpx;
    height: 64rpx;
  }

  .showcase-photo {
    position: relative;
    width: 280rpx;
    height: 389rpx;
    padding: 18rpx;
    box-sizing: border-box;
    overflow: hidden;
    border-radius: 16rpx;
    box-shadow: 0 12rpx 28rpx rgba(105, 70, 186, 0.16);
  }

  .showcase-photo-bg {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
  }

  .showcase-photo-frame {
    position: absolute;
    z-index: 1;
    top: 20rpx;
    right: 16rpx;
    bottom: 20rpx;
    left: 16rpx;
    width: calc(100% - 32rpx);
    height: calc(100% - 40rpx);
  }

  .showcase-image {
    position: absolute;
    z-index: 2;
    top: 24rpx;
    right: 20rpx;
    bottom: 24rpx;
    left: 20rpx;
    width: calc(100% - 40rpx);
    height: calc(100% - 48rpx);
    border-radius: 10rpx;
  }

  .showcase-dots {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8rpx;
    min-height: 20rpx;
    margin-bottom: 12rpx;
  }

  .showcase-dot {
    width: 8rpx;
    height: 8rpx;
    border-radius: 50%;
    background: #d2cbe6;

    &.active {
      width: 22rpx;
      border-radius: 8rpx;
      background: #8257e8;
    }
  }

  .collection-summary {
    padding: 20rpx 30rpx 22rpx;
    background: #ffffff;
    border-radius: 16rpx;
    box-shadow: 0 8rpx 22rpx rgba(49, 29, 97, 0.07);
  }

  .summary-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20rpx;
  }

  .summary-title {
    min-width: 0;
    overflow: hidden;
    color: #12163f;
    font-size: 32rpx;
    font-weight: 700;
    line-height: 44rpx;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .summary-id {
    display: flex;
    align-items: center;
    flex-shrink: 0;
    color: #8b8a9c;
    font-size: 18rpx;
  }

  .copy-btn {
    width: 22rpx;
    height: 26rpx;
    margin-left: 8rpx;
    opacity: 0.56;
    filter: grayscale(1) brightness(0.55);
  }

  .summary-divider {
    height: 1rpx;
    margin: 12rpx 0 14rpx;
    background: #d8d5df;
  }

  .summary-meta {
    display: grid;
    grid-template-columns: minmax(0, 0.6fr) minmax(0, 1.25fr) minmax(0, 0.7fr) minmax(0, 1.65fr);
    align-items: start;
    column-gap: 16rpx;
  }

  .meta-item {
    min-width: 0;
    display: flex;
    flex-direction: column;
  }

  .meta-label {
    margin-bottom: 8rpx;
    color: #6f6d7d;
    font-size: 18rpx;
    line-height: 26rpx;
  }

  .meta-value {
    height: 40rpx;
    overflow: hidden;
    color: #15183d;
    font-size: 20rpx;
    font-weight: 600;
    line-height: 40rpx;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .status-value {
    color: #6840cf;
  }

  .category-item {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }

  .category-value-row {
    width: 100%;
    height: 40rpx;
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  .category-badge {
    min-width: 0;
    max-width: 140rpx;
    height: 36rpx;
    padding: 0 12rpx;
    box-sizing: border-box;
    overflow: hidden;
    color: #ffffff;
    font-size: 18rpx;
    line-height: 36rpx;
    text-overflow: ellipsis;
    white-space: nowrap;
    background: linear-gradient(90deg, #9d6df5, #754ee0);
    border-radius: 18rpx;
  }

  .category-edit-button {
    height: 40rpx;
    flex-shrink: 0;
    padding: 0 10rpx;
    box-sizing: border-box;
    color: #6840cf;
    font-size: 16rpx;
    font-weight: 600;
    line-height: 38rpx;
    white-space: nowrap;
    background: #f5f0ff;
    border: 1rpx solid #9b78eb;
    border-radius: 19rpx;
  }

  .category-edit-button--pressed {
    background: #e8ddff;
  }

  .inventory-panel {
    min-height: 520rpx;
    margin-top: 22rpx;
    padding: 0 20rpx 28rpx;
    background: #ffffff;
    border-radius: 16rpx 16rpx 0 0;
    box-shadow: 0 8rpx 24rpx rgba(49, 29, 97, 0.06);
  }

  .list-header {
    min-height: 82rpx;
    padding: 0 2rpx;
  }

  .list-name {
    color: #171a45;
    font-size: 27rpx;
    font-weight: 700;
  }

  .list-count {
    margin-left: 4rpx;
    color: #754ee0;
    font-size: 17rpx;
    font-weight: 500;
  }

  .check-all-control {
    display: flex;
    align-items: center;
    margin-left: auto;
  }

  .list-header .check-all {
    margin: 0 8rpx 0 0;
    transform: scale(0.76);
    transform-origin: center;
  }

  .list-header .check-all-text {
    color: #171a45;
    font-size: 20rpx;
    line-height: 30rpx;
  }

  .list {
    display: grid;
    grid-template-columns: repeat(3, 200rpx);
    justify-content: start;
    align-content: start;
    gap: 22rpx 20rpx;
    height: auto;
    padding: 0;
    overflow: visible;
  }

  .list .card {
    width: 200rpx;
    height: auto;
    min-width: 0;
    min-height: 246rpx;
    aspect-ratio: auto;
    padding: 8rpx 8rpx 14rpx;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background: #ffffff;
    border: 2rpx solid rgba(132, 82, 235, 0.28);
    border-radius: 14rpx;
    box-shadow: 0 8rpx 18rpx rgba(91, 55, 174, 0.08);

    &.current {
      border-color: #8b5ceb;
      box-shadow: 0 10rpx 24rpx rgba(110, 67, 213, 0.16);
    }
  }

  .thumb-wrap {
    position: relative;
    width: 100%;
    height: 184rpx;
    min-height: 184rpx;
    flex: none;
    overflow: hidden;
    background: linear-gradient(180deg, #f2ecff, #e7ddff);
    border-radius: 10rpx;
  }

  .cover {
    width: 100%;
    height: 100%;
    border-radius: 0;
  }

  .list .card .single-check-box {
    position: absolute;
    top: 0;
    right: 0;
    z-index: 2;
    width: 76rpx;
    height: 76rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .check-wrap {
    padding: 0;
    background: transparent;
    border-radius: 0;
  }

  .card-name {
    margin: 10rpx 6rpx 0;
    overflow: hidden;
    color: #15183d;
    font-size: 20rpx;
    line-height: 30rpx;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .deliver-btn {
    width: calc(100% - 12rpx);
    height: 42rpx;
    margin: 10rpx 6rpx 0;
    color: #ffffff;
    font-size: 18rpx;
    line-height: 42rpx;
    background: #8055e5;
    border-radius: 12rpx;
  }

  .selection-dock {
    position: fixed;
    z-index: 30;
    right: 0;
    bottom: 0;
    left: 0;
    display: flex;
    align-items: center;
    min-height: 108rpx;
    padding: 14rpx 22rpx calc(14rpx + env(safe-area-inset-bottom));
    box-sizing: border-box;
    color: #ffffff;
    background: #11183f;
  }

  .selected-copy {
    flex: 1;
    color: rgba(255, 255, 255, 0.92);
    font-size: 20rpx;
    white-space: nowrap;

    text {
      color: #a985ff;
      font-size: 24rpx;
      font-weight: 700;
    }
  }

  .dock-actions {
    display: flex;
    gap: 18rpx;
  }

  .dock-btn {
    width: 190rpx;
    height: 64rpx;
    color: #ffffff;
    font-size: 25rpx;
    font-weight: 700;
    line-height: 64rpx;
    text-align: center;
    background: #8155e6;
    border-radius: 32rpx;
  }

  .dock-btn-primary {
    box-shadow: 0 8rpx 18rpx rgba(129, 85, 230, 0.28);
  }

  .action-box {
    padding: 36rpx 30rpx 30rpx;
  }

  .action-title {
    color: #171a45;
    font-size: 32rpx;
    font-weight: 700;
    text-align: center;
  }

  .action-subtitle {
    margin: 10rpx 0 26rpx;
    color: #77758a;
    font-size: 22rpx;
    line-height: 32rpx;
    text-align: center;
  }

  .action-list {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
  }

  .action-item {
    min-height: 92rpx;
    padding: 16rpx 22rpx;
    box-sizing: border-box;
    color: #ffffff;
    text-align: center;
    background: #8155e6;
    border-radius: 14rpx;

    &.secondary {
      color: #6e45d3;
      background: #eee7ff;
    }
  }

  .action-item-title {
    font-size: 26rpx;
    font-weight: 700;
    line-height: 34rpx;
  }

  .action-item-desc {
    margin-top: 4rpx;
    color: rgba(255, 255, 255, 0.78);
    font-size: 19rpx;
    font-weight: 400;
    line-height: 28rpx;
  }

  .action-item.secondary .action-item-desc {
    color: #8a76bc;
  }
</style>
