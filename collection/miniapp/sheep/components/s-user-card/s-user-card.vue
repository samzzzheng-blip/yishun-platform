<!-- 装修用户组件：用户卡片 -->
<template>
  <view class="ss-user-info-wrap ss-p-t-50" :style="[bgStyle, { marginLeft: `${data.space}px` }]">
    <view class="ss-flex ss-col-center ss-row-between ss-m-b-20">
      <view class="left-box ss-flex ss-col-center ss-m-l-36">
        <view class="avatar-box ss-m-r-24">
          <image class="avatar-img" :src="
              isLogin && userInfo.avatar
                ? sheep.$url.cdn(userInfo.avatar)
                : sheep.$url.static('/static/img/shop/default_avatar.png')"
                 mode="aspectFill" @tap="sheep.$router.go('/pages/user/info')">
          </image>
        </view>
        <view class="profile-content">
          <view class="nickname-box ss-flex ss-col-center">
            <view class="nick-name ss-m-r-20" @tap="sheep.$router.go('/pages/user/info')">{{ userInfo?.nickname || nickname }}</view>
            <view class="level-tag" v-if="isLogin" @tap="emits('clickLevel')">
              寄存等级 {{ storageCapacity.storageId || 0 }}
            </view>
            <view class="login-entry" v-else @tap="sheep.$router.go('/pages/user/info')">
              <text>登录后查看</text>
              <view class="login-arrow"></view>
            </view>
          </view>
          <view class="ss-flex ss-col-center" v-if="userInfo?.mobile">
            <view class="user-id">ID:{{ userInfo?.mobile }}</view>
            <image class="copy-btn" @tap="sheep.$helper.copyText(userInfo?.mobile)" :src="sheep.$url.static('/static/project/copy.png')"></image>
          </view>
		  <view class="stone-wrap" v-if="isLogin" role="button" aria-label="查看能量石明细" hover-class="stone-pressed" @tap="sheep.$router.go('/pages/user/wallet/stone')">
			  <view class="stone-mark"></view>
			  <view class="stone-label">能量石</view>
			  <view class="num">{{stoneAmount || 0}}</view>
              <text class="stone-detail">明细 ›</text>
		  </view>
          <view class="login-description" v-else>管理藏品、交易与寄存空间</view>
        </view>
      </view>
      
    </view>
  </view>
</template>

<script setup>
  /**
   * 用户卡片
   *
   * @property {Number} leftSpace                  - 容器左间距
   * @property {Number} rightSpace                  - 容器右间距
   *
   * @property {String} avatar          - 头像
   * @property {String} nickname          - 昵称
   * @property {String} vip              - 等级
   * @property {String} collectNum        - 收藏数
   * @property {String} likeNum          - 点赞数
   *
   *
   */
  import { computed } from 'vue';
  import sheep from '@/sheep';
  import {
    showShareModal,
    showAuthModal,
  } from '@/sheep/hooks/useModal';
  
  const emits = defineEmits(['clickLevel']);

  // 用户信息
  const userInfo = computed(() => sheep.$store('user').userInfo);

  // 是否登录
  const isLogin = computed(() => sheep.$store('user').isLogin);

  // 空间容量信息
  const storageCapacity = computed(() => sheep.$store('user').storageCapacity);


  // 接收参数
  const props = defineProps({
    // 装修数据
    data: {
      type: Object,
      default: () => ({}),
    },
    // 装修样式
    styles: {
      type: Object,
      default: () => ({}),
    },
    // 头像
    avatar: {
      type: String,
      default: '',
    },
    nickname: {
      type: String,
      default: '请先登录',
    },
    vip: {
      type: [String, Number],
      default: '1',
    },
    collectNum: {
      type: [String, Number],
      default: '1',
    },
    likeNum: {
      type: [String, Number],
      default: '1',
    },
	stoneAmount: {
		type: Number,
		default: 0,
	}
  });

  // 设置背景样式
  const bgStyle = computed(() => {
    // 直接从 props.styles 解构
    const { bgType, bgImg, bgColor } = props.styles;

    // 根据 bgType 返回相应的样式
    return {
      background: bgType === 'img'
        ? `url(${bgImg}) no-repeat top center / 100% 100%`
        : bgColor,
    };
  });

  // 绑定手机号
  function onBind() {
    showAuthModal('changeMobile');
  }
</script>

<style lang="scss" scoped>
  .ss-user-info-wrap {
    box-sizing: border-box;

    .profile-content {
      flex: 1;
      min-width: 0;
    }

    .avatar-box {
      width: 118rpx;
      height: 118rpx;
      border-radius: 50%;
      overflow: hidden;
      border: 6rpx solid #ffffff;
      box-shadow: 0 0 0 2rpx var(--ys-border-strong);

      .avatar-img {
        width: 100%;
        height: 100%;
      }
    }

    .nick-name {
      font-size: 34rpx;
      font-weight: 700;
      color: var(--ys-text);
      line-height: normal;
    }

    .level-tag {
      flex: 0 0 auto;
      font-size: 20rpx;
      color: #ffffff;
      background-color: var(--ys-brand-color);
      padding: 7rpx 14rpx;
      border-radius: 9rpx;
      line-height: normal;
    }

    .login-entry {
      display: flex;
      align-items: center;
      flex: 0 0 auto;
      padding: 8rpx 14rpx;
      color: var(--ys-text);
      font-size: 20rpx;
      font-weight: 600;
      background: var(--ys-surface-muted);
      border-radius: 9rpx;
    }

    .login-arrow {
      width: 9rpx;
      height: 9rpx;
      margin-left: 8rpx;
      border-top: 2rpx solid var(--ys-text-secondary);
      border-right: 2rpx solid var(--ys-text-secondary);
      transform: rotate(45deg);
    }

    .vip-img {
      width: 30rpx;
      height: 30rpx;
    }

    .sicon-qrcode {
      font-size: 40rpx;
    }
  }

  .bind-mobile-box {
    width: 100%;
    height: 84rpx;
    padding: 0 34rpx 0 44rpx;
    box-sizing: border-box;
    background: #ffffff;
    box-shadow: 0px -8rpx 9rpx 0px rgba(#e0e0e0, 0.3);

    .cicon-mobile-o {
      font-size: 30rpx;
      color: #ff690d;
    }

    .mobile-title {
      font-size: 24rpx;
      font-weight: 500;
      color: #ff690d;
    }

    .bind-btn {
      width: 100rpx;
      height: 50rpx;
      background: #ff6100;
      border-radius: 25rpx;
      font-size: 24rpx;
      font-weight: 500;
      color: #ffffff;
    }
  }

  .user-id {
    margin-top: 5px;
    font-size: 12px;
    height: 27rpx;
    line-height: 27rpx;
    margin-right: 5px;
  }

  .copy-btn {
    margin-top: 5px;
    width: 21rpx;
    height: 27rpx;
  }
  
  .stone-wrap {
    min-height: 48px;
    margin-top: 12rpx;
    padding: 0 16rpx;
    display: inline-flex;
    align-items: center;
    color: var(--ys-text);
    background: var(--ys-surface-muted);
    border-radius: 10rpx;
  }

  .stone-mark {
    width: 15rpx;
    height: 15rpx;
    margin-right: 10rpx;
    background: var(--ys-brand-color);
    transform: rotate(45deg);
    border-radius: 3rpx;
  }

  .stone-detail { margin-left: 16rpx; font-size: 24rpx; }
  .stone-pressed { opacity: 0.65; }

  .stone-label {
    color: var(--ys-text-secondary);
    font-size: 21rpx;
  }

  .num {
    margin-left: 14rpx;
    color: var(--ys-text);
    font-size: 23rpx;
    font-weight: 700;
  }

  .login-description {
    margin-top: 14rpx;
    color: var(--ys-text-secondary);
    font-size: 22rpx;
  }
</style>
