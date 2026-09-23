<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <div v-for="(item, index) in info" :key="index">
      <h4>{{ item.question }}</h4>
      <div>{{ item.answer }}</div>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">确 定</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { ExchangeLogApi, ExchangeLog } from '@/api/app/exchangelog'

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('兑换信息') // 弹窗的标题

const info = ref([])

/** 打开弹窗 */
const open = async (id?: number) => {
  dialogVisible.value = true
  const res = await ExchangeLogApi.getExchangeLog(id)
  info.value = JSON.parse(res.answer)
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗
</script>
