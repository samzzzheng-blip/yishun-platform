<!-- 底部导航栏 -->
<template>
  <view class="u-tabbar">
    <view
      class="u-tabbar__content"
      ref="u-tabbar__content"
      @touchmove.stop.prevent=""
      :class="[border && 'u-border-top']"
      :style="[tabbarStyle]"
    >
      <view class="u-tabbar__content__item-wrapper">
        <slot></slot>
      </view>
      <view v-if="safeAreaInsetBottom" :style="[{ height: safeBottomHeight + 'px' }]"></view>
    </view>
  </view>
</template>

<script>
  // #ifdef APP-NVUE
  const dom = uni.requireNativePlugin('dom');
  // #endif
  /**
   * Tabbar 底部导航栏
   * @description 此组件提供了自定义tabbar的能力。
   * @property {String | Number}	value				当前匹配项的name
   * @property {Boolean}			safeAreaInsetBottom	是否为iPhoneX留出底部安全距离（默认 true ）
   * @property {Boolean}			border				是否显示上方边框（默认 true ）
   * @property {String | Number}	zIndex				元素层级z-index（默认 1 ）
   * @property {String}			activeColor			选中标签的颜色（默认 '#1989fa' ）
   * @property {String}			inactiveColor		未选中标签的颜色（默认 '#7d7e80' ）
   * @property {Object}			customStyle			定义需要用到的外部样式
   *
   */

  import { deepMerge, addStyle, sleep } from '@/sheep/helper';
  import sheep from '@/sheep';

  export default {
    name: 'su-tabbar',
    props: {
      customStyle: {
        type: [Object, String],
        default: () => ({}),
      },
      customClass: {
        type: String,
        default: '',
      },
      // 跳转的页面路径
      url: {
        type: String,
        default: '',
      },
      // 页面跳转的类型
      linkType: {
        type: String,
        default: 'navigateTo',
      },
      // 当前匹配项的name
      value: {
        type: [String, Number, null],
        default: '',
      },
      // 是否为iPhoneX留出底部安全距离
      safeAreaInsetBottom: {
        type: Boolean,
        default: true,
      },
      // 是否显示上方边框
      border: {
        type: Boolean,
        default: true,
      },
      // 元素层级z-index
      zIndex: {
        type: [String, Number],
        default: 10,
      },
      // 选中标签的颜色
      activeColor: {
        type: String,
        default: '#1989fa',
      },
      // 未选中标签的颜色
      inactiveColor: {
        type: String,
        default: '#7d7e80',
      },
    },
    data() {
      return {
        safeBottomHeight: sheep.$platform.device.safeAreaInsets.bottom,
      };
    },
    computed: {
      tabbarStyle() {
        const style = {
          zIndex: this.zIndex,
        };
        // 合并来自父组件的customStyle样式
        return deepMerge(style, addStyle(this.customStyle));
      },
      // 监听多个参数的变化，通过在computed执行对应的操作
      updateChild() {
        return [this.value, this.activeColor, this.inactiveColor];
      },
    },
    watch: {
      updateChild() {
        // 如果updateChildren中的元素发生了变化，则执行子元素初始化操作
        this.updateChildren();
      },
    },
    created() {
      this.children = [];
    },
    methods: {
      updateChildren() {
        // 如果存在子元素，则执行子元素的updateFromParent进行更新数据
        this.children.length && this.children.map((child) => child.updateFromParent());
      },
      getRect(selector, all) {
        return new Promise((resolve) => {
          uni.createSelectorQuery()
            .in(this)
            [all ? 'selectAll' : 'select'](selector)
            .boundingClientRect((rect) => {
              if (all && Array.isArray(rect) && rect.length) {
                resolve(rect);
              }
              if (!all && rect) {
                resolve(rect);
              }
            })
            .exec();
        });
      },
    },
  };
</script>

<style lang="scss" scoped>
  .u-tabbar {
    display: flex;
    flex: 1;
    justify-content: center;

    &__content {
      width: 100%;
      display: flex;
      flex-direction: column;
      background-color: #fff;
      box-shadow: 0px -2px 4px 0px rgba(51, 51, 51, 0.06);

      &__item-wrapper {
        height: 55px;
        display: flex;
        justify-content: space-around;
        align-items: center;
      }
    }

  }
</style>
