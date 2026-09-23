<template>
  <view>
    <view v-for="(item, index) in popupList" :key="index">
      <su-popup
        v-if="index === currentIndex"
        :show="item.isShow"
        type="center"
        backgroundColor="none"
        round="0"
        :showClose="true"
        :isMaskClick="false"
        @close="onClose(index)"
      >
        <view class="img-box">
          <scroll-view scroll-y="true" class="modal-img">
			  <s-richtext-block :data="item"></s-richtext-block>
		  </scroll-view>
        </view>
      </su-popup>
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { computed, ref } from 'vue';
  import { saveAdvHistory } from '@/sheep/hooks/useModal';

  // 定义属性
  const props = defineProps({
    data: {
      type: Object,
      default() {},
    }
  })

  // const modalStore = sheep.$store('modal');
  const modalStore = JSON.parse(uni.getStorageSync('modal-store') || '{}');
  console.log(modalStore)
  const advHistory = modalStore.advHistory || [];
  const currentIndex = ref(0);
  const popupList = computed(() => {
    const list = props.data.list || [];
    const newList = [];
    if (list.length > 0) {
      list.forEach((adv) => {
        if (adv.showType === 'once' && advHistory.includes(adv.title)) {
          adv.isShow = false;
        } else {
          adv.isShow = true;
          newList.push(adv);
        }

        // 记录弹窗已显示过
        saveAdvHistory(adv);
      });
    }
    return newList;
  });

  // 跳转链接
  function onPopup(path) {
    sheep.$router.go(path);
  }

  // 关闭
  function onClose(index) {
    currentIndex.value = index + 1;
    popupList.value[index].isShow = false;
  }
</script>

<style lang="scss" scoped>
  .img-box {
    width: 610rpx;
	background: var(--ys-surface);
	border: 1rpx solid var(--ys-border);
	border-radius: 24rpx;
	padding: 30rpx;
	box-sizing: border-box;
	box-shadow: 0 18rpx 48rpx rgba(24, 24, 27, 0.18);
  }
  .modal-img {
    width: 100%;
	max-height: 800rpx;
  }
</style>
