<template>
  <div>
    <p>请上传与闲鱼发布时相同的照片，最多 9 张。第一张是封面，不影响仓库原图。</p>
    <el-upload :show-file-list="false" multiple accept="image/jpeg,image/png,image/webp"
      :before-upload="beforeUpload" :http-request="upload" :disabled="disabled || pending > 0"
      :on-error="() => message.error('图片上传失败，请重试')">
      <el-button :disabled="disabled || pending > 0" :loading="pending > 0">批量上传照片</el-button>
    </el-upload>
    <div v-for="(url, index) in modelValue" :key="url" class="photo-row">
      <el-image :src="url" :preview-src-list="modelValue" :initial-index="index" fit="contain"
        style="width: 72px; height: 72px" preview-teleported />
      <span>{{ index === 0 ? '封面' : `第 ${index + 1} 张` }}</span>
      <el-button :disabled="disabled || pending > 0 || index === 0" @click="move(index, 0)">设为封面</el-button>
      <el-button :disabled="disabled || pending > 0 || index === 0" @click="move(index, index - 1)">前移</el-button>
      <el-button :disabled="disabled || pending > 0 || index === modelValue.length - 1" @click="move(index, index + 1)">后移</el-button>
      <el-button :disabled="disabled || pending > 0" type="danger" plain @click="remove(index)">移除</el-button>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue'
import type { UploadRawFile, UploadRequestOptions } from 'element-plus'
import { useUpload } from '@/components/UploadFile/src/useUpload'
const props = defineProps<{ modelValue: string[]; disabled?: boolean }>()
const emit = defineEmits(['update:modelValue', 'busy'])
const message = useMessage()
const pending = ref(0)
const currentPhotos = ref([...props.modelValue])
watch(() => props.modelValue, value => { currentPhotos.value = [...value] })
const { httpRequest } = useUpload('auction-display')
function beforeUpload(file: UploadRawFile) {
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || file.size > 5 * 1024 * 1024) {
    message.error('请上传不超过 5MB 的 JPG、PNG 或 WebP 图片')
    return false
  }
  if (props.modelValue.length + pending.value >= 9) {
    message.error('最多上传 9 张照片')
    return false
  }
  pending.value++
  emit('busy', true)
  return true
}
async function upload(options: UploadRequestOptions) {
  try {
    const result: any = await httpRequest(options)
    if (typeof result?.data !== 'string' || !/^https?:\/\//.test(result.data)) throw new Error('无效图片地址')
    currentPhotos.value = [...new Set([...currentPhotos.value, result.data])]
    emit('update:modelValue', currentPhotos.value)
    return result
  } finally {
    pending.value--
    emit('busy', pending.value > 0)
  }
}
function move(from: number, to: number) {
  const list = [...props.modelValue]
  list.splice(to, 0, list.splice(from, 1)[0])
  emit('update:modelValue', list)
}
function remove(index: number) { emit('update:modelValue', props.modelValue.filter((_, i) => i !== index)) }
</script>
<style scoped>
.photo-row { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
</style>
