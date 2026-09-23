<template>
  <el-button
    v-if="enabled"
    type="primary"
    plain
    @click="openPanel"
    >入库包裹 / 单号查询</el-button
  >
  <el-drawer v-model="show" title="入库包裹" size="90%" :close-on-click-modal="false">
    <el-alert
      title="先确认收到实物，再逐件核对并审核。异常件可单独处理，其余藏品正常入库。"
      type="info"
      :closable="false"
    />
    <el-form inline class="mt-4" @submit.prevent="search">
      <el-form-item label="包裹状态"><el-select v-model="parcelStatus" clearable placeholder="全部状态" style="width: 170px"><el-option v-for="(label, value) in ['待收货', '已收货核对中', '已全部入库', '已撤销登记']" :key="value" :label="label" :value="value" /></el-select></el-form-item>
      <el-form-item label="快递单号 / 登记编号"
        ><el-input
          v-model="tracking"
          clearable
          placeholder="输入快递单号或线下登记编号"
          @keyup.enter="search"
      /></el-form-item>
      <el-form-item
        ><el-button type="primary" @click="search">查找包裹</el-button
        ><el-button
          @click="resetSearch"
          >重置</el-button
        ></el-form-item
      >
    </el-form>
    <el-alert v-if="error" :title="error" type="error" :closable="false" class="mb-4" />
    <el-table v-loading="loading" :data="rows">
      <el-table-column label="包裹编号" prop="id" width="100" />
      <el-table-column label="用户ID" prop="user_id" width="100" />
      <el-table-column label="交付方式" width="110"><template #default="{ row }">{{ row.delivery_method === 'offline' ? '线下送达' : '邮寄' }}</template></el-table-column>
      <el-table-column label="快递公司" prop="carrier" />
      <el-table-column label="单号" min-width="190"><template #default="{ row }">{{ row.delivery_method === 'offline' ? '无需单号，凭登记编号核对' : row.tracking_no }}</template></el-table-column>
      <el-table-column label="预计件数" prop="expected_count" width="100" />
      <el-table-column label="已核对收到" prop="received_count" width="110" />
      <el-table-column label="状态"
        ><template #default="{ row }">{{ statusName(row.status) }}</template></el-table-column
      >
      <el-table-column label="操作"
        ><template #default="{ row }"
          ><el-button link type="primary" @click="open(row.id)">查看 / 收货</el-button></template
        ></el-table-column
      >
    </el-table>
    <div class="mt-4 flex items-center gap-4"
      ><el-button
        :disabled="page <= 1"
        @click="changePage(-1)"
        >上一页</el-button
      ><span>第 {{ page }} 页</span
      ><el-button
        :disabled="rows.length < 20"
        @click="changePage(1)"
        >下一页</el-button
      ></div
    >
    <section v-if="current" class="mt-6">
      <h3>包裹 {{ current.id }} · {{ current.carrier }} {{ current.tracking_no }}</h3>
      <p>用户 {{ current.user_id }} · 备注：{{ current.remark || '无' }}</p>
      <el-button
        v-if="current.delivery_method !== 'offline'"
        :disabled="busy"
        @click="
          correction = {
            id: current.id,
            version: current.version,
            carrier: current.carrier,
            trackingNo: current.tracking_no,
            reason: ''
          }
        "
        >更正快递信息</el-button
      >
      <el-form v-if="correction" label-position="top" class="mt-4">
        <el-form-item label="快递公司"
          ><el-input v-model="correction.carrier" maxlength="60"
        /></el-form-item>
        <el-form-item label="快递单号"
          ><el-input v-model="correction.trackingNo" maxlength="64"
        /></el-form-item>
        <el-form-item label="更正原因（必填，客户可见）"
          ><el-input v-model="correction.reason" maxlength="500"
        /></el-form-item>
        <el-button type="primary" :loading="busy" @click="saveCorrection">保存更正</el-button
        ><el-button @click="correction = null">取消</el-button>
      </el-form>
      <el-button v-if="current.status === 0" type="primary" :loading="busy" @click="receive"
        >确认公司已收到包裹</el-button
      >
      <el-table :data="current.items" class="mt-4">
        <el-table-column label="藏品编号" prop="collection_id" width="110" />
        <el-table-column label="实物照片（全部）" min-width="280"
          ><template #default="{ row }"
            ><div v-if="row.photos.length" class="flex flex-wrap gap-2">
            <el-image
              v-for="(photo, index) in row.photos"
              :key="photo"
              :src="photo"
              :alt="`${row.name || '藏品'} · 照片 ${index + 1}`"
              :preview-src-list="row.photos"
              :initial-index="index"
              preview-teleported
              fit="contain"
              class="w-20 h-20 cursor-pointer">
              <template #error><span>图片加载失败</span></template>
            </el-image>
            </div><span v-else>暂无上传照片</span></template
        ></el-table-column>
        <el-table-column label="品名" prop="name" min-width="140" />
        <el-table-column label="状态" min-width="150"
          ><template #default="{ row }">{{
            row.audit_status === 1
              ? '已入库'
              : row.audit_status === 2
                ? '已驳回'
                : row.status === 1
                  ? '已核对待审核'
                  : row.status === 2
                    ? '异常待处理'
                    : '待核对'
          }}</template></el-table-column
        >
        <el-table-column label="异常 / 备注" prop="note" min-width="140" />
        <el-table-column label="附件" width="80"
          ><template #default="{ row }"
            ><el-image
              v-if="row.evidence"
              :src="row.evidence"
              :preview-src-list="[row.evidence]"
              preview-teleported
              class="w-12 h-12" /></template
        ></el-table-column>
        <el-table-column label="操作" min-width="270"
          ><template #default="{ row }">
            <template v-if="current.status === 1 && row.audit_status === 0">
              <el-button link type="primary" :disabled="busy" @click="verify(row)"
                >核对收到</el-button
              >
              <el-button link type="warning" :disabled="busy" @click="exception(row)"
                >记录异常</el-button
              >
              <el-button
                v-if="row.status === 1"
                link
                type="success"
                :disabled="busy"
                @click="approve(row)"
                >审核通过入库</el-button
              >
            </template>
            <el-button v-if="current.status === 1 && row.audit_status === 2" link type="primary" :disabled="busy" @click="reopen(row)">重新核对驳回件</el-button>
          </template></el-table-column
        >
      </el-table>
      <el-form v-if="issueItem" label-position="top" class="mt-4">
        <h4>藏品 {{ issueItem.collection_id }} · 异常说明</h4>
        <el-form-item label="少件、错件、破损等情况（必填）"
          ><el-input v-model="issueNote" type="textarea" maxlength="500" show-word-limit
        /></el-form-item>
        <el-form-item label="现场照片（选填）"><UploadImg v-model="issueEvidence" /></el-form-item>
        <el-button type="primary" :loading="busy" @click="saveIssue">保存异常并通知客户</el-button
        ><el-button @click="issueItem = null">取消</el-button>
      </el-form>
      <h4 class="mt-6">处理记录</h4>
      <p v-for="(event, i) in current.events" :key="i"
        >{{ event.create_time }} · {{ event.detail }}</p
      >
    </section>
  </el-drawer>
</template>
<script setup lang="ts">
import { useRoute } from 'vue-router'
import { watch } from 'vue'
import * as api from '@/api/app/inbound'
import { parcelPhotos } from './parcelPhotos'
import { CollectionApi as collectionApi } from '@/api/app/collection'
import { UploadImg } from '@/components/UploadFile'
const message = useMessage()
const enabled = ref(false),
  show = ref(false),
  loading = ref(false),
  busy = ref(false)
const error = ref(''),
  tracking = ref(''),
  page = ref(1)
const rows = ref<any[]>([]),
  current = ref<any>(null),
  issueItem = ref<any>(null)
const issueNote = ref(''),
  issueEvidence = ref('')
const correction = ref<any>(null)
const emit = defineEmits(['updated'])
const route = useRoute()
const ownRouteName = route.name
const parcelStatus = ref<number | undefined>()
function openFromWorkbench() {
  if (route.name !== ownRouteName || route.query.panel !== 'inbound' || !enabled.value) return
  parcelStatus.value = ['0', '1'].includes(String(route.query.parcelStatus)) ? Number(route.query.parcelStatus) : undefined
  tracking.value = ''
  page.value = 1
  current.value = null
  openPanel()
}
watch(() => route.fullPath, openFromWorkbench)
const statusName = (s: number) => ['待收货', '已收货核对中', '已全部入库', '已撤销登记'][s] || '待处理'
async function reopen(row: any) {
  await message.confirm('将此驳回藏品重新放入待核对？需要再次核对实物后才能审核入库。')
  await run(() => api.reopen(current.value.id, row.collection_id))
}
onMounted(async () => {
  try {
    enabled.value = (await api.config()).enabled
    openFromWorkbench()
  } catch {
    enabled.value = false
  }
})
async function load() {
  loading.value = true
  error.value = ''
  try {
    rows.value = await api.list({ tracking: tracking.value.trim(), page: page.value, status: parcelStatus.value })
  } catch {
    error.value = '包裹加载失败，请重新查询'
  } finally {
    loading.value = false
  }
}
function search() {
  page.value = 1
  current.value = null
  issueItem.value = null
  load()
}
function openPanel() {
  show.value = true
  load()
}
function resetSearch() {
  tracking.value = ''
  parcelStatus.value = undefined
  search()
}
function changePage(delta: number) {
  page.value += delta
  load()
}
async function open(id: number) {
  try {
    current.value = await api.detail(id)
    current.value.items = current.value.items.map((item: any) => ({ ...item, photos: parcelPhotos(item) }))
    issueItem.value = null
    correction.value = null
  } catch {
    error.value = '详情加载失败，请重试'
  }
}
async function run(action: () => Promise<unknown>) {
  if (busy.value) return
  busy.value = true
  error.value = ''
  try {
    await action()
    await open(current.value.id)
    await load()
    emit('updated')
  } catch {
    error.value = '操作未完成，请核对提示后重试'
  } finally {
    busy.value = false
  }
}
async function receive() {
  await message.confirm('确认公司已收到此包裹？确认后客户将不能修改包裹信息。')
  await run(() => api.receive(current.value.id))
}
async function saveCorrection() {
  if (
    !correction.value.reason.trim() ||
    !correction.value.carrier.trim() ||
    !/^[A-Za-z0-9-]{6,64}$/.test(correction.value.trackingNo.trim())
  )
    return message.warning('请填写有效快递信息和更正原因')
  await run(() => api.correct(correction.value))
}
async function verify(row: any) {
  await message.confirm('确认已收到并核对藏品“' + row.name + '”？')
  await run(() =>
    api.inspect({
      parcelId: current.value.id,
      collectionId: row.collection_id,
      status: 1,
      note: row.note || '',
      evidence: row.evidence || ''
    })
  )
}
function exception(row: any) {
  issueItem.value = row
  issueNote.value = row.note || ''
  issueEvidence.value = row.evidence || ''
}
async function saveIssue() {
  if (!issueNote.value.trim()) return message.warning('请填写异常说明')
  await run(() =>
    api.inspect({
      parcelId: current.value.id,
      collectionId: issueItem.value.collection_id,
      status: 2,
      note: issueNote.value.trim(),
      evidence: issueEvidence.value
    })
  )
}
async function approve(row: any) {
  await message.confirm('确认藏品“' + row.name + '”审核通过并入库？')
  await run(() => collectionApi.auditCollection({ id: row.collection_id, status: 1 }))
}
</script>
