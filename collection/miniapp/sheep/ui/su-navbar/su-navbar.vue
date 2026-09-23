<!-- 自定义导航栏 -->
<template>
  <view class="uni-navbar" :class="{ 'uni-dark': dark }" :style="fixed ? { height: reservedHeight } : {}">
    <view
      :class="{
        'uni-navbar--shadow': shadow,
        'uni-navbar--border': border,
        'uni-navbar__content--fixed': fixed,
      }"
      class="uni-navbar__content"
    >
      <view class="fixed-bg" :class="[opacity ? '' : opacityBgUi]"></view>
      <su-status-bar v-if="statusBar" />
      <view
        :style="{
          color: themeColor,
          height: navbarHeight,
          background: backgroundColor,
        }"
        class="uni-navbar__header"
      >
        <view
          class="uni-navbar__header-btns uni-navbar__header-btns-left"
          :style="{ width: leftIconWidth }"
        >
          <slot name="left">
            <view class="uni-navbar__content_view" v-if="leftIcon.length > 0">
              <view class="icon-box ss-flex">
                <view class="icon-button icon-button-left ss-flex ss-row-center" @tap="onClickLeft">
                  <text class="sicon-back" v-if="hasHistory" />
                  <!-- <text class="sicon-home" v-else /> -->
                </view>
                <!-- <view class="line"></view>
                <view
                  class="icon-button icon-button-right ss-flex ss-row-center"
                  @tap="showMenuTools"
                >
                  <text class="sicon-more" />
                </view> -->
              </view>
            </view>
            <view
              :class="{ 'uni-navbar-btn-icon-left': !leftIcon.length > 0 }"
              class="uni-navbar-btn-text"
              v-if="
                titleAlign === 'left' &&
                title.length &&
                sheep.$platform.name !== 'WechatOfficialAccount'
              "
            >
              <text :style="{ color: themeColor, fontSize: '18px' }">{{ title }}</text>
            </view>
          </slot>
        </view>
        <view v-if="tools === 'search'" class="ss-flex-1">
          <slot name="center">
            <uni-search-bar
              class="ss-flex-1 search-box"
              :radius="20"
              placeholder="请输入关键词"
              cancelButton="none"
              v-model="searchModel"
              @confirm="onSearch"
            />
          </slot>
        </view>
        <view v-else class="uni-navbar__header-container" @tap="onClickTitle">
          <slot name="center">
            <view
              v-if="tools === 'title' && titleAlign === 'center' && title.length"
              class="uni-navbar__header-container-inner"
            >
              <text :style="{ color: themeColor, fontSize: '36rpx' }" class="ss-line-1">{{
                title
              }}</text>
            </view>
          </slot>
        </view>
      </view>
    </view>
    <!-- 头部问题 -->
    <!-- #ifdef MP -->
    <!-- <view :style="[capsuleStyle]"></view> -->
    <!-- #endif -->
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { onLoad } from '@dcloudio/uni-app';
  import { showMenuTools, closeMenuTools } from '@/sheep/hooks/useModal';
  import { computed, ref } from 'vue';

  /**
   * NavBar 自定义导航栏
   * @description 导航栏组件，主要用于头部导航
   * @property {Boolean} dark 开启黑暗模式
   * @property {String} title 标题文字
   * @property {String} rightText 右侧按钮文本
   * @property {String} leftIcon 左侧按钮图标
   * @property {String} rightIcon 右侧按钮图标
   * @property {String} color 图标和文字颜色
   * @property {String} backgroundColor 导航栏背景颜色
   * @property {Boolean} statusBar = [true|false] 是否包含状态栏
   * @property {Boolean} shadow = [true|false] 导航栏下是否有阴影
   * @event {Function} clickLeft 左侧按钮点击时触发
   * @event {Function} clickRight 右侧按钮点击时触发
   * @event {Function} clickTitle 中间标题点击时触发
   */

  const getVal = (val) => (typeof val === 'number' ? val + 'px' : val);

  const emits = defineEmits(['clickLeft', 'clickRight', 'clickTitle', 'search']);
  const props = defineProps({
    fixed: {
      type: Boolean,
      default: false,
    },
    dark: {
      type: Boolean,
      default: false,
    },
    modelValue: {
      type: String,
      default: '',
    },
    title: {
      type: String,
      default: '',
    },
    titleAlign: {
      type: String,
      default: 'center', // left | center
    },
    rightText: {
      type: String,
      default: '',
    },
    leftIcon: {
      type: String,
      default: 'left',
    },
    rightIcon: {
      type: String,
      default: '',
    },
    color: {
      type: String,
      default: '',
    },
    backgroundColor: {
      type: String,
      default: '',
    },
    opacity: {
      type: [Boolean, String],
      default: false,
    },
    opacityBgUi: {
      type: String,
      default: 'bg-white',
    },
    statusBar: {
      type: [Boolean, String],
      default: false,
    },
    shadow: {
      type: [Boolean, String],
      default: false,
    },
    border: {
      type: [Boolean, String],
      default: false,
    },
    height: {
      type: [Number, String],
      default: 44,
    },
    leftWidth: {
      type: [Number, String],
      default: 40,
    },
    rightWidth: {
      type: [Number, String],
      default: 0,
    },
    tools: {
      type: String,
      default: 'title',
    },
    defaultSearch: {
      type: String,
      default: '',
    },
  });

  const capsuleStyle = computed(() => {
    return {
      width: sheep.$platform.capsule.width + 'px',
      height: sheep.$platform.capsule.height + 'px',
      margin: '0 ' + (sheep.$platform.device.windowWidth - sheep.$platform.capsule.right) + 'px',
    };
  });

  const searchModel = computed(() => {
		return props.defaultSearch
	})

  const themeBgColor = computed(() => {
    if (props.dark) {
      // 默认值
      if (props.backgroundColor) {
        return props.backgroundColor;
      } else {
        return props.dark ? '#333' : '#FFF';
      }
    }
    return props.backgroundColor || '#FFF';
  });
  const themeColor = computed(() => {
    if (props.dark) {
      // 默认值
      if (props.color) {
        return props.color;
      } else {
        return props.dark ? '#fff' : '#333';
      }
    }
    return props.color || '#333';
  });
  const navbarHeight = computed(() => {
    return getVal(props.height);
  });
  // 与实际状态栏和标题栏使用相同高度；占位不参与 flex 收缩。
  const reservedHeight = computed(() => props.statusBar
    ? `calc(${navbarHeight.value} + ${sheep.$platform.device.statusBarHeight || 0}px)`
    : navbarHeight.value);
  const leftIconWidth = computed(() => {
    return getVal(props.leftWidth);
  });
  const rightIconWidth = computed(() => {
    return getVal(props.rightWidth);
  });

  function onSearch(e) {
    emits('search', e.value);
  }

  onLoad(() => {
    if (uni.report && props.title !== '') {
      uni.report('title', props.title);
    }
  });

  const hasHistory = sheep.$router.hasHistory();

  function onClickLeft() {
    if (hasHistory) {
      sheep.$router.back();
    } 
    // else {
    //   sheep.$router.go('/pages/index/index');
    // }
    emits('clickLeft');
  }
  function onClickRight() {
    showMenuTools();
  }
  function onClickTitle() {
    emits('clickTitle');
  }
</script>

<style lang="scss" scoped>
  .uni-navbar { flex-shrink: 0; }
  .uni-navbar__content.uni-navbar__content--fixed {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 980;
  }
  .bg-main {
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient)) !important;
    color: #fff !important;
  }
  .icon-box {
    
    margin-left: 8rpx;
    .line {
      width: 2rpx;
      height: 24rpx;
      background: #e5e5e7;
    }
    .sicon-back {
      font-size: 50rpx;
      color: #000;
    }
    .sicon-home {
      font-size: 32rpx;
      color: #000;
    }
    .sicon-more {
      font-size: 32rpx;
      color: #000;
    }
    .icon-button {
      width: 67rpx;
      height: 56rpx;
    }
  }

  $nav-height: 44px;

  .fixed-bg {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    z-index: 1;
    pointer-events: none;
  }

  .uni-nav-bar-text {
    /* #ifdef APP-PLUS */
    font-size: 34rpx;
    /* #endif */
    /* #ifndef APP-PLUS */
    font-size: 14px;
    /* #endif */
  }

  .uni-nav-bar-right-text {
    font-size: 12px;
  }

  .uni-navbar__content {
    position: relative;
    // background-color: #fff;
    // box-sizing: border-box;
    background-color: transparent;
  }

  .uni-navbar__content_view {
    // box-sizing: border-box;
  }

  .uni-navbar-btn-text {
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    flex-direction: column;
    justify-content: flex-start;
    align-items: center;
    line-height: 18px;
  }

  .uni-navbar__header {
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    padding: 0 10px;
    flex-direction: row;
    justify-content: space-between;
    height: $nav-height;
    font-size: 12px;
    position: relative;
    z-index: 2;
  }

  .uni-navbar__header-btns {
    /* #ifndef APP-NVUE */
    overflow: hidden;
    display: flex;
    /* #endif */
    flex-wrap: nowrap;
    flex-direction: row;
    // min-width: 120rpx;
    min-width: 40rpx;
    // padding: 0 6px;
    justify-content: center;
    align-items: center;
    /* #ifdef H5 */
    cursor: pointer;
    /* #endif */
  }

  .uni-navbar__header-btns-left {
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    width: 120rpx;
    justify-content: flex-start;
    align-items: center;
  }

  .uni-navbar__header-btns-right {
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    flex-direction: row;
    // width: 150rpx;
    // padding-right: 30rpx;
    justify-content: flex-end;
    align-items: center;
  }
  .uni-navbar__header-container {
    /* #ifndef APP-NVUE */
    // display: flex;
    /* #endif */
    // flex: 1;
    // padding: 0 10px;
    // overflow: hidden;
    position: absolute;
    left: 50%;
    transform: translateX(-50%) translateY(-50%);
    top: 50%;
  }

  .uni-navbar__header-container-inner {
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    flex: 1;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    overflow: hidden;
    // box-sizing: border-box;
  }


  .uni-navbar--shadow {
    box-shadow: 0 1px 6px #ccc;
  }

  .uni-navbar--border {
    border-bottom-width: 1rpx;
    border-bottom-style: solid;
    border-bottom-color: #eee;
  }

  .uni-ellipsis-1 {
    overflow: hidden;
    /* #ifndef APP-NVUE */
    white-space: nowrap;
    text-overflow: ellipsis;
    /* #endif */
    /* #ifdef APP-NVUE */
    lines: 1;
    text-overflow: ellipsis;
    /* #endif */
  }

  // 暗主题配置
  .uni-dark {
  }
</style>
