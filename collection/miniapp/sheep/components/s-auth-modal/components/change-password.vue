<!-- 修改密码（登录时）  -->
<template>
  <view>
    <!-- 标题栏 -->
    <view class="head-box ss-m-b-60">
      <view class="head-title ss-m-b-20">修改密码</view>
    </view>

    <!-- 表单项 -->
    <uni-forms
      ref="changePasswordRef"
      v-model="state.model"
      :rules="state.rules"
      validateTrigger="bind"
      labelAlign="center"
    >
      <uni-forms-item name="code">
        <MyEasyInput
          placeholder="请输入原密码"
          v-model="state.model.oldPassword"
		  :styles="inputStyles"
		  paddingLeft="10"
        >
        </MyEasyInput>
      </uni-forms-item>

      <uni-forms-item name="reNewPassword">
        <MyEasyInput
          type="password"
          placeholder="请输入新密码"
          v-model="state.model.password"
          :styles="inputStyles"
          paddingLeft="10"
        >
        </MyEasyInput>
      </uni-forms-item>
	  <button class="ss-reset-button login-btn-start" @tap="changePasswordSubmit">
	    确认
	  </button>
    </uni-forms>

    <button class="ss-reset-button type-btn" @tap="closeAuthModal">
      取消修改
    </button>
  </view>
</template>

<script setup>
  import { ref, reactive, unref } from 'vue';
  import { code, password } from '@/sheep/validate/form';
  import { closeAuthModal, getSmsCode, getSmsTimer } from '@/sheep/hooks/useModal';
  import UserApi from '@/sheep/api/member/user';
  import MyEasyInput from './uni-easyinput.vue'

  const changePasswordRef = ref(null);
  const inputStyles = {
    borderColor: '#07f5f5',
    color: '#fff',
  }

  // 数据
  const state = reactive({
    model: {
      oldPassword: '', // 旧密码
      password: '', // 新密码
    },
    rules: {
      oldPassword: password,
      password,
    },
  });

  // 更改密码
  async function changePasswordSubmit() {
    // 参数校验
    const validate = await unref(changePasswordRef)
      .validate()
      .catch((error) => {
        console.log('error: ', error);
      });
    if (!validate) {
      return;
    }
    // 发起请求
    const { code } = await UserApi.updateUserPassword(state.model);
    if (code !== 0) {
      return;
    }
    // 成功后，只需要关闭弹窗
    closeAuthModal();
  }
</script>

<style lang="scss" scoped>
  @import '../index.scss';
</style>
