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
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COLLECTION_TRADE_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
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
      @selection-change="handleRowCheckboxChange"
      default-expand-all
    >
      <el-table-column type="expand">
        <template #default="{ row }">
          <el-image
            class="h-80px w-80px"
            lazy
            :src="item.picUrls ? item.picUrls[0] : item.picUrl"
            :preview-src-list="item.picUrls ? item.picUrls : [item.picUrl]"
            preview-teleported
            fit="cover"
            v-for="item in row.items"
            :key="item.id"
            :title="item.name"
          />
        </template>
      </el-table-column>
      <el-table-column type="selection" width="55" />
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="用户id" align="center" prop="userId" />

      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COLLECTION_TRADE_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="成交价格" align="center" prop="price" :formatter="fenToYuanFormat" />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 0"
            link
            type="primary"
            @click="openForm(scope.row.id)"
          >
            转账
          </el-button>
          <el-button
            v-if="scope.row.status === 0"
            link
            type="primary"
            @click="trade(2, scope.row.id)"
          >
            撤回
          </el-button>
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
  <ConfigForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { useTodoFilter } from '@/hooks/web/useTodoFilter'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { FastTradeApi, FastTrade } from '@/api/app/fasttrade'
import ConfigForm from './ConfigForm.vue'
import { fenToYuanFormat } from '@/utils/formatter'

/** 快速变现 列表 */
defineOptions({ name: 'FastTrade' })

const loading = ref(true) // 列表的加载中
const list = ref<FastTrade[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单

function trade(status, id) {
  FastTradeApi.updateFastTrade({
    id,
    status
  }).then((res) => {
    getList()
  })
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await FastTradeApi.getFastTradePage(queryParams)
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

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: FastTrade[]) => {
  checkedIds.value = records.map((item) => item.id)
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (id?: number) => {
  formRef.value.open(id)
}

/** 初始化 **/
onMounted(() => {
  getList()
})

useTodoFilter(queryParams, getList, { status: [0, 1, 2] })
</script>
