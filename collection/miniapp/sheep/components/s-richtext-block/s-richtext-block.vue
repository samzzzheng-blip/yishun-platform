<!-- 装修营销组件：营销文章 -->
<template>
  <view
    class="richtext"
  >
    <mp-html :content="state.content"></mp-html>
  </view>
</template>
<script setup>
  import { reactive, onMounted } from 'vue';
  import ArticleApi from '@/sheep/api/promotion/article';

  const props = defineProps({
    data: {
      type: Object,
      default: {},
    },
    styles: {
      type: Object,
      default() {},
    },
  });

  const state = reactive({
    content: '',
  });

  onMounted(async () => {
    const { data } = await ArticleApi.getArticle(props.data.id, props.data.title);
    state.content = data.content;
  });
</script>
<style lang="scss" scoped>
</style>
