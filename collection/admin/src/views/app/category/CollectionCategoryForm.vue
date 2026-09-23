<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="分类图片" prop="picUrl">
        <UploadImg v-model="formData.picUrl" />
      </el-form-item>
      <el-form-item label="兑换能量石数量" prop="exchangeRate">
        <el-input-number
          v-model="formData.exchangeRate"
          placeholder="请输入兑换能量石数量"
          :min="1"
          :precision="0"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { CollectionCategoryApi, CollectionCategory } from '@/api/app/category'

/** 藏品分类 表单 */
defineOptions({ name: 'CollectionCategoryForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  name: undefined,
  picUrl: undefined,
  exchangeRate: undefined
})
const formRules = reactive({
  name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
  exchangeRate: [{ required: true, message: '兑换能量石数量不能为空', trigger: 'blur' }],
  picUrl: [{ required: true, message: '分类图片不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await CollectionCategoryApi.getCollectionCategory(id)
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
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as CollectionCategory
    if (formType.value === 'create') {
      data.userId = 0
      await CollectionCategoryApi.createCollectionCategory(data)
      message.success(t('common.createSuccess'))
    } else {
      await CollectionCategoryApi.updateCollectionCategory(data)
      message.success(t('common.updateSuccess'))
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
    name: undefined,
    picUrl: undefined,
    exchangeRate: undefined
  }
  formRef.value?.resetFields()
}
</script>
