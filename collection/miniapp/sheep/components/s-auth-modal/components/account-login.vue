<!-- 账号密码登录 accountLogin  -->
<template>
  <view class="account-login">
    <!-- 标题栏 -->
    <view class="head-box ss-m-b-60 ss-flex-col">
      <view class="ss-flex ss-m-b-20">
        <view class="head-title" @tap="showAuthModal('smsLogin')">
          短信登录
        </view>
        <view class="head-title head-title-active ss-m-l-40 head-title-animation">账号登录</view>
      </view>
    </view>

    <!-- 表单项 -->
    <uni-forms
      ref="accountLoginRef"
      v-model="state.model"
      :rules="state.rules"
      validateTrigger="bind"
      labelWidth="0"
      labelAlign="center"
    >
      <uni-forms-item name="mobile">
        <MyEasyInput placeholder="请输入账号" v-model="state.model.mobile" :styles="inputStyles">
          <!-- <template v-slot:right>
            <button class="ss-reset-button forgot-btn" @tap="showAuthModal('resetPassword')">
              忘记密码
            </button>
          </template> -->
        </MyEasyInput>
      </uni-forms-item>

      <uni-forms-item name="password">
        <MyEasyInput
          type="password"
          placeholder="请输入密码"
          v-model="state.model.password"
          :styles="inputStyles"
        >
          
        </MyEasyInput>
      </uni-forms-item>
      <button class="ss-reset-button login-btn-start" @tap="accountLoginSubmit">登录</button>
    </uni-forms>
  </view>
</template>

<script setup>
  import { ref, reactive, unref } from 'vue';
  import sheep from '@/sheep';
  import { account, password } from '@/sheep/validate/form';
  import { showAuthModal, closeAuthModal } from '@/sheep/hooks/useModal';
  import AuthUtil from '@/sheep/api/member/auth';
  import MyEasyInput from './uni-easyinput.vue'

  const accountLoginRef = ref(null);

  const inputStyles = {
    borderColor: '#e5e7eb',
    color: '#18181b',
    backgroundColor: '#ffffff',
  };

  const emits = defineEmits(['onConfirm']);

  const props = defineProps({
    agreeStatus: {
      type: [Boolean, null],
      default: null,
    },
  });

  // 数据
  const state = reactive({
    model: {
      mobile: '', // 账号
      password: '', // 密码
    },
    rules: {
      mobile: account,
      password,
    },
  });

  // 账号登录
  async function accountLoginSubmit() {
    // 表单验证
    const validate = await unref(accountLoginRef)
      .validate()
      .catch((error) => {
        console.log('error: ', error);
      });
    if (!validate) return;

    // 检查协议状态
    if (props.agreeStatus !== true) {
      emits('onConfirm', true);
      if (props.agreeStatus === false) {
        sheep.$helper.toast('您已拒绝协议，无法继续登录');
      } else {
        sheep.$helper.toast('请选择是否同意协议');
      }
      return;
    }

    // 提交数据
    const { code, data } = await AuthUtil.login(state.model);
    if (code === 0) {
      closeAuthModal();
    }
  }
</script>

<style lang="scss" scoped>
  @import '../index.scss';
</style>
