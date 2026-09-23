<template>
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
      <el-table-column label="寄存档位" align="center" prop="id" />
      <el-table-column label="藏品数量范围" align="center" min-width="150px">
        <template #default="scope">
          {{ scope.row.minCount }} - {{ scope.row.maxCount }} 个
        </template>
      </el-table-column>
      <el-table-column label="月费（能量石）" align="center" prop="monthlyPrice" />
      <el-table-column label="年费（能量石）" align="center" prop="yearlyPrice" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column
        label="更新时间"
        align="center"
        prop="updateTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)">
            编辑
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <StoragePlanForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import { StoragePlanApi, StoragePlan } from '@/api/app/storage-plan'
import StoragePlanForm from './StoragePlanForm.vue'

defineOptions({ name: 'StoragePlan' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<StoragePlan[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await StoragePlanApi.getStoragePlanPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await StoragePlanApi.deleteStoragePlan(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: StoragePlan[]) => {
  checkedIds.value = records.map((item) => item.id)
}

const handleDeleteBatch = async () => {
  try {
    await message.delConfirm()
    for (const id of checkedIds.value) {
      await StoragePlanApi.deleteStoragePlan(id)
    }
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
