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
        <UploadImgs v-model="formData.picUrls" />
      </el-form-item>
      <el-form-item label="账号" prop="user" v-if="formType === 'create'">
        <el-input v-model="formData.user" placeholder="请输入账号" @blur="getCategory" />
      </el-form-item>
      <el-form-item label="品名" prop="name">
        <el-input v-model="formData.name" placeholder="请输入品名" />
      </el-form-item>

      <el-form-item label="分类" prop="categoryId">
        <el-select
          v-model="formData.categoryId"
          placeholder="请选择分类"
          clearable
          class="!w-240px"
        >
          <el-option
            :value="item.value"
            :label="item.label"
            v-for="item in categoryOptions"
            :key="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="新增自定义分类" prop="customCategory">
        <el-input v-model="formData.customCategory" placeholder="请输入自定义分类" />
      </el-form-item>
      <el-form-item label="数量" prop="stock">
        <el-input v-model="formData.stock" placeholder="请输入数量" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { CollectionApi, Collection } from '@/api/app/collection'
import { CollectionCategoryApi } from '@/api/app/category'

/** 藏品登记 表单 */
defineOptions({ name: 'CollectionForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  name: undefined,
  userId: undefined,
  categoryId: undefined,
  picUrls: [],
  stock: undefined,
  categoryName: undefined,
  customCategory: undefined,
  user: undefined
})
const formRules = reactive({
  name: [{ required: true, message: '品名不能为空', trigger: 'blur' }],
  user: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  stock: [{ required: true, message: '数量不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

const categoryOptions = ref([])

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
      const data = await CollectionApi.getCollection(id)
      formData.value = data
      if (!data.picUrls) {
        formData.value.picUrls = [data.picUrl]
      }
      CollectionCategoryApi.getCategoryList(formData.value.userId).then((data) => {
        categoryOptions.value = data.map((item) => ({
          label: item.name,
          value: item.id
        }))
      })
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
    const data = formData.value as unknown as Collection
    if (!data.categoryId && !data.customCategory) {
      message.alert('请选择分类')
      return
    }
    data.categoryName =
      categoryOptions.value.find((item) => item.value === data.categoryId)?.label || ''
    if (formType.value === 'create') {
      await CollectionApi.createCollection(data)
      message.success(t('common.createSuccess'))
    } else {
      await CollectionApi.updateCollection(data)
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
    userId: undefined,
    categoryId: undefined,
    picUrls: [],
    stock: undefined,
    categoryName: undefined,
    customCategory: undefined,
    user: undefined
  }
  formRef.value?.resetFields()
}

function getCategory() {
  CollectionCategoryApi.getCategoryListByUser(formData.value.user).then((data) => {
    categoryOptions.value = data.map((item) => ({
      label: item.name,
      value: item.id
    }))
  })
}
</script>
