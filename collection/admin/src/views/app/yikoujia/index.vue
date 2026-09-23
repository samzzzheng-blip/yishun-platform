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
          <el-option label="双端成交冲突" :value="6" />
        </el-select>
      </el-form-item>
      <el-form-item label="商品名称" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入商品名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button type="success" plain @click="openImportForm">
          <Icon icon="ep:refresh" class="mr-5px" /> 同步闲鱼店铺
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
      default-expand-all
    >
      <el-table-column type="expand">
        <template #default="{ row }">
          <ImageOrder
:id="row.id" :images="row.picUrl || []"
            @saved="row.picUrl = $event" />
        </template>
      </el-table-column>
      <el-table-column type="selection" width="55" />
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="商品名称" align="center" prop="name" />
      <el-table-column label="用户id" align="center" prop="userId" />
      <el-table-column label="价格" align="center" prop="price" :formatter="fenToYuanFormat" />
      <el-table-column label="商品id" align="center" prop="productId" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 6" type="danger" effect="dark">双端成交冲突</el-tag>
          <dict-tag v-else :type="DICT_TYPE.COLLECTION_TRADE_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="成交渠道" align="center" min-width="120px">
        <template #default="scope">
          <el-tag v-if="scope.row.saleChannel === 'MINIAPP'" type="success">小程序</el-tag>
          <el-tag v-else-if="scope.row.saleChannel === 'GOOFISH'" type="warning">闲鱼</el-tag>
          <el-tag v-else-if="scope.row.saleChannel === 'CONFLICT'" type="danger">待人工核对</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="primary">双端在售</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column
        label="最后同步"
        align="center"
        prop="lastSyncTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="同步说明" align="center" prop="syncRemark" min-width="220px" />
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
            v-if="scope.row.status === 4 && scope.row.collectionId"
            link
            type="primary"
            @click="trade(1, scope.row)"
          >
            转账
          </el-button>
          <el-button v-if="scope.row.status === 0" link type="primary" @click="trade(2, scope.row)">
            撤回
          </el-button>
          <el-button
            v-if="scope.row.status === 0"
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            上架
          </el-button>
          <el-button v-if="scope.row.status === 3" link type="primary" @click="trade(0, scope.row)">
            下架
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
    <Dialog v-model="dialogVisible" title="确认转账">
      <span>{{ `转账金额为${fenToYuan(currentRow.totalPrice)}，是否确认转账？` }}</span>
      <template #footer>
        <el-button type="primary" @click="confirm">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </template>
    </Dialog>
  </ContentWrap>
  <ConfigForm ref="formRef" @success="getList" />
  <ImportForm ref="importFormRef" @success="getList" />
</template>

<script setup lang="ts">
import { useTodoFilter } from '@/hooks/web/useTodoFilter'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { YikoujiaApi, Yikoujia } from '@/api/app/yikoujia'
import { fenToYuanFormat } from '@/utils/formatter'
import { fenToYuan } from '@/utils/index'
import ConfigForm from './ConfigForm.vue'
import ImageOrder from './ImageOrder.vue'
import ImportForm from './ImportForm.vue'

/** 一口价 列表 */
defineOptions({ name: 'Yikoujia' })

const loading = ref(true) // 列表的加载中
const list = ref<Yikoujia[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined,
  pendingSettlement: undefined as boolean | undefined,
  keyword: undefined
})
const queryFormRef = ref() // 搜索的表单

const dialogVisible = ref(false)
const currentRow = ref({})

const formRef = ref()
const openForm = (type, id?: number) => {
  formRef.value.open(type, id)
}

const importFormRef = ref()
const openImportForm = () => importFormRef.value.open()

function trade(status, row) {
  if (status === 2 || status === 0) {
    YikoujiaApi.updateYikoujia({
      id: row.id,
      status
    }).then((res) => {
      getList()
    })
  } else if (status === 1) {
    currentRow.value = row
    currentRow.value.totalPrice = row.price
    dialogVisible.value = true
  }
}

function confirm() {
  YikoujiaApi.updateYikoujia({
    id: currentRow.value.id,
    status: 1
  }).then((res) => {
    dialogVisible.value = false
    getList()
  })
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await YikoujiaApi.getYikoujiaPage(queryParams)
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
  queryParams.pendingSettlement = undefined
  handleQuery()
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: Yikoujia[]) => {
  checkedIds.value = records.map((item) => item.id)
}

/** 初始化 **/
onMounted(() => {
  getList()
})

function test() {
  YikoujiaApi.test()
}

useTodoFilter(queryParams, getList, { status: [0, 4, 6], pendingSettlement: [true, false] })
</script>
