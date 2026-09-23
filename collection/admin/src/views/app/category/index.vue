<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['app:collection-category:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="分类编号" align="center" prop="id" />
      <el-table-column label="分类名称" align="center" prop="name" />
      <el-table-column label="图片" align="center" prop="picUrl">
        <template #default="{ row }">
          <el-image
            class="h-80px w-80px"
            lazy
            :src="row.picUrl"
            :preview-src-list="[row.picUrl]"
            preview-teleported
            fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column label="兑换能量石数量" align="center" prop="exchangeRate" />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['app:collection-category:update']"
          >
            编辑
          </el-button>
          <!-- <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['app:collection-category:delete']"
          >
            删除
          </el-button> -->
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <CollectionCategoryForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { isEmpty } from '@/utils/is'
import download from '@/utils/download'
import { CollectionCategoryApi, CollectionCategory } from '@/api/app/category'
import CollectionCategoryForm from './CollectionCategoryForm.vue'

/** 藏品分类 列表 */
defineOptions({ name: 'CollectionCategory' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<CollectionCategory[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CollectionCategoryApi.getCollectionCategoryPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await CollectionCategoryApi.deleteCollectionCategory(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除藏品分类 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await CollectionCategoryApi.deleteCollectionCategoryList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: CollectionCategory[]) => {
  checkedIds.value = records.map((item) => item.id)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await CollectionCategoryApi.exportCollectionCategory(queryParams)
    download.excel(data, '藏品分类.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
