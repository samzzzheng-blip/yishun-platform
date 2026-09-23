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
      <el-form-item label="品名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入品名"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COLLECTION_AUDIT_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="账号" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入账号"
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
    <el-button type="primary" plain @click="openForm('create')">
      <Icon icon="ep:plus" class="mr-5px" /> 新增
    </el-button>
    <InboundParcels @updated="getList" />
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
      <el-table-column label="藏品编号" align="center" prop="id" />
      <el-table-column label="品名" align="center" prop="name" />
      <el-table-column label="分类" align="center" prop="categoryName" />
      <el-table-column label="数量" align="center" prop="stock" />
      <el-table-column label="账号" align="center" prop="mobile" />
      <el-table-column label="照片" align="center" prop="picUrl">
        <template #default="{ row }">
          <el-image
            class="h-80px w-80px"
            lazy
            :src="row.picUrls ? row.picUrls[0] : row.picUrl"
            :preview-src-list="row.picUrls ? row.picUrls : [row.picUrl]"
            preview-teleported
            fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COLLECTION_AUDIT_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="上传人" align="center" prop="creatorUserName" />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 0"
            link
            type="primary"
            @click="audit(1, scope.row.id)"
          >
            通过
          </el-button>
          <el-button
            v-if="scope.row.status === 0"
            link
            type="danger"
            @click="audit(2, scope.row.id)"
          >
            驳回
          </el-button>
          <el-button
            v-if="scope.row.stock > 0"
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            编辑
          </el-button>
          <el-button
            v-if="scope.row.creatorUserName && scope.row.stock > 0"
            link
            type="danger"
            @click="delCollection(scope.row.id)"
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
  <CollectionForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { useTodoFilter } from '@/hooks/web/useTodoFilter'
import InboundParcels from './InboundParcels.vue'
import { CollectionApi, Collection } from '@/api/app/collection'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import CollectionForm from './CollectionForm.vue'

/** 藏品登记 列表 */
defineOptions({ name: 'Collection' })

const loading = ref(true) // 列表的加载中
const list = ref<Collection[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  userId: undefined,
  categoryId: undefined,
  status: undefined,
  userName: undefined
})
const queryFormRef = ref() // 搜索的表单

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CollectionApi.getCollectionPage(queryParams)
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

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: Collection[]) => {
  checkedIds.value = records.map((item) => item.id)
}

function audit(status, id) {
  CollectionApi.auditCollection({
    id,
    status
  }).then((res) => {
    getList()
  })
}

function delCollection(id) {
  CollectionApi.deleteCollection(id).then((res) => {
    getList()
  })
}

/** 初始化 **/
onMounted(() => {
  getList()
})

useTodoFilter(queryParams, getList, { status: [0, 1, 2] })
</script>
