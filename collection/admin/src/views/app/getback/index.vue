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
            v-for="dict in getIntDictOptions(DICT_TYPE.COLLECTION_DELIVER_STATUS)"
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
      <el-table-column type="selection" width="55" />
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="用户id" align="center" prop="userId" />
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="flex flex-wrap gap-12px px-12px py-6px">
            <div
              v-for="(item, index) in row.items"
              :key="item.id"
              class="flex w-96px flex-col items-center gap-4px"
            >
              <el-image
                class="h-80px w-80px"
                lazy
                :src="item.picUrls ? item.picUrls[0] : item.picUrl"
                :preview-src-list="item.picUrls ? item.picUrls : [item.picUrl]"
                preview-teleported
                fit="cover"
                :initial-index="Number(index)"
              />
              <el-tooltip :content="item.name || '未命名'" placement="top">
                <div class="w-full truncate text-center text-12px text-gray-600">
                  {{ item.name || '未命名' }}
                </div>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COLLECTION_DELIVER_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="收件人名称" align="center" prop="receiverName" />
      <el-table-column label="收件人手机" align="center" prop="receiverMobile" />
      <el-table-column label="收件人地区" align="center" prop="receiverAreaName" />
      <el-table-column label="收件人详细地址" align="center" prop="receiverDetailAddress" />
      <el-table-column label="快递单号" align="center" prop="deliverCode" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="180px" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 0"
            link
            type="primary"
            :disabled="cancelLoadingIds.has(scope.row.id)"
            @click="openForm(scope.row.id)"
            v-hasPermi="['app:getback:update']"
          >
            发货
          </el-button>
          <el-button
            v-if="scope.row.status === 0"
            link
            type="danger"
            :loading="cancelLoadingIds.has(scope.row.id)"
            @click="handleCancel(scope.row)"
            v-hasPermi="['app:getback:update']"
          >
            取消取回
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
import { GetbackApi, Getback } from '@/api/app/getback'
import ConfigForm from './ConfigForm.vue'

/** 取回 列表 */
defineOptions({ name: 'Getback' })

const message = useMessage()
const loading = ref(true) // 列表的加载中
const list = ref<Getback[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await GetbackApi.getGetbackPage(queryParams)
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
const handleRowCheckboxChange = (records: Getback[]) => {
  checkedIds.value = records.map((item) => item.id)
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (id?: number) => {
  formRef.value.open(id)
}

const cancelLoadingIds = reactive(new Set<number>())

/** 取消待发货的取回申请 */
const handleCancel = async (row: Getback) => {
  if (cancelLoadingIds.has(row.id)) return
  try {
    await message.confirm(
      `确认取消取回申请 #${row.id} 吗？取消后不会发货，关联的 ${row.items?.length || 0} 件藏品将恢复为在库、未取回状态。`,
      '取消取回确认'
    )
  } catch {
    // 用户关闭确认框时无需提示。
    return
  }

  cancelLoadingIds.add(row.id)
  try {
    await GetbackApi.cancelGetback(row.id)
    message.success('取回申请已取消，藏品已恢复在库')
    await getList()
  } finally {
    cancelLoadingIds.delete(row.id)
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})

useTodoFilter(queryParams, getList, { status: [0, 1, 2] })
</script>
