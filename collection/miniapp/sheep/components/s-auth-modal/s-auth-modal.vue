<template>
  <!-- 规格弹窗 -->
  <su-popup :show="authType !== ''" round="10" :showClose="true" @close="closeAuthModal">
    <view
      class="login-wrap"
      :class="{ 'login-wrap-light': ['', 'accountLogin', 'smsLogin'].includes(authType) }"
    >
      <!-- 1. 账号密码登录 accountLogin -->
      <account-login
        v-if="authType === 'accountLogin'"
        :agreeStatus="state.protocol"
        @onConfirm="onConfirm"
      />
      <!-- <view class="head">
        欢迎登录
      </view> -->

      <!-- 2. 短信登录  smsLogin -->
      <sms-login
        v-if="authType === 'smsLogin'"
        :agreeStatus="state.protocol"
        @onConfirm="onConfirm"
      />

      <!-- 3. 忘记密码 resetPassword-->
      <reset-password v-if="authType === 'resetPassword'" />

      <!-- 4. 绑定手机号 changeMobile -->
      <change-mobile v-if="authType === 'changeMobile'" />

      <!-- 5. 修改密码 changePassword-->
      <changePassword v-if="authType === 'changePassword'" />

      <!-- 6. 微信小程序授权 -->
      <mp-authorization v-if="authType === 'mpAuthorization'" />

      


      <!-- 用户协议的勾选 -->
      <view
        v-if="['accountLogin', 'smsLogin'].includes(authType)"
        class="agreement-box ss-flex ss-flex-col ss-col-center"
        :class="{ shake: currentProtocol }"
      >
        
        <view class="agreement-options-container">
          <!-- 同意选项 -->
          <view class="agreement-option ss-m-b-20">
            <label class="radio ss-flex ss-col-center" @tap="onAgree">
              <radio
                :checked="state.protocol === true"
                color="var(--ys-brand-color)"
                style="transform: scale(0.8)"
                @tap.stop="onAgree"
              />
              <view class="agreement-text ss-flex ss-col-center ss-m-l-8">
                我已阅读并同意遵守
                <view class="tcp-text" @tap.stop="onProtocol('用户协议')"> 《用户协议》 </view>
                <view class="agreement-text">与</view>
                <view class="tcp-text" @tap.stop="onProtocol('隐私政策')"> 《隐私政策》 </view>
              </view>
            </label>
          </view>
          
        </view>
      </view>
      <view class="safe-box" />
    </view>
  </su-popup>
</template>

<script setup>
  import { computed, reactive, ref } from 'vue';
  import sheep from '@/sheep';
  import smsLogin from './components/sms-login.vue';
  import resetPassword from './components/reset-password.vue';
  import changeMobile from './components/change-mobile.vue';
  import changePassword from './components/change-password.vue';
  import mpAuthorization from './components/mp-authorization.vue';
  import { closeAuthModal, showAuthModal } from '@/sheep/hooks/useModal';
  import accountLogin from './components/account-login.vue';

  const modalStore = sheep.$store('modal');
  // 授权弹窗类型
  const authType = computed(() => modalStore.auth);

  const state = reactive({
    protocol: null, // null表示未选择，true表示同意，false表示拒绝
  });

  const currentProtocol = ref(false);

  // 同意协议
  function onAgree() {
    state.protocol = !state.protocol;
  }
  

  // 查看协议
  function onProtocol(title) {
    closeAuthModal();
    sheep.$router.go('/pages/public/richtext', {
      title,
    });
  }

  // 点击登录 / 注册事件
  function onConfirm(e) {
    currentProtocol.value = e;
    setTimeout(() => {
      currentProtocol.value = false;
    }, 1000);
  }

  // 第三方授权登陆（微信小程序、Apple）
  const thirdLogin = async (provider) => {
    if (state.protocol !== true) {
      currentProtocol.value = true;
      setTimeout(() => {
        currentProtocol.value = false;
      }, 1000);
      
      if (state.protocol === false) {
        sheep.$helper.toast('您已拒绝协议，无法继续登录');
      } else {
        sheep.$helper.toast('请选择是否同意协议');
      }
      return;
    }
    const loginRes = await sheep.$platform.useProvider(provider).login();
    if (loginRes) {
      const userInfo = await sheep.$store('user').getInfo();
      closeAuthModal();
      // 如果用户已经有头像和昵称，不需要再次授权
      if (userInfo.avatar && userInfo.nickname) {
        return;
      }

      // 触发小程序授权信息弹框
      // #ifdef MP-WEIXIN
      showAuthModal('mpAuthorization');
      // #endif
    }
  };

  // 微信小程序的“手机号快速验证”：https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/getPhoneNumber.html
  const getPhoneNumber = async (e) => {
    if (e.detail.errMsg !== 'getPhoneNumber:ok') {
      sheep.$helper.toast('快捷登录失败');
      return;
    }
    let result = await sheep.$platform.useProvider().mobileLogin(e.detail);
    if (result) {
      closeAuthModal();
    }
  };
</script>

<style lang="scss" scoped>
  @import './index.scss';
  .login-wrap {
    height: 60vh;
    color: var(--ys-text);
    background: var(--ys-page-bg);
    .head {
      color: #fff;
      font-size: 50rpx;
      margin-bottom: 40rpx;
    }
  }

  .login-wrap-light {
    box-sizing: border-box;
    color: var(--ys-text);
    background: var(--ys-page-bg);

    .agreement-box {
      margin-top: 54rpx;
    }

    .agreement-text {
      color: var(--ys-text-secondary);
    }

    .agreement-box .agreement-text .tcp-text {
      color: #18181b !important;
      font-weight: 600;
    }
  }

  .shake {
    animation: shake 0.05s linear 4 alternate;
  }

  @keyframes shake {
    from {
      transform: translateX(-10rpx);
    }
    to {
      transform: translateX(10rpx);
    }
  }

  .register-box {
    position: relative;
    justify-content: center;
    .register-btn {
      color: #999999;
      font-size: 30rpx;
      font-weight: 500;
    }
    .register-title {
      color: #999999;
      font-size: 30rpx;
      font-weight: 400;
      margin-right: 24rpx;
    }
    .or-title {
      margin: 0 16rpx;
      color: #999999;
      font-size: 30rpx;
      font-weight: 400;
    }
    .login-btn {
      color: var(--ui-BG-Main);
      font-size: 30rpx;
      font-weight: 500;
    }
    .circle {
      position: absolute;
      right: 0rpx;
      top: 18rpx;
      width: 8rpx;
      height: 8rpx;
      border-radius: 8rpx;
      background: var(--ui-BG-Main);
    }
  }
  .safe-box {
    height: calc(constant(safe-area-inset-bottom) / 5 * 3);
    height: calc(env(safe-area-inset-bottom) / 5 * 3);
  }

  .tcp-text {
    color: var(--ui-BG-Main);
  }

  .agreement-text {
    color: $dark-9;
  }
  
  .agreement-title {
    font-size: 28rpx;
    color: $dark-9;
    text-align: left;
    width: 100%;
    padding-left: 60rpx;
  }
  
  .agreement-options-container {
    width: 100%;
    padding-left: 50rpx;
  }
  
  .agreement-option {
    width: 100%;
    display: flex;
    justify-content: flex-start;
  }
</style>
