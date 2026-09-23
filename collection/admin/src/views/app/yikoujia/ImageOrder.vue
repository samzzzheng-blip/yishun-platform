<template>
  <div class="image-order" v-loading="saving" element-loading-text="正在保存图片顺序">
    <div class="image-order__hint" v-if="images.length > 1">
      左右拖动图片上方手柄调整顺序，松开后自动保存；第一张为主图。
    </div>
    <VueDraggable
v-model="items" item-key="key" handle=".image-order__handle"
      class="image-order__list" :animation="180" :disabled="saving || !canEdit"
      direction="horizontal" @change="saveOrder">
      <template #item="{ element, index }">
        <div class="image-order__item">
          <button
v-if="canEdit && images.length > 1" type="button"
            class="image-order__handle" :aria-label="`拖动第 ${index + 1} 张图片排序`"
            title="左右拖动排序">⠿ {{ index + 1 }}</button>
          <el-image
class="image-order__image" :src="element.url" fit="cover" lazy
            :preview-src-list="items.map(item => item.url)" :initial-index="index"
            preview-teleported />
        </div>
      </template>
    </VueDraggable>
    <span v-if="!images.length">暂无图片</span>
  </div>
</template>
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import VueDraggable from 'vuedraggable'
import { ElMessage } from 'element-plus'
import { YikoujiaApi } from '@/api/app/yikoujia'
import { checkPermi } from '@/utils/permission'

const props = defineProps<{ id: number; images: string[] }>()
const emit = defineEmits<{ saved: [images: string[]] }>()
const saving = ref(false)
const canEdit = computed(() => checkPermi(['app:yikoujia:update']))
const items = ref<{ key: number; url: string }[]>([])
const reset = () => { items.value = props.images.map((url, key) => ({ key, url })) }
watch(() => props.images, reset, { immediate: true })
async function saveOrder() {
  const original = [...props.images]
  const reordered = items.value.map(item => item.url)
  if (original.every((url, i) => url === reordered[i])) return
  saving.value = true
  try {
    await YikoujiaApi.updateImageOrder(props.id, original, reordered)
    emit('saved', reordered)
    ElMessage.success('图片顺序已保存')
  } catch {
    reset()
    ElMessage.error('图片顺序未保存，已恢复原顺序；请刷新后重试')
  } finally {
    saving.value = false
  }
}
</script>
<style scoped>
.image-order { padding: 8px 16px; }
.image-order__hint { margin-bottom: 8px; font-size: 12px; color: var(--el-text-color-secondary); }
.image-order__list { display: flex; gap: 10px; overflow-x: auto; padding-bottom: 4px; }
.image-order__item { flex: 0 0 80px; }
.image-order__handle { display: block; width: 80px; border: 0; border-radius: 4px 4px 0 0; padding: 3px;
  color: var(--el-text-color-regular); background: var(--el-fill-color-light); cursor: grab; touch-action: none; }
.image-order__handle:active { cursor: grabbing; }
.image-order__image { width: 80px; height: 80px; }
</style>
