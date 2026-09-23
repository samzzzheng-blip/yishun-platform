<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="80px"
    >
      <el-form-item label="快递公司" prop="expressCompany">
        <el-select v-model="formData.expressCompany" placeholder="请选择快递公司">
          <el-option v-for="item in carriers" :key="item[0]" :value="item[0]" :label="item[1]" />
        </el-select>
      </el-form-item>
      <el-form-item label="快递单号" prop="deliverCode">
        <el-input v-model="formData.deliverCode" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { GetbackApi } from '@/api/app/getback'

const carriers = [['shunfeng', '顺丰速运'], ['zhongtong', '中通快递'], ['yuantong', '圆通速递'], ['shentong', '申通快递'], ['yunda', '韵达快递'], ['jd', '京东物流'], ['ems', 'EMS'], ['youzhengguonei', '邮政快递包裹'], ['debangkuaidi', '德邦快递'], ['jtexpress', '极兔速递']]
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('填写快递单号') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formData = ref({
  id: undefined,
  expressCompany: '',
  deliverCode: undefined,
  status: 1
})
const formRef = ref() // 表单 Ref
const formRules = reactive({
  expressCompany: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
  deliverCode: [{ required: true, message: '快递单号不能为空', trigger: 'blur' }]
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
    await GetbackApi.updateGetback(data)
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
    expressCompany: '',
    deliverCode: undefined,
    status: 1
  }
  formRef.value?.resetFields()
}
</script>
