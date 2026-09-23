<template>
  <view class="u-page__item" v-if="tabbar?.items?.length > 0">
    <su-tabbar
      :value="path"
      :fixed="true"
      :placeholder="true"
      :safeAreaInsetBottom="true"
      :inactiveColor="tabbar.style.color"
      :activeColor="tabbar.style.activeColor"
      :customStyle="tabbarStyle"
    >
      <su-tabbar-item
        v-for="(item, index) in tabbar.items"
        :key="item.text"
        :text="item.text"
        :name="item.url"
        :badge="item.badge"
        :dot="item.dot"
        :badgeStyle="tabbar.badgeStyle"
        :isCenter="getTabbarCenter(index)"
        :centerImage="sheep.$url.cdn(item.iconUrl)"
        :activeCenterImage="sheep.$url.cdn(item.activeIconUrl)"
        @tap="sheep.$router.go(item.url)"
      >
        <template v-slot:active-icon>
          <image class="u-page__item__slot-icon tabbar-icon--active" :src="sheep.$url.cdn(item.activeIconUrl)"></image>
        </template>
        <template v-slot:inactive-icon>
          <image class="u-page__item__slot-icon tabbar-icon--inactive" :src="sheep.$url.cdn(item.iconUrl)"></image>
        </template>
      </su-tabbar-item>
    </su-tabbar>
  </view>
</template>

<script setup>
  import { computed, unref } from 'vue';
  import sheep from '@/sheep';
  import SuTabbar from '@/sheep/ui/su-tabbar/su-tabbar.vue';

  const fallbackTabbar = {
    theme: 'ink',
    style: {
      bgType: 'color',
      bgColor: '#ffffff',
      color: '#8b8f98',
      activeColor: '#6f42c1',
    },
    mode: 2,
    items: [
      {
        text: '首页',
        url: '/pages/index/index',
        iconUrl: 'https://ysyz2025.oss-cn-hangzhou.aliyuncs.com/static/home-active.png',
        activeIconUrl: 'https://ysyz2025.oss-cn-hangzhou.aliyuncs.com/static/home-active.png',
      },
      {
        text: '藏品',
        url: '/pages/index/store',
        iconUrl: 'https://ysyz2025.oss-cn-hangzhou.aliyuncs.com/static/store-active.png',
        activeIconUrl: 'https://ysyz2025.oss-cn-hangzhou.aliyuncs.com/static/store-active.png',
      },
      {
        text: '交易',
        url: '/pages/index/trade',
        iconUrl: 'https://ysyz2025.oss-cn-hangzhou.aliyuncs.com/static/trade-active.png',
        activeIconUrl: 'https://ysyz2025.oss-cn-hangzhou.aliyuncs.com/static/trade-active.png',
      },
      {
        text: '消息',
        url: '/pages/index/message',
        iconUrl: 'https://ysyz2025.oss-cn-hangzhou.aliyuncs.com/static/msg-active.png',
        activeIconUrl: 'https://ysyz2025.oss-cn-hangzhou.aliyuncs.com/static/msg-active.png',
      },
      {
        text: '我的',
        url: '/pages/index/user',
        iconUrl: 'https://ysyz2025.oss-cn-hangzhou.aliyuncs.com/static/mine-active.png',
        activeIconUrl: 'https://ysyz2025.oss-cn-hangzhou.aliyuncs.com/static/mine-active.png',
      },
    ],
  };

  const tabbar = computed(() => {
    return sheep.$store('app').template.basic?.tabbar || fallbackTabbar;
  });

  const tabbarStyle = computed(() => {
    const backgroundStyle = tabbar.value.style;
    if (backgroundStyle.bgType === 'color') {
      return { background: backgroundStyle.bgColor };
    }
    if (backgroundStyle.bgType === 'img')
      return {
        background: `url(${sheep.$url.cdn(
          backgroundStyle.bgImg,
        )}) no-repeat top center / 100% auto`,
      };
  });

  const getTabbarCenter = (index) => {
    if (unref(tabbar).mode !== 2) return false;
    return unref(tabbar).items.length % 2 > 0
      ? Math.ceil(unref(tabbar).items.length / 2) === index + 1
      : false;
  };

  const props = defineProps({
    path: String,
    default: '',
  });
</script>

<style lang="scss">
  .u-page {
    padding: 0;

    &__item {
      &__title {
        color: var(--textSize);
        background-color: #fff;
        padding: 15px;
        font-size: 15px;

        &__slot-title {
          color: var(--textSize);
          font-size: 14px;
        }
      }

      &__slot-icon {
        width: 25px;
        height: 25px;
      }
    }
  }

  .tabbar-icon--inactive {
	filter: grayscale(1);
    opacity: 0.48;
  }

  .tabbar-icon--active {
	filter: none;
    opacity: 1;
  }
</style>
