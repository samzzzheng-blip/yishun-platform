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
        <el-input v-model="queryParams.mobile" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
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
    <el-table-column type="selection" width="55" />
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="用户名" align="center" prop="mobile" />
      <el-table-column label="分类名称" align="center" prop="categoryName" />

      <el-table-column label="交易数量" align="center" prop="dealAmount" />
      <el-table-column label="已成交数量" align="center" prop="amount">
        <template #default="scope">
          {{ scope.row.dealAmount - scope.row.amount }}
        </template>
      </el-table-column>
      <el-table-column label="价格" align="center" prop="price" :formatter="fenToYuanFormat" />
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
import { dateFormatter } from '@/utils/formatTime'
import { SellOrderApi, SellOrder } from '@/api/app/sellorder'
import { fenToYuanFormat } from '@/utils/formatter'

/** 批量交易卖单 列表 */
defineOptions({ name: 'SellOrder' })



const loading = ref(true) // 列表的加载中
const list = ref<SellOrder[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  mobile: '',
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await SellOrderApi.getSellOrderPage(queryParams)
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
  getList()
})
</script>