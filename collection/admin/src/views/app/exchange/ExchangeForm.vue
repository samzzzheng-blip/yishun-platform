<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="图片" prop="picUrl">
        <UploadImg v-model="formData.picUrl" />
      </el-form-item>
      <el-form-item label="兑换品名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入兑换品名称" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="库存" prop="stock">
        <el-input-number
          v-model="formData.stock"
          placeholder="请输入库存"
          :min="0"
          :precision="0"
        />
      </el-form-item>
      <el-form-item label="兑换数量" prop="amount">
        <el-input-number
          v-model="formData.amount"
          placeholder="请输入兑换数量"
          :min="0"
          :precision="0"
        />
      </el-form-item>
      <el-form-item label="排序" prop="ordinalPosition">
        <el-input-number
          v-model="formData.ordinalPosition"
          placeholder="请输入排序"
          :min="0"
          :precision="0"
        />
      </el-form-item>
      <el-form-item v-for="(question, index) in formData.questions" :key="index">
        <el-input v-model="formData.questions[index].question" placeholder="请输入" />
      </el-form-item>
      <el-button @click="addQuestion" type="primary" :disabled="formLoading">添加表单</el-button>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { ExchangeApi, Exchange } from '@/api/app/exchange'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'

/** 兑换品 表单 */
defineOptions({ name: 'ExchangeForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  picUrl: undefined,
  name: undefined,
  status: undefined,
  stock: undefined,
  amount: undefined,
  ordinalPosition: undefined,
  questions: []
})
const formRules = reactive({
  name: [{ required: true, message: '兑换品名称不能为空', trigger: 'blur' }],
  amount: [{ required: true, message: '兑换数量不能为空', trigger: 'blur' }],
  stock: [{ required: true, message: '库存不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }],
  ordinalPosition: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

function addQuestion() {
  formData.value.questions.push({ question: '' })
}

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
      formData.value = await ExchangeApi.getExchange(id)
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
    const data = formData.value as unknown as Exchange
    if (formType.value === 'create') {
      await ExchangeApi.createExchange(data)
      message.success(t('common.createSuccess'))
    } else {
      data.questions = data.questions.map((q: any) => ({ question: q.question }))
      await ExchangeApi.updateExchange(data)
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
    picUrl: undefined,
    name: undefined,
    status: undefined,
    stock: undefined,
    amount: undefined,
    ordinalPosition: undefined,
    questions: []
  }
  formRef.value?.resetFields()
}
</script>
