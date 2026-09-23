<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="80px"
    >
      <el-form-item label="转账金额" prop="price">
        <el-input-number v-model="formData.price" :min="0.01" :precision="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { FastTradeApi } from '@/api/app/fasttrade'

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('填写转账金额') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formData = ref({
  id: undefined,
  price: undefined,
  status: 1
})
const formRef = ref() // 表单 Ref
const formRules = reactive({
  price: [{ required: true, message: '转账金额不能为空', trigger: 'blur' }]
})

/** 打开弹窗 */
const open = async (id?: number) => {
  dialogVisible.value = true
  resetForm()
  formData.value.id = id
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value
    data.price = data.price * 100
    await FastTradeApi.updateFastTrade(data)
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    price: undefined,
    status: 1
  }
  formRef.value?.resetFields()
}
</script>
