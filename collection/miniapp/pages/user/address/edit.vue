<!-- 收货地址的新增/编辑 -->
<template>
  <s-layout
    :title="state.model.id ? '编辑地址' : '新增地址'"
    :dark="false"
    :bgStyle="{ color: 'var(--ys-page-bg)' }"
  >
    <view class="wrap">
      <uni-forms
        ref="addressFormRef"
        v-model="state.model"
        :rules="rules"
        validateTrigger="bind"
        labelWidth="0"
      >
        <view class="form-box ss-p-x-30">
          <uni-forms-item name="name" class="form-item">
            <uni-easyinput
              v-model="state.model.name"
              placeholder="请填写收货人姓名"
              placeholderStyle="color:#BBBBBB;font-size:30rpx;font-weight:400;line-height:normal"
              :styles="inputStyles"
              paddingLeft="10"
            />
          </uni-forms-item>

          <uni-forms-item name="mobile" class="form-item">
            <uni-easyinput
              v-model="state.model.mobile"
              type="number"
              placeholder="请输入手机号"
              placeholderStyle="color:#BBBBBB;font-size:30rpx;font-weight:400;line-height:normal"
              :styles="inputStyles"
              paddingLeft="10"
            >
            </uni-easyinput>
          </uni-forms-item>
          <uni-forms-item
            name="areaName"
            @tap="state.showRegion = true"
            class="form-item"
          >
            <view class="area">
              <view class="label" :class="{hasValue: state.model.areaName }">{{state.model.areaName ? state.model.areaName : '请选择省市区'}}</view>
              <view class="arrow"></view>
            </view>
          </uni-forms-item>
          <uni-forms-item
            name="detailAddress"
            :formItemStyle="{ alignItems: 'flex-start' }"
            class="textarea-item"
          >
            <uni-easyinput
              type="textarea"
              v-model="state.model.detailAddress"
              placeholderStyle="color:#BBBBBB;font-size:30rpx;font-weight:400;line-height:normal"
              placeholder="请输入详细地址"
              clearable
              :styles="inputStyles"
              paddingLeft="10"
            />
          </uni-forms-item>
        </view>
        <view class="ss-p-x-30">
          <view class="is-default">
            <view class="label"> 设为默认地址 </view>
            <su-switch style="transform: scale(0.8)" v-model="state.model.defaultStatus" />
          </view>
        </view>
      </uni-forms>
      <view
        class="add-btn"
        @tap="onSave"
      >
        保存
      </view>

      <!-- 省市区弹窗 -->
      <su-region-picker
        :show="state.showRegion"
        @cancel="state.showRegion = false"
        @confirm="onRegionConfirm"
      />
    </view>
  </s-layout>
</template>

<script setup>
  import { ref, reactive, unref } from 'vue';
  import sheep from '@/sheep';
  import { onLoad } from '@dcloudio/uni-app';
  import _ from 'lodash-es';
  import { mobile } from '@/sheep/validate/form';
  import AreaApi from '@/sheep/api/system/area';
  import AddressApi from '@/sheep/api/member/address';

  const inputStyles = {
    borderColor: '#e5e7eb',
    color: '#18181b',
  }

  const addressFormRef = ref(null);
  const state = reactive({
    showRegion: false,
    model: {
      name: '',
      mobile: '',
      detailAddress: '',
      defaultStatus: false,
      areaName: '',
    },
    rules: {},
  });

  const rules = {
    name: {
      rules: [
        {
          required: true,
          errorMessage: '请输入收货人姓名',
        },
      ],
    },
    mobile,
    detailAddress: {
      rules: [
        {
          required: true,
          errorMessage: '请输入详细地址',
        },
      ],
    },
    areaName: {
      rules: [
        {
          required: true,
          errorMessage: '请选择您的位置',
        },
      ],
    },
  };

  // 确认选择地区
  const onRegionConfirm = (e) => {
    state.model.areaName = `${e.province_name} ${e.city_name} ${e.district_name}`;
    state.model.areaId = e.district_id;
    state.showRegion = false;
  };

  // 获得地区数据
  const getAreaData = () => {
    if (_.isEmpty(uni.getStorageSync('areaData'))) {
      AreaApi.getAreaTree().then((res) => {
        if (res.code === 0) {
          uni.setStorageSync('areaData', res.data);
        }
      });
    }
  };

  // 保存收货地址
  const onSave = async () => {
    // 参数校验
    const validate = await unref(addressFormRef)
      .validate()
      .catch((error) => {
        console.log('error: ', error);
      });
    if (!validate) {
      return;
    }

    // 提交请求
    const formData = {
      ...state.model,
    };
    const { code } =
      state.model.id > 0
        ? await AddressApi.updateAddress(formData)
        : await AddressApi.createAddress(formData);
    if (code === 0) {
      sheep.$router.back();
    }
  };

  // 删除收货地址
  const onDelete = () => {
    uni.showModal({
      title: '提示',
      content: '确认删除此收货地址吗？',
      success: async function (res) {
        if (!res.confirm) {
          return;
        }
        const { code } = await AddressApi.deleteAddress(state.model.id);
        if (code === 0) {
          sheep.$router.back();
        }
      },
    });
  };

  onLoad(async (options) => {
    // 获得地区数据
    getAreaData();
    // 情况一：基于 id 获得收件地址
    if (options.id) {
      let { code, data } = await AddressApi.getAddress(options.id);
      if (code !== 0) {
        return;
      }
      state.model = data;
    }
    // 情况二：微信导入
    if (options.data) {
      let data = JSON.parse(options.data);
      const areaData = uni.getStorageSync('areaData');
      const findAreaByName = (areas, name) => areas.find((item) => item.name === name);

      let provinceObj = findAreaByName(areaData, data.province_name);
      let cityObj = provinceObj ? findAreaByName(provinceObj.children, data.city_name) : undefined;
      let districtObj = cityObj ? findAreaByName(cityObj.children, data.district_name) : undefined;
      let areaId = (districtObj || cityObj || provinceObj).id;

      state.model = {
        ...state.model,
        areaId,
        areaName: [data.province_name, data.city_name, data.district_name]
          .filter(Boolean)
          .join(' '),
        defaultStatus: false,
        detailAddress: data.address,
        mobile: data.mobile,
        name: data.consignee,
      };
    }
  });
</script>

<style lang="scss" scoped>
  .wrap {
    background: var(--ys-page-bg);
    height: 100%;
    box-sizing: border-box;
    padding: 36rpx 30rpx 50rpx;
  }

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
  .form-box {
    padding-top: 30rpx;
    padding-bottom: 10rpx;
    background: var(--ys-surface);
    border: 1rpx solid var(--ys-border);
    border-radius: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(24, 24, 27, 0.06);
  }

  :deep(.uni-easyinput__content-textarea) {
    height: 50px;
    min-height: 50px;
  }
  .label {
    padding-left: 20rpx;
  }

  .area, .is-default{
    border-radius: 18rpx;
    border: 1rpx solid var(--ys-border);
    background-color: var(--ys-surface);
    height: 72rpx;
    display: flex;
    align-items: center;
    color:#BBBBBB;font-size:30rpx;font-weight:400;line-height:normal;
    .arrow {
      width: 12rpx;
      height: 12rpx;
      border-right: 2rpx solid #9ca3af;
      border-bottom: 2rpx solid #9ca3af;
      margin-left: auto;
      margin-right: 24rpx;
      transform: rotate(45deg);
    }
  }

  .is-default {
    margin-top: 20rpx;
    padding-right: 10rpx;
    box-shadow: 0 6rpx 18rpx rgba(24, 24, 27, 0.04);
    .label {
      color: var(--ys-text);
      margin-right: auto;
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

  .hasValue {
    color: var(--ys-text);
  }
</style>
