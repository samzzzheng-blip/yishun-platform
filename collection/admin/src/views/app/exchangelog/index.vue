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
            v-for="dict in getIntDictOptions(DICT_TYPE.EXCHANGE_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          placeholder="请输入手机号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
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
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="用户id" align="center" prop="userId" />
      <el-table-column label="名称" align="center" prop="exchangeName" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.EXCHANGE_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="手机号" align="center" prop="mobile" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openInfoForm(scope.row.id)"
            v-hasPermi="['app:exchange-log:update']"
          >
            查看
          </el-button>
          <el-button
            v-if="scope.row.status === 0"
            link
            type="primary"
            @click="openForm(scope.row.id)"
            v-hasPermi="['app:exchange-log:update']"
          >
            发货
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
    <ConfigForm ref="formRef" @success="getList" />
    <InfoForm ref="infoformRef" />
  </ContentWrap>
</template>

<script setup lang="ts">
import { useTodoFilter } from '@/hooks/web/useTodoFilter'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { ExchangeLogApi, ExchangeLog } from '@/api/app/exchangelog'
import ConfigForm from './ConfigForm.vue'
import InfoForm from './InfoForm.vue'

/** 兑换记录 列表 */
defineOptions({ name: 'ExchangeLog' })

const loading = ref(true) // 列表的加载中
const list = ref<ExchangeLog[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined,
  mobile: undefined
})
const queryFormRef = ref() // 搜索的表单

function exchange(id) {
  ExchangeLogApi.updateExchangeLog({
    id,
    status: 1
  }).then((res) => {
    getList()
  })
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ExchangeLogApi.getExchangeLogPage(queryParams)
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
const handleRowCheckboxChange = (records: ExchangeLog[]) => {
  checkedIds.value = records.map((item) => item.id)
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (id?: number) => {
  formRef.value.open(id)
}

const infoformRef = ref()
const openInfoForm = (id?: number) => {
  infoformRef.value.open(id)
}

/** 初始化 **/
onMounted(() => {
  getList()
})

useTodoFilter(queryParams, getList, { status: [0, 1] })
</script>
