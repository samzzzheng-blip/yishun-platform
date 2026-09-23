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
      </el-form-item>
    </el-form>
    <el-button type="primary" plain @click="openForm('create')">
      <Icon icon="ep:plus" class="mr-5px" /> 新增能量石
    </el-button>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
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
  <StoneForm ref="formRef" />
</template>

<script setup lang="ts">
import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { StoneExchangeApi, StoneExchange } from '@/api/app/stoneexchange'
import StoneForm from './StoneForm.vue'
const { push } = useRouter() // 路由

/** 能量石兑换 列表 */
defineOptions({ name: 'StoneExchange' })



const loading = ref(true) // 列表的加载中
const list = ref<StoneExchange[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10
})
const queryFormRef = ref() // 搜索的表单

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await StoneExchangeApi.getStoneExchangePage(queryParams)
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







/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
