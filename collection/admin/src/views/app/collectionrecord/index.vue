<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="用户名" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          placeholder="请输入用户名"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select
          v-model="queryParams.categoryId"
          placeholder="请选择分类"
          clearable
          filterable
          class="!w-240px"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型" clearable class="!w-240px">
          <el-option label="用户新增" :value="1" />
          <el-option label="管理员新增" :value="2" />
          <el-option label="删除" :value="3" />
          <el-option label="兑换能量石" :value="4" />
          <el-option label="买" :value="5" />
          <el-option label="卖" :value="6" />
          <el-option label="取回" :value="7" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
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
    >
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="用户名" align="center" prop="mobile" />
      <el-table-column label="分类名称" align="center" prop="categoryName" />
      <el-table-column label="数量" align="center" prop="amount">
        <template #default="scope">
          <span :style="{ color: scope.row.amount < 0 ? '#f56c6c' : '#67c23a' }">
            {{ scope.row.amount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="type">
        <template #default="scope">
          <el-tag :type="getTypeTag(scope.row.type)">
            {{ getTypeLabel(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
import { CollectionRecordApi, CollectionRecord } from '@/api/app/collectionrecord'
import { CollectionCategoryApi } from '@/api/app/category'
import { dateFormatter } from '@/utils/formatTime'

/** 藏品变更记录 列表 */
defineOptions({ name: 'CollectionRecord' })

const loading = ref(true)
const list = ref<CollectionRecord[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  mobile: undefined,
  categoryId: undefined,
  type: undefined
})
const queryFormRef = ref()

const categoryOptions = ref<{ label: string; value: number }[]>([])

const loadCategories = async () => {
  try {
    const data = await CollectionCategoryApi.getCategoryList(0)
    categoryOptions.value = data.map((item) => ({
      label: item.name,
      value: item.id
    }))
  } catch (e) {
    console.error('加载分类列表失败', e)
  }
}

const typeMap: Record<number, string> = {
  1: '用户新增',
  2: '管理员新增',
  3: '删除',
  4: '兑换能量石',
  5: '买',
  6: '卖',
  7: '取回'
}

const getTypeLabel = (type: number) => typeMap[type] || '未知'

const getTypeTag = (type: number) => {
  if (type === 3 || type === 4 || type === 6 || type === 7) return 'danger'
  return 'success'
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CollectionRecordApi.getCollectionRecordPage(queryParams)
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

/** 初始化 **/
onMounted(() => {
  loadCategories()
  getList()
})
</script>
