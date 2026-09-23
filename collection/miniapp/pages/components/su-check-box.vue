<template>
  <view class="check-box" :class="{ checked }" @tap.stop="$emit('click', !checked)">
    <view class="check" v-show="checked"></view>
  </view>
</template>

<script setup>
const emits = defineEmits(['click']);
const props = defineProps({
  checked: {
    type: Boolean,
    default: false,
  },
});
</script>

<style lang="scss" scoped>
.check-box {
  width: 20px;
  height: 20px;
  position: relative;
  box-sizing: border-box;
  border: 2px solid #c7b5fa;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 2px 5px rgba(93, 57, 190, 0.12);

  &.checked {
    border-color: #6d28d9;
    background: #7c3aed;
    box-shadow: 0 3px 7px rgba(109, 40, 217, 0.3);
  }

  .check {
    position: absolute;
    left: 6px;
    top: 2px;
    width: 5px;
    height: 10px;
    box-sizing: border-box;
    border-right: 2px solid #ffffff;
    border-bottom: 2px solid #ffffff;
    transform: rotate(45deg);
  }
}
</style>
