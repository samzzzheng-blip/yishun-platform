<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="140px"
      v-loading="formLoading"
    >
      <el-form-item label="藏品数量下限" prop="minCount">
        <el-input v-model.number="formData.minCount" :disabled="formType === 'update'" />
      </el-form-item>
      <el-form-item label="藏品数量上限" prop="maxCount">
        <el-input v-model.number="formData.maxCount" :disabled="formType === 'update'" />
      </el-form-item>
      <el-form-item label="月费（能量石）" prop="monthlyPrice">
        <el-input v-model.number="formData.monthlyPrice" />
      </el-form-item>
      <el-form-item label="年费（能量石）" prop="yearlyPrice">
        <el-input v-model.number="formData.yearlyPrice" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { StoragePlanApi, StoragePlan } from '@/api/app/storage-plan'

defineOptions({ name: 'StoragePlanForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined as number | undefined,
  minCount: undefined as number | undefined,
  maxCount: undefined as number | undefined,
  monthlyPrice: undefined as number | undefined,
  yearlyPrice: undefined as number | undefined
})
const formRules = reactive({
  minCount: [{ required: true, message: '藏品数量下限不能为空', trigger: 'blur' }],
  maxCount: [{ required: true, message: '藏品数量上限不能为空', trigger: 'blur' }],
  monthlyPrice: [{ required: true, message: '月费不能为空', trigger: 'blur' }],
  yearlyPrice: [{ required: true, message: '年费不能为空', trigger: 'blur' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await StoragePlanApi.getStoragePlan(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as StoragePlan
    if (formType.value === 'create') {
      await StoragePlanApi.createStoragePlan(data)
      message.success(t('common.createSuccess'))
    } else {
      await StoragePlanApi.updateStoragePlan(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    minCount: undefined,
    maxCount: undefined,
    monthlyPrice: undefined,
    yearlyPrice: undefined
  }
  formRef.value?.resetFields()
}
</script>