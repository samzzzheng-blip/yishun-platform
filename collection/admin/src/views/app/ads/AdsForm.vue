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
      <el-form-item label="跳转商品" prop="targetProductId">
        <div class="w-full">
          <el-select v-model="formData.targetProductId" filterable remote clearable
            class="w-full" placeholder="输入商品名称或编号搜索" :remote-method="searchProducts"
            :loading="productsLoading" @visible-change="(visible) => visible && searchProducts('')">
            <el-option v-if="selectedProduct" :key="selectedProduct.id" :value="selectedProduct.id"
              :label="selectedProduct.name + '（编号：' + selectedProduct.id + '）'" />
            <el-option v-for="product in products.filter(p => p.id !== selectedProduct?.id)"
              :key="product.id" :value="product.id" :label="product.name + '（编号：' + product.id + '）'" />
            <template #footer>
              <el-button v-if="products.length < productsTotal" link type="primary"
                :loading="productsLoading" @click="loadMoreProducts">加载更多商品</el-button>
            </template>
          </el-select>
          <div class="text-12px text-gray-500 mt-4px">选择在售的一口价商品；留空时广告仅展示图片。</div>
        </div>
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
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { AdsApi, Ads } from '@/api/app/ads'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

/** 广告 表单 */
defineOptions({ name: 'AdsForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  picUrl: undefined,
  status: undefined,
  targetProductId: null as number | null
})
const formRules = reactive({
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref
const products = ref<{ id: number; name: string }[]>([])
const productsTotal = ref(0)
const productsLoading = ref(false)
const selectedProduct = ref<{ id: number; name: string }>()
let productRequest = 0
let productKeyword = ''
let productPage = 1
const loadProducts = async (append = false) => {
  const request = ++productRequest
  productsLoading.value = true
  const nextPage = append ? productPage + 1 : 1
  try {
    const result = await AdsApi.getProductOptions({ keyword: productKeyword, pageNo: nextPage, pageSize: 30 })
    if (request !== productRequest) return
    products.value = append ? [...products.value, ...result.list] : result.list
    productsTotal.value = result.total
    productPage = nextPage
  } finally {
    if (request === productRequest) productsLoading.value = false
  }
}
const searchProducts = (keyword: string) => {
  productKeyword = keyword
  products.value = []
  productsTotal.value = 0
  return loadProducts()
}
const loadMoreProducts = () => loadProducts(true)

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
      const data = await AdsApi.getAds(id)
      formData.value = { ...data, targetProductId: data.targetProductId ?? null }
      if (data.targetProductId) selectedProduct.value = {
        id: data.targetProductId, name: data.targetProductName || '商品已删除'
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
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = { ...formData.value, targetProductId: formData.value.targetProductId || null } as unknown as Ads
    if (formType.value === 'create') {
      await AdsApi.createAds(data)
      message.success(t('common.createSuccess'))
    } else {
      await AdsApi.updateAds(data)
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
  ++productRequest
  products.value = []
  productsTotal.value = 0
  productsLoading.value = false
  selectedProduct.value = undefined
  formData.value = {
    id: undefined,
    picUrl: undefined,
    status: undefined,
    targetProductId: null
  }
  formRef.value?.resetFields()
}
</script>
