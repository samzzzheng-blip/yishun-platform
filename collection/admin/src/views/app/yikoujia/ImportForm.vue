<template>
  <Dialog v-model="dialogVisible" title="同步我的闲鱼店铺" width="920px">
    <el-alert
      title="这里只读取当前闲管家应用已授权店铺的商品"
      type="info"
      :closable="false"
      show-icon
      class="mb-16px"
    />

    <div class="product-search">
      <el-input
        v-model.trim="query.source"
        clearable
        placeholder="粘贴闲鱼商品链接，或输入闲管家商品 ID"
        @keyup.enter="handleProductSearch"
        @clear="handleProductSearch"
      >
        <template #prepend>商品链接 / ID</template>
      </el-input>
      <el-button type="primary" :loading="listLoading" @click="handleProductSearch">
        <Icon icon="ep:search" class="mr-5px" />搜索
      </el-button>
      <el-button :disabled="!query.source" @click="resetProductSearch">重置</el-button>
    </div>
    <div class="search-hint">搜索结果仅限当前已授权店铺，链接中需包含商品 ID。</div>

    <div class="toolbar">
      <div>
        <strong>店铺商品</strong>
        <span class="toolbar-hint">选择一件商品后导入一口价管理</span>
      </div>
      <el-button :loading="listLoading" @click="loadProducts">
        <Icon icon="ep:refresh" class="mr-5px" />重新同步
      </el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="products"
      row-key="productId"
      height="390"
      highlight-current-row
      :empty-text="query.source ? '当前授权店铺中未找到该商品' : '授权店铺中暂未读取到可同步商品'"
      @current-change="handleSelect"
    >
      <el-table-column label="图片" width="82">
        <template #default="{ row }">
          <el-image
            v-if="row.images?.[0]"
            :src="row.images[0]"
            :preview-src-list="row.images"
            preview-teleported
            fit="cover"
            class="cover"
          />
          <div v-else class="cover-empty"><Icon icon="ep:picture" /></div>
        </template>
      </el-table-column>
      <el-table-column label="商品" min-width="320" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="product-title">{{ row.title || '未命名商品' }}</div>
          <div class="product-meta">ID {{ row.productId }} · {{ row.sellerName || '已授权店铺' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="110">
        <template #default="{ row }">¥{{ fenToYuan(row.price) }}</template>
      </el-table-column>
      <el-table-column label="库存" prop="stock" width="70" align="center" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMeta(row).type" effect="light">{{ statusMeta(row).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.alreadyImported" type="info">已导入</el-tag>
          <el-button v-else link type="primary" @click.stop="handleSelect(row)">
            {{ selected?.productId === row.productId ? '已选择' : '选择' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      :total="total"
      v-model:page="query.pageNo"
      v-model:limit="query.pageSize"
      @pagination="loadProducts"
    />

    <div v-if="selected" class="selection-panel">
      <div class="selected-product">
        <span>已选择</span>
        <strong>{{ selected.title }}</strong>
        <span class="selected-price">¥{{ fenToYuan(selected.price) }}</span>
      </div>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-position="top">
        <el-form-item label="商品归属" prop="selfOperated">
          <el-radio-group v-model="formData.selfOperated" @change="handleOwnershipChange">
            <el-radio-button :label="true">平台自营</el-radio-button>
            <el-radio-button :label="false">归属会员</el-radio-button>
          </el-radio-group>
          <div class="field-hint">
            {{ formData.selfOperated ? '自营商品不绑定会员，用户 ID 保持为空' : '商品和成交款将归属所选会员' }}
          </div>
        </el-form-item>
        <el-form-item label="商品所属会员" prop="userId" :required="!formData.selfOperated" class="mb-0">
          <el-select
            v-model="formData.userId"
            filterable
            remote
            clearable
            :remote-method="searchMembers"
            :loading="memberLoading"
            :disabled="formData.selfOperated"
            :placeholder="formData.selfOperated ? '平台自营商品无需选择会员' : '输入会员昵称或手机号搜索'"
            class="w-100%"
          >
            <el-option
              v-for="member in memberOptions"
              :key="member.id"
              :value="member.id"
              :label="memberLabel(member)"
            />
          </el-select>
          <div v-if="!formData.selfOperated" class="field-hint">成交款将结算到该会员账户，请仔细核对</div>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button
        type="primary"
        :loading="importLoading"
        :disabled="!canImport"
        @click="handleImport"
      >
        导入所选商品
      </el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { computed, reactive, ref } from 'vue'
import { YikoujiaApi, type GoofishImportPreview } from '@/api/app/yikoujia'
import * as UserApi from '@/api/member/user'
import { fenToYuan } from '@/utils/index'

const message = useMessage()
const dialogVisible = ref(false)
const listLoading = ref(false)
const importLoading = ref(false)
const memberLoading = ref(false)
const products = ref<GoofishImportPreview[]>([])
const selected = ref<GoofishImportPreview>()
const total = ref(0)
const query = reactive({ pageNo: 1, pageSize: 20, source: '' })
const memberOptions = ref<UserApi.UserVO[]>([])
const formRef = ref()
const formData = reactive<{ selfOperated: boolean; userId?: number }>({
  selfOperated: true,
  userId: undefined
})
const formRules = {
  userId: [
    {
      validator: (_rule: unknown, value: number | undefined, callback: (error?: Error) => void) => {
        if (!formData.selfOperated && !value) callback(new Error('请选择商品所属会员'))
        else callback()
      },
      trigger: 'change'
    }
  ]
}
const canImport = computed(
  () =>
    !!selected.value &&
    !selected.value.alreadyImported &&
    (formData.selfOperated || !!formData.userId)
)

const statusMeta = (product: GoofishImportPreview) => {
  if (product.localStatus === 3) return { label: '在售', type: 'success' as const }
  if (product.localStatus === 4) return { label: '已成交', type: 'warning' as const }
  return { label: '未上架', type: 'info' as const }
}

const open = async () => {
  dialogVisible.value = true
  query.pageNo = 1
  query.source = ''
  formData.selfOperated = true
  formData.userId = undefined
  selected.value = undefined
  memberOptions.value = []
  formRef.value?.clearValidate()
  await loadProducts()
}
defineExpose({ open })

const loadProducts = async () => {
  listLoading.value = true
  selected.value = undefined
  try {
    const data = await YikoujiaApi.getGoofishProducts(query)
    products.value = data.list || []
    total.value = data.total || 0
  } finally {
    listLoading.value = false
  }
}

const handleProductSearch = () => {
  query.pageNo = 1
  loadProducts()
}

const resetProductSearch = () => {
  query.source = ''
  query.pageNo = 1
  loadProducts()
}

const handleSelect = async (row?: GoofishImportPreview) => {
  if (!row || row.alreadyImported) return
  selected.value = row
  if (!formData.selfOperated && !memberOptions.value.length) await searchMembers('')
}

const handleOwnershipChange = async () => {
  formData.userId = undefined
  formRef.value?.clearValidate('userId')
  if (!formData.selfOperated && !memberOptions.value.length) await searchMembers('')
}

const searchMembers = async (value: string) => {
  memberLoading.value = true
  try {
    const keyword = value.trim()
    const data = await UserApi.getUserPage({
      pageNo: 1,
      pageSize: 20,
      ...(keyword && /^\d+$/.test(keyword) ? { mobile: keyword } : {}),
      ...(keyword && !/^\d+$/.test(keyword) ? { nickname: keyword } : {})
    })
    memberOptions.value = data.list || []
  } finally {
    memberLoading.value = false
  }
}

const memberLabel = (member: UserApi.UserVO) => {
  const name = member.nickname || member.name || `会员${member.id}`
  return `${name} · ${member.mobile || '无手机号'} · ID ${member.id}`
}

const emit = defineEmits(['success'])
const handleImport = async () => {
  await formRef.value?.validate()
  if (!selected.value || (!formData.selfOperated && !formData.userId)) return
  importLoading.value = true
  try {
    await YikoujiaApi.importGoofishProduct({
      source: selected.value.productId,
      selfOperated: formData.selfOperated,
      userId: formData.selfOperated ? undefined : formData.userId
    })
    message.success('商品已从授权闲鱼店铺导入')
    selected.value.alreadyImported = true
    emit('success')
  } finally {
    importLoading.value = false
  }
}
</script>

<style scoped>
.toolbar,
.selected-product { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.toolbar { margin-bottom: 12px; }
.product-search { display: flex; gap: 8px; margin-bottom: 6px; }
.product-search .el-input { flex: 1; }
.search-hint { margin-bottom: 14px; color: var(--el-text-color-secondary); font-size: 12px; }
.toolbar-hint,
.product-meta,
.field-hint { color: var(--el-text-color-secondary); font-size: 12px; }
.toolbar-hint { margin-left: 10px; }
.cover,
.cover-empty { width: 54px; height: 54px; border-radius: 8px; }
.cover-empty { display: grid; place-items: center; background: var(--el-fill-color-light); color: var(--el-text-color-placeholder); }
.product-title { font-weight: 600; color: var(--el-text-color-primary); }
.product-meta { margin-top: 5px; }
.selection-panel { margin-top: 14px; padding: 16px; border: 1px solid var(--el-border-color-lighter); border-radius: 12px; background: var(--el-fill-color-extra-light); }
.selected-product { justify-content: flex-start; margin-bottom: 14px; }
.selected-product strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.selected-price { margin-left: auto; color: var(--el-color-danger); font-weight: 700; }
.field-hint { margin-top: 5px; }
</style>
