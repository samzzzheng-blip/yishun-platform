<template>
  <s-layout
    title="资产登记"
    navbar="normal"
    :dark="false"
    :bgStyle="{ color: 'var(--ys-page-bg)' }"
  >
    <view class="wrap">
      <view class="add-box">
        <view class="section-heading">
          <view class="section-title">藏品信息</view>
          <view class="section-hint">请填写真实信息，便于后续审核</view>
        </view>
        <view class="photo-heading">
          <view class="field-label">
            <image class="field-icon" src="/static/ui-icons/add-photo.png" mode="aspectFit"></image>
            <text>藏品照片</text>
          </view>
          <text class="photo-count">最多 9 张</text>
        </view>
        <s-uploader
          v-model:url="photo"
          fileMediatype="image"
          limit="9"
          mode="grid"
          :imageStyles="{ width: '168rpx', height: '168rpx' }"
        >
          <view class="registration-add-photo" aria-label="添加藏品照片" role="button"
            hover-class="registration-add-photo-pressed"></view>
        </s-uploader>
        <uni-easyinput
          v-model="name"
          :styles="inputStyles1"
          paddingLeft="10"
        >
          <template v-slot:left>
            <view class="field-label input-label">
              <image class="field-icon" src="/static/ui-icons/field-name.png" mode="aspectFit"></image>
              <text>品名</text>
            </view>
          </template>
        </uni-easyinput>
        <uni-easyinput
          v-model="amount"
          :styles="inputStyles1"
          paddingLeft="10"
          type="number"
        >
          <template v-slot:left>
            <view class="field-label input-label">
              <image class="field-icon" src="/static/ui-icons/field-quantity.png" mode="aspectFit"></image>
              <text>数量</text>
            </view>
          </template>
        </uni-easyinput>
        <view class="type-select" @tap="showPopup">
          <view class="field-label input-label">
            <image class="field-icon" src="/static/ui-icons/field-category.png" mode="aspectFit"></image>
            <text>类别</text>
          </view>
          <view class="type">{{categoryName}}</view>
          <view class="arrow"></view>
        </view>
        <view class="tip">个别类别无法进行批量交易</view>
        <view class="btn" @tap="saveCollection">确认</view>
      </view>
    </view>
    <uni-popup ref="popup" type="bottom" @maskClick="onCloseModal">
      <scroll-view class="select-wrap" scroll-y="true" :scroll-top="scrollTop">
        <view class="category-grid">
          <button class="category-option" v-for="item in categoryList" :key="item.id"
            :class="{ 'category-option--selected': categoryId === item.id }"
            hover-class="category-option--pressed" @tap="onSelect(item)">{{ item.name }}</button>
        <view class="select-item" v-if="showCustom">
          <view class="custom-input">
            <uni-easyinput
              v-model="customName"
              :styles="inputStyles"
              paddingLeft="20"
            />
          </view>
          <text class="cicon-check-round-o" @tap="saveCustom" />
        </view>
        </view>
      </scroll-view>
      <view class="custom" @tap="addCustom">自定义</view>
    </uni-popup>
    <view class="pay-popup-mask" v-if="showPayPopup" @tap="closePayPopup">
      <view class="pay-popup" @tap.stop>
        <view class="pay-header">
          <view class="level-info">当前寄存等级 {{ storageId }} <text v-if="expireTime">有效期至 {{sheep.$helper.timeFormat(expireTime, 'yyyy-mm-dd')}}</text></view>
          <view class="rule-link" @tap="goStorageRule">寄存规则</view>
        </view>
        <view class="pay-card-list">
          <view 
            class="pay-card" 
            :class="{ active: payType === 'month' }"
            @tap="selectPayType('month')"
          >
            <view class="card-title">1个月</view>
            <view class="card-price">{{ monthlyPrice }}</view>
            <view class="card-unit">能量石</view>
          </view>
          <view 
            class="pay-card" 
            :class="{ active: payType === 'year' }"
            @tap="selectPayType('year')"
          >
            <view class="card-title">1年</view>
            <view class="card-price">{{ yearlyPrice }}</view>
            <view class="card-unit">能量石</view>
          </view>
        </view>
        <view class="pay-btn" @tap="onPayClick">立即支付</view>
      </view>
    </view>
    <!-- 购买须知弹窗 -->
    <view class="notice-mask" v-if="showNoticePopup" @tap="closeNoticePopup">
      <view class="notice-popup" @tap.stop>
        <view class="notice-title">购买须知</view>
        <scroll-view
          class="notice-content"
          scroll-y
        >
          <view class="notice-text" v-html="noticeContent"></view>
          <view class="notice-btn" @tap="onNoticeConfirm">我已知晓</view>
        </scroll-view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
import { ref } from 'vue'
import { sortCategoriesByUsage } from './category-usage.mjs';
import CollectionCategoryApi from '@/sheep/api/collection/category';
import CollectionApi from '@/sheep/api/collection/collection';
import ParcelApi from '@/sheep/api/collection/parcel';
async function afterRegistration() {
  try {
    const r = await ParcelApi.config();
    if (r?.code === 0 && r.data.enabled) {
      uni.showModal({
        title: '藏品登记成功',
        content: '可以邮寄或线下送达，支持多件一起登记。线下送达无需单号，可查看公司地址和接收时间。是否现在登记？',
        confirmText: '交付登记',
        cancelText: '继续上传',
        success: result => {
          if (result.confirm) sheep.$router.go('/pages/collection/parcel');
          else sheep.$router.back();
        }
      });
      return;
    }
  } catch {}
  sheep.$helper.toast('登记成功');
  sheep.$router.back();
}
import StoragePlanApi from '@/sheep/api/collection/storage-plan';
import ArticleApi from '@/sheep/api/promotion/article';
import sheep from '@/sheep';
import { onLoad } from '@dcloudio/uni-app';
import {
    chooseAndUploadFile,
  } from '@/sheep/components/s-uploader/choose-and-upload-file';

const inputStyles = {
  borderColor: '#e5e7eb',
  color: '#18181b',
}

const inputStyles1 = {
  borderColor: '#e5e7eb',
  color: '#18181b',
}

const popup = ref()

function showPopup() {
  popup.value.open()
}

const photo = ref([])
const name = ref('')
const amount = ref('')
const categoryName = ref('')
const categoryId = ref('')
const showCustom = ref(false)
const customName = ref('自定义命名')

const showPhoto = ref(false)

const id = ref(null)

const showPayPopup = ref(false)
const payType = ref('month')
const storagePlanId = ref(0)
const storageId = ref(0)
const monthlyPrice = ref(0)
const yearlyPrice = ref(0)
const pendingCollectionData = ref(null)
const expireTime = ref(null)

const showNoticePopup = ref(false)
const noticeContent = ref('')


const categoryList = ref([])

function onCloseModal() {
  customName.value = '自定义命名'
  showCustom.value = false
}

function onClosePhoto() {
  showPhoto.value= false
}

function addCustom() {
  showCustom.value = true
  goBottom()
}

function getCategoryList () {
  CollectionCategoryApi.getCollectionCategory().then(res => {
    const list1 = sortCategoriesByUsage(res.data.filter(i => i.userId == 0 && i.name !== '直购物品' && !/^直购[0-9]+$/.test(i.name || '')))
	CollectionCategoryApi.getMyCollectionCategory().then(res1 => {
	  const list2 = res1.data.filter(i => i.userId !== 0 && !i.copyId && i.name !== '直购物品' && !/^直购[0-9]+$/.test(i.name || ''))
	  categoryList.value = [...list1, ...list2]
	})
  })
}

function onSelect(item) {
  categoryName.value = item.name
  categoryId.value = item.id
  popup.value.close()
}

function copyCategory(item) {
  let newCategory = {
    name: item.name + (categoryList.value.length - 2),
	copyId: item.id,
	picUrl: item.picUrl
  }
  CollectionCategoryApi.createCategory(newCategory).then(res => {
    getCategoryList()
    goBottom()
  })
}

const scrollTop = ref(0)

function goBottom() {
  scrollTop.value = scrollTop.value + 1000
}



function saveCustom() {
  if (!customName.value) {
    sheep.$helper.toast('分类名称不能为空')
    return
  }
  CollectionCategoryApi.createCategory({name: customName.value}).then(res => {
    if (res.code === 0) {
      showCustom.value = false
      categoryList.value.push({
      		  id: res.data,
      		  name: customName.value
      })
      customName.value = '自定义命名'
    }
  })
}

function saveCollection() {
  if (photo.value.length === 0) {
    sheep.$helper.toast('请上传照片')
    return
  }
  if (!name.value) {
    sheep.$helper.toast('请输入品名')
    return
  }
  if (!amount.value) {
    sheep.$helper.toast('请输入数量')
    return
  }
  if (!categoryId.value) {
    sheep.$helper.toast('请选择分类')
    return
  }
  const data = {
    picUrls: photo.value,
    categoryId: categoryId.value,
    categoryName: categoryName.value,
    name: name.value,
    stock: amount.value
  }
  if (id.value !== null) {
    data.id = id.value
    data.status = 0
    update(data)
    return
  }
  CollectionApi.createCollection(data).then(res => {
    if (res.code === 0) {
      if (res.data.needUpgrade) {
        pendingCollectionData.value = data
        storagePlanId.value = res.data.storagePlanId
        storageId.value = res.data.storageId
		expireTime.value = res.data.expireTime
        monthlyPrice.value = res.data.monthlyPrice
        yearlyPrice.value = res.data.yearlyPrice
        showPayPopup.value = true
      } else {
        afterRegistration()
      }
    }
  })
}

function selectPayType(type) {
  payType.value = type
}

function closePayPopup() {
  showPayPopup.value = false
}

function goStorageRule() {
  sheep.$router.go('/pages/collection/storage-rule')
}

async function onPayClick() {
  showNoticePopup.value = true
  const { data } = await ArticleApi.getArticle(null, '购买须知')
  noticeContent.value = data ? data.content : ''
}

function closeNoticePopup() {
  showNoticePopup.value = false
}

function onNoticeConfirm() {
  showNoticePopup.value = false
  confirmPay()
}

async function confirmPay() {
  const type = payType.value === 'month' ? 1 : 2
  const { code } = await StoragePlanApi.purchaseStoragePlan({
    planId: storagePlanId.value,
    type: type,
  })
  if (code === 0) {
    showPayPopup.value = false
    if (pendingCollectionData.value) {
      CollectionApi.createCollection(pendingCollectionData.value).then(res => {
        if (res.code === 0 && !res.data.needUpgrade) {
          afterRegistration()
        }
      })
    } else {
      sheep.$router.back()
    }
  }
}

function deleteCategory(id) {
  CollectionCategoryApi.deleteCategory({id}).then(res => {
    getCategoryList()
  })
}

function update(data) {
  CollectionApi.updateCollection(data).then(res => {
    if (res.code === 0) {
      sheep.$router.back()
    }
  })
}


onLoad((options) => {
  if (options.id) {
    id.value = options.id
    CollectionApi.getCollectionById({id: id.value}).then(res => {
      photo.value = res.data.picUrls ? res.data.picUrls : [res.data.picUrl]
      name.value = res.data.name
      amount.value = res.data.stock
      categoryName.value = res.data.categoryName
      categoryId.value = res.data.categoryId
    })
  }
})

getCategoryList()
</script>

<style lang="scss" scoped>
.wrap {
  background: var(--ys-page-bg);
  min-height: 100%;
  box-sizing: border-box;
  padding: 48rpx 30rpx 80rpx;
}

:deep(.is-input-border) {
  border-radius: 18rpx;
  border-width: 1rpx;
}
:deep(.uni-easyinput) {
  border-radius: 18rpx;
}
:deep(.uni-easyinput__content) {
  color: var(--ys-text);
  background-color: var(--ys-surface);
}

.add-box {
  padding: 36rpx 30rpx 50rpx;
  color: var(--ys-text);
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);

  :deep(.uni-file-picker__container) {
    margin-bottom: 28rpx;
  }

  :deep(.file-picker__box-content) {
    border-radius: 16rpx;
  }

  :deep(.file-picker__box-content.is-add) {
    background: #f8f9fb;
    border: 1rpx dashed #cbd5e1 !important;
  }

  :deep(.icon-add) {
    background-color: #9ca3af;
  }

  :deep(.uni-easyinput) {
    margin: 20rpx 0;
    background-color: var(--ys-surface);
  }
  .type {
    margin-left: 20rpx;
    color: var(--ys-text);
  }

  .type-select {
    border-radius: 18rpx;
    border: 1rpx solid var(--ys-border);
    background-color: var(--ys-surface);
    height: 72rpx;
    display: flex;
    align-items: center;
    .arrow {
      width: 12rpx;
      height: 12rpx;
      border-right: 2rpx solid #9ca3af;
      border-bottom: 2rpx solid #9ca3af;
      transform: rotate(45deg);
      margin-left: auto;
      margin: 0 24rpx 8rpx auto;
    }
  }
  .tip {
    text-align: right;
    color: var(--ys-text-secondary);
    font-size: 24rpx;
    margin-top: 12rpx;
  }
  .btn {
    border-radius: 18rpx;
    border: 1rpx solid var(--ys-brand-color);
    background-color: var(--ys-brand-color);
    color: #ffffff;
    height: 84rpx;
    width: 100%;
    margin: 72rpx auto 0;
    line-height: 84rpx;
    text-align: center;
    font-weight: bold;
    font-size: 30rpx;
    box-shadow: 0 10rpx 24rpx rgba(24, 24, 27, 0.14);
  }
}

.section-heading {
  margin-bottom: 28rpx;
}

.section-title {
  color: var(--ys-text);
  font-size: 34rpx;
  font-weight: 700;
}

.section-hint {
  margin-top: 8rpx;
  color: var(--ys-text-secondary);
  font-size: 22rpx;
}

.registration-add-photo {
  position: relative;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  margin: 0;
  flex-shrink: 0;
  background: #fff;
  border: 2rpx solid #8254df;
  border-radius: 16rpx;
}
.registration-add-photo::before,
.registration-add-photo::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 84rpx;
  height: 8rpx;
  border-radius: 4rpx;
  background: #8254df;
  transform: translate(-50%, -50%);
}
.registration-add-photo::after {
  transform: translate(-50%, -50%) rotate(90deg);
}
.registration-add-photo-pressed {
  opacity: 0.7;
}
.photo-heading {
  min-height: 56rpx;
  margin-bottom: 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.photo-count {
  color: var(--ys-text-secondary);
  font-size: 22rpx;
}

.field-label {
  display: flex;
  align-items: center;
  gap: 12rpx;
  color: var(--ys-text);
  font-size: 26rpx;
  font-weight: 600;
}

.input-label {
  min-width: 132rpx;
  padding-left: 18rpx;
  box-sizing: border-box;
}

.field-icon {
  width: 34rpx;
  height: 34rpx;
  flex: 0 0 auto;
}

.add-photo {
  width: 100rpx;
  height: 100rpx;
  position: relative;
  background: var(--ys-surface-muted);
  border: 1rpx dashed var(--ys-border-strong);
  border-radius: 16rpx;

  &::before,
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 34rpx;
    height: 4rpx;
    background: var(--ys-text-secondary);
    border-radius: 4rpx;
    transform: translate(-50%, -50%);
  }

  &::after {
    transform: translate(-50%, -50%) rotate(90deg);
  }
}

.photo {
  width: 100rpx;
  height: 100rpx;
}

.photo-preview {
  width: 520rpx;
  height: 560rpx;
}

.select-wrap {
  background-color: var(--ys-surface);
  border-radius: 30rpx 30rpx 0 0;
  padding: 20rpx 0 30rpx;
  max-height: 50vh;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
  padding: 12rpx 28rpx;
}
.category-option {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-width: 0;
  min-height: 96rpx;
  margin: 0;
  padding: 16rpx 10rpx;
  box-sizing: border-box;
  border: 1rpx solid var(--ys-border);
  border-radius: 16rpx;
  color: var(--ys-text);
  background: var(--ys-surface);
  font-size: 28rpx;
  line-height: 1.5;
  white-space: normal;
  word-break: break-all;
}
.category-option::after { border: 0; }
.category-option--selected { border: 3rpx solid var(--ys-brand-color); font-weight: 600; }
.category-option--pressed { opacity: .65; }
.category-grid > .select-item { grid-column: 1 / -1; margin-top: 0; min-width: 0; }
.category-grid .custom-input { flex: 1; width: 0; min-width: 0; margin: 0 16rpx 0 0; }

.custom-input {
  width: 550rpx;
  height: 60rpx;
  margin: 0 30rpx 0 50rpx;
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
  .copy-btn {
    width: 60rpx;
    height: 60rpx;
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

.remove-btn {
  font-size: 60rpx;
  color: var(--ys-text-secondary);
}

.pay-popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.pay-popup {
  width: 100%;
  background: var(--ys-surface);
  border: 1rpx solid var(--ys-border);
  border-radius: 30rpx 30rpx 0 0;
  padding: 40rpx 30rpx 60rpx;
  box-sizing: border-box;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.pay-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.level-info {
  color: var(--ys-text);
  font-size: 28rpx;
  font-weight: bold;
}

.rule-link {
  color: var(--ys-brand-color);
  font-size: 26rpx;
}

.pay-card-list {
  display: flex;
  justify-content: space-between;
  gap: 30rpx;
  margin-bottom: 60rpx;
}

.pay-card {
  flex: 1;
  height: 240rpx;
  border: 1rpx solid var(--ys-border);
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8f9fb;
  position: relative;
  transition: all 0.3s;
}

.pay-card.active {
  border-color: var(--ys-brand-color);
  background: var(--ys-surface);
  box-shadow: 0 8rpx 22rpx rgba(24, 24, 27, 0.12);
}

.card-title {
  color: var(--ys-text);
  font-size: 26rpx;
  margin-bottom: 20rpx;
}

.card-price {
  color: var(--ys-text);
  font-size: 56rpx;
  font-weight: bold;
  font-family: OPPOSANS;
}

.card-unit {
  color: var(--ys-text-secondary);
  font-size: 24rpx;
  margin-top: 10rpx;
}

.pay-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  border: 1rpx solid var(--ys-brand-color);
  border-radius: 18rpx;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: bold;
  background: var(--ys-brand-color);
  box-shadow: 0 10rpx 24rpx rgba(24, 24, 27, 0.14);
}

.notice-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice-popup {
  width: 600rpx;
  background: var(--ys-surface);
  border-radius: 24rpx;
  border: 1rpx solid var(--ys-border);
  box-shadow: 0 18rpx 48rpx rgba(24, 24, 27, 0.18);
  overflow: hidden;
}

.notice-title {
  text-align: center;
  color: var(--ys-text);
  font-size: 34rpx;
  font-weight: bold;
  padding: 40rpx 0 20rpx;
}

.notice-content {
  max-height: 600rpx;
  padding: 0 40rpx;
  box-sizing: border-box;
}

.notice-text {
  padding-bottom: 20rpx;
  color: var(--ys-text-secondary);
  font-size: 26rpx;
  line-height: 1.8;
}

.notice-section-title {
  color: var(--ys-text);
  font-size: 28rpx;
  font-weight: bold;
  margin: 30rpx 0 15rpx;
}

.notice-section-body {
  color: var(--ys-text-secondary);
  font-size: 26rpx;
  line-height: 1.8;
  margin-bottom: 10rpx;
}

.notice-btn {
  height: 100rpx;
  line-height: 100rpx;
  text-align: center;
  color: var(--ys-text);
  font-size: 32rpx;
  font-weight: bold;
  border-top: 1rpx solid var(--ys-border);
  margin-top: 20rpx;
}
</style>
