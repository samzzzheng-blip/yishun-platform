<template>
  <ContentWrap>
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="账号" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          placeholder="请输入账号"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型" clearable class="!w-240px">
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="账号" align="center" prop="mobile" />
      <el-table-column label="数量" align="center" prop="amount" />
      <el-table-column label="余额" align="center" prop="balance" />
      <el-table-column label="类型" align="center" prop="type">
        <template #default="scope">
          <el-tag size="small">{{ getTypeLabel(scope.row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
      />
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
import { StoneRecordApi, StoneRecord } from '@/api/app/stonerecord'
import { dateFormatter } from '@/utils/formatTime'

defineOptions({ name: 'StoneRecord' })

const typeOptions = [
  { label: '兑换能量石', value: 1 },
  { label: '兑换藏品', value: 2 },
  { label: '管理员', value: 3 },
  { label: '购买容量', value: 4 }
]

const loading = ref(true)
const list = ref<StoneRecord[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: undefined,
  mobile: undefined,
  type: undefined
})
const queryFormRef = ref()

const getTypeLabel = (type: number) => {
  const option = typeOptions.find((item) => item.value === type)
  return option ? option.label : '未知'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await StoneRecordApi.getStoneRecordPage(queryParams)
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
  queryFormRef.value.resetFields()
  handleQuery()
}

onMounted(() => {
  getList()
})
</script>
