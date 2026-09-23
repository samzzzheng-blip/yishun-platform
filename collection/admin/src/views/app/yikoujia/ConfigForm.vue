<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="80px"
    >
      <el-form-item label="产品名称" prop="name">
        <el-input v-model="formData.name" />
      </el-form-item>
      <el-form-item label="图片" prop="picUrl">
        <UploadImgs v-model="formData.picUrl" />
      </el-form-item>
      <el-form-item label="价格" prop="price">
        <el-input-number v-model="formData.price" :min="0.01" :precision="2" />
      </el-form-item>
      <!-- 产品简介 -->
      <el-form-item label="商品简介" prop="introduction">
        <el-input
          v-model="formData.introduction"
          :autosize="{ minRows: 2, maxRows: 100 }"
          :clearable="true"
          class="w-80!"
          placeholder="请输入产品简介"
          type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { YikoujiaApi } from '@/api/app/yikoujia'
const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('填写产品ID') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formData = ref({
  id: undefined,
  productId: undefined,
  picUrl: [],
  name: undefined,
  price: undefined,
  introduction: undefined
})
const formRef = ref() // 表单 Ref
const formRules = reactive({})
const formType = ref('')

/** 打开弹窗 */
const open = async (type, id?: number) => {
  dialogVisible.value = true
  resetForm()
  formData.value.id = id
  formType.value = type
  if (id) {
    formLoading.value = true
    try {
      const res = await YikoujiaApi.getYikoujia(id)
      formData.value = {
        id: res.id,
        productId: res.productId,
        picUrl: res.picUrl,
        name: res.name,
        introduction: res.introduction,
        price: res.price / 100
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  if (!formData.value.introduction || formData.value.introduction.trim().length < 5) {
    message.error('商品简介不能少于5个字')
    return
  }
  // 提交请求
  formLoading.value = true
  try {
    const data = { ...formData.value, price: Math.round(Number(formData.value.price) * 100) }
    if (formType.value === 'create') {
      await YikoujiaApi.createYikoujia(data)
      message.success(t('common.updateSuccess'))
    } else {
      data.status = 3
      await YikoujiaApi.updateYikoujia(data)
      message.success(t('common.createSuccess'))
    }
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
    productId: undefined,
    picUrl: [],
    name: undefined,
    price: undefined,
    introduction: undefined
  }
  formRef.value?.resetFields()
}
</script>
