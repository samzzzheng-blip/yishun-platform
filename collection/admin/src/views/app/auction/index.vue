<template>
  <ContentWrap>
    <el-alert
      title="人工发布流程"
      description="请先在闲鱼人工发布竞拍，再关联闲管家 product_id，自动读取商品信息和展示照片。检查状态会刷新接口信息；接口标价不等于实时竞拍价，售出或下架后仍需人工核对成交结果。"
      type="info"
      show-icon
      :closable="false"
    />
  </ContentWrap>
  <ContentWrap>
    <el-form ref="queryFormRef" :model="query" inline>
      <el-form-item label="商品名称" prop="keyword"
        ><el-input v-model="query.keyword" clearable @keyup.enter="search"
      /></el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="query.status" clearable class="!w-180px">
          <el-option
            v-for="item in statuses"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="下架审核" prop="delistStatus">
        <el-select v-model="query.delistStatus" clearable class="!w-160px">
          <el-option
            v-for="item in delistStatuses"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search"><Icon icon="ep:search" />搜索</el-button>
        <el-button @click="reset"><Icon icon="ep:refresh" />重置</el-button>
        <el-button type="warning" plain @click="syncAll">检查全部到期竞拍</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="ID" prop="id" width="80" />
      <el-table-column label="拍品" min-width="260">
        <template #default="{ row }">
          <div class="flex items-center gap-12px">
            <el-image
              v-if="coverUrl(row)"
              class="h-56px w-56px flex-none rounded-6px"
              :src="coverUrl(row)"
              :preview-src-list="imageUrls(row)"
              preview-teleported
              fit="cover"
            />
            <div class="min-w-0">
              <div class="truncate font-500">{{ row.collectionName }}</div>
              <div class="mt-4px text-xs text-gray-500">{{ row.categoryName || '未分类' }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="用户ID" prop="sellerId" width="100" />
      <el-table-column label="起拍价" :formatter="fenToYuanFormat" prop="startPrice" width="100" />
      <el-table-column
        label="本地参考价"
        :formatter="fenToYuanFormat"
        prop="currentPrice"
        width="100"
      />
      <el-table-column label="最低加价" :formatter="fenToYuanFormat" prop="minIncrement" />
      <el-table-column label="出价次数" prop="bidCount" />
      <el-table-column label="闲鱼商品" min-width="150">
        <template #default="{ row }">
          <el-link v-if="row.goofishUrl" :href="row.goofishUrl" target="_blank" type="primary">{{
            row.goofishProductId || '打开竞拍'
          }}</el-link>
          <span v-else>{{ row.goofishProductId || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="闲管家信息" min-width="190">
        <template #default="{ row }">
          <div>{{ row.goofishManagedProductId || '未关联' }}</div>
          <el-button v-if="row.goofishDetail" link type="primary" @click="detailAuction = row">查看接口信息</el-button>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          <div v-if="row.status === 7 && row.reviewRejectReason" class="mt-1 text-xs text-red-500">
            {{ row.reviewRejectReason }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="下架审核" min-width="130">
        <template #default="{ row }">
          <el-tag :type="delistTagType(row.delistStatus)">{{
            delistStatusLabel(row.delistStatus)
          }}</el-tag>
          <div
            v-if="row.delistStatus === 1 && row.delistApplyTime"
            class="text-xs text-gray-500 mt-1"
            >{{ formatDate(new Date(row.delistApplyTime)) }}</div
          >
          <div
            v-if="row.delistStatus === 3 && row.delistRejectReason"
            class="text-xs text-red-500 mt-1"
            >{{ row.delistRejectReason }}</div
          >
        </template>
      </el-table-column>
      <el-table-column label="截止时间" prop="endTime" :formatter="dateFormatter" width="180" />
      <el-table-column label="处理提示" prop="syncError" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="290" fixed="right">
        <template #default="{ row }">
          <el-button v-if="[0, 1, 2, 6].includes(row.status)" v-hasPermi="['app:auction:update']" link type="primary" @click="openManaged(row)">关联闲管家</el-button>
          <el-button v-hasPermi="['app:auction:update']" link type="primary" @click="openPhotos(row)">展示照片</el-button>
          <el-button v-hasPermi="['app:auction:update']" link type="primary" @click="openJump(row)">分享文案</el-button>
          <el-button v-if="[0, 6].includes(row.status)" link type="primary" @click="openBind(row)">
            录入闲鱼竞拍
          </el-button>
          <el-button
            v-if="[0, 6].includes(row.status)"
            link
            type="danger"
            @click="rejectPublish(row)"
          >
            驳回
          </el-button>
          <el-button
            v-if="row.status === 1 && row.delistStatus === 1"
            link
            type="success"
            @click="approveDelist(row)"
            >同意下架</el-button
          >
          <el-button
            v-if="row.status === 1 && row.delistStatus === 1"
            link
            type="danger"
            @click="rejectDelist(row)"
            >拒绝</el-button
          >
          <el-button v-if="[1, 2].includes(row.status)" link type="primary" :loading="syncingIds.includes(row.id)" @click="sync(row)"
            >检查状态</el-button
          >
          <el-button v-if="row.status === 2" link type="danger" @click="openConfirmSale(row)"
            >确认已售出</el-button
          >
          <el-button v-if="row.status === 2" link type="warning" @click="confirmUnsold(row)"
            >确认流拍</el-button
          >
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="query.pageNo"
      v-model:limit="query.pageSize"
      @pagination="load"
    />
  </ContentWrap>

  <Dialog v-model="jumpVisible" title="闲鱼跳转配置" width="620px" :close-on-click-modal="false" :show-close="!jumpSaving" :close-on-press-escape="!jumpSaving">
    <el-alert title="支持闲鱼 App 的完整分享文案，或微信闲鱼小程序商品页复制的 #小程序://闲鱼/ 链接。保存前请确认是同一拍品。" type="info" :closable="false" />
    <el-form label-position="top" class="mt-16px" :disabled="jumpSaving">
      <el-form-item label="完整分享文案"><el-input v-model="jumpForm.shareText" type="textarea" :rows="6" maxlength="3000" show-word-limit placeholder="粘贴完整分享文案或 #小程序://闲鱼/… 链接" /></el-form-item>
      <el-alert v-if="jumpError" :title="jumpError" type="error" :closable="false" role="alert" />
    </el-form>
    <template #footer><el-button :disabled="jumpSaving" @click="jumpVisible = false">取消</el-button><el-button type="primary" :loading="jumpSaving" @click="saveJump">保存配置</el-button></template>
  </Dialog>
  <Dialog v-model="photosVisible" title="拍卖展示照片" width="680px" :close-on-click-modal="false" :show-close="!photosBusy && !photosSaving" :close-on-press-escape="!photosBusy && !photosSaving">
    <p>{{ photosAuction?.collectionName }}</p>
    <DisplayPhotos v-if="photosVisible" v-model="photos" :disabled="photosSaving" @busy="photosBusy = $event" />
    <template #footer>
      <el-button :disabled="photosBusy || photosSaving" @click="photosVisible = false">取消</el-button>
      <el-button type="primary" :disabled="photosBusy || !photos.length" :loading="photosSaving" @click="savePhotos">保存照片</el-button>
    </template>
  </Dialog>
  <Dialog v-model="bindDialogVisible" title="录入闲鱼竞拍" width="680px" :close-on-click-modal="false" :show-close="!photosBusy && !binding" :close-on-press-escape="!photosBusy && !binding">
    <el-alert
      title="请先在闲鱼确认已发布为“竞拍”，不是普通定价商品。"
      type="warning"
      show-icon
      :closable="false"
      class="mb-20px"
    />
    <el-descriptions v-if="bindingAuction" :column="1" border class="mb-20px">
      <el-descriptions-item label="拍品">{{ bindingAuction.collectionName }}</el-descriptions-item>
      <el-descriptions-item label="起拍价"
        >¥{{ (bindingAuction.startPrice / 100).toFixed(2) }}</el-descriptions-item
      >
      <el-descriptions-item label="最低加价"
        >¥{{ (bindingAuction.minIncrement / 100).toFixed(2) }}</el-descriptions-item
      >
      <el-descriptions-item label="申请竞拍时长">{{
        requestedDurationLabel(bindingAuction)
      }}</el-descriptions-item>
    </el-descriptions>
    <el-form ref="bindFormRef" :model="bindForm" :rules="bindRules" label-position="top">
      <el-form-item label="拍卖展示照片" prop="displayPicUrls">
        <DisplayPhotos v-if="bindDialogVisible" v-model="bindForm.displayPicUrls" :disabled="binding" @busy="photosBusy = $event" />
      </el-form-item>
      <el-form-item label="闲鱼竞拍商品ID / 链接" prop="goofishUrl">
        <el-input
          v-model="bindForm.goofishUrl"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          placeholder="输入闲管家‘拍卖中’页面显示的闲鱼商品ID，或粘贴含ID的完整链接"
        />
        <div class="mt-6px text-xs text-gray-500"
          >请人工确认该商品确为竞拍。闲管家开放接口暂不提供拍卖实时出价，系统会按申请截止时间进入结果确认。</div
        >
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="photosBusy || binding" @click="bindDialogVisible = false">取消</el-button>
      <el-button type="primary" :disabled="photosBusy" :loading="binding" @click="submitBind">确认接管</el-button>
    </template>
  </Dialog>

  <Dialog :model-value="!!managedAuction" @update:model-value="!$event && !managedSaving && (managedAuction = undefined)" title="关联闲管家商品" width="620px" :close-on-click-modal="false" :show-close="!managedSaving" :close-on-press-escape="!managedSaving">
    <p>{{ managedAuction?.collectionName }}</p>
    <el-alert title="请确认对应的是同一件竞拍商品。关联后自动读取照片，并替换拍卖展示照片，不修改藏品原始照片。不要填写闲鱼 item_id。" type="warning" :closable="false" />
    <el-form label-position="top" class="mt-16px" @submit.prevent="saveManaged">
      <el-form-item label="闲管家 product_id" :error="managedError">
        <el-input v-model="managedId" :disabled="managedSaving" placeholder="例如 1746190442089157" @blur="validateManaged" />
      </el-form-item>
    </el-form>
    <template #footer><el-button :disabled="managedSaving" @click="managedAuction = undefined">取消</el-button><el-button type="primary" :loading="managedSaving" @click="saveManaged">读取并关联</el-button></template>
  </Dialog>
  <Dialog :model-value="!!detailAuction" @update:model-value="!$event && (detailAuction = undefined)" title="闲管家接口信息" width="620px">
    <el-alert title="以下是上次成功读取的商品信息。接口标价不是实时竞拍价，状态码暂按原值展示，不据此自动结算。" type="info" :closable="false" />
    <el-descriptions :column="1" border class="mt-16px">
      <el-descriptions-item label="闲管家 product_id">{{ detailAuction?.goofishManagedProductId }}</el-descriptions-item>
      <el-descriptions-item label="闲鱼 item_id">{{ remoteDetail.item_id || '—' }}</el-descriptions-item>
      <el-descriptions-item label="商品名称">{{ remoteDetail.title || '—' }}</el-descriptions-item>
      <el-descriptions-item label="接口标价（非竞拍价）">{{ remoteDetail.price == null ? '—' : `¥${(Number(remoteDetail.price) / 100).toFixed(2)}` }}</el-descriptions-item>
      <el-descriptions-item label="库存 / 已售数量">{{ remoteDetail.stock ?? '—' }} / {{ remoteDetail.sold ?? '—' }}</el-descriptions-item>
      <el-descriptions-item label="商品 / 发布状态码">{{ remoteDetail.product_status ?? '—' }} / {{ remoteDetail.publish_status ?? '—' }}</el-descriptions-item>
      <el-descriptions-item label="最近检查时间">{{ detailAuction?.lastSyncTime ? formatDate(new Date(detailAuction.lastSyncTime)) : '—' }}</el-descriptions-item>
      <el-descriptions-item label="检查提示">{{ detailAuction?.syncError || '—' }}</el-descriptions-item>
    </el-descriptions>
  </Dialog>
  <Dialog v-model="saleDialogVisible" title="确认闲鱼订单已成交" width="620px">
    <el-alert
      title="此操作会扣除手续费，并立即把卖家实收金额记入用户钱包。此处不会发起微信转账。"
      type="warning"
      show-icon
      :closable="false"
      class="mb-20px"
    />
    <el-descriptions v-if="saleAuction" :column="1" border class="mb-20px">
      <el-descriptions-item label="售卖商品">{{ saleAuction.collectionName }}</el-descriptions-item>
      <el-descriptions-item label="卖家用户ID">{{ saleAuction.sellerId }}</el-descriptions-item>
      <el-descriptions-item label="登记参考价">¥{{ (saleAuction.currentPrice / 100).toFixed(2) }}</el-descriptions-item>
      <el-descriptions-item label="手续费设置">{{ feeRateText }}</el-descriptions-item>
      <el-descriptions-item label="手续费">¥{{ (calculatedFee / 100).toFixed(2) }}</el-descriptions-item>
      <el-descriptions-item label="进入钱包">¥{{ (calculatedIncome / 100).toFixed(2) }}</el-descriptions-item>
    </el-descriptions>
    <el-form label-position="top">
      <el-form-item label="闲鱼订单实际已付款金额（元）" required>
        <el-input-number v-model="saleForm.grossYuan" :min="0.01" :max="1000000" :precision="2" :step="1" class="!w-full" />
      </el-form-item>
      <el-form-item label="核对说明" required>
        <el-input v-model="saleForm.remark" maxlength="200" show-word-limit placeholder="例如：已核对订单号后四位 1234，买家已付款且当前无退款" />
      </el-form-item>
      <el-checkbox v-model="saleForm.checked">我已在闲鱼核对订单已付款、商品一致且当前无退款</el-checkbox>
    </el-form>
    <template #footer>
      <el-button @click="saleDialogVisible = false">取消</el-button>
      <el-button type="danger" :loading="saleSubmitting" :disabled="!saleForm.checked" @click="submitConfirmSale">确认并记入钱包</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { useTodoFilter } from '@/hooks/web/useTodoFilter'
import { AuctionApi, type Auction } from '@/api/app/auction'
import DisplayPhotos from './DisplayPhotos.vue'
import { dateFormatter, formatDate } from '@/utils/formatTime'
import { fenToYuanFormat } from '@/utils/formatter'
import * as ConfigApi from '@/api/infra/config'
defineOptions({ name: 'Auction' })
const message = useMessage()
const managedAuction = ref<Auction>()
const managedId = ref('')
const managedSaving = ref(false)
const managedError = ref('')
const detailAuction = ref<Auction>()
const remoteDetail = computed(() => {
  try { return JSON.parse(detailAuction.value?.goofishDetail || '{}') || {} } catch { return {} }
})
function openManaged(row: Auction) {
  managedAuction.value = row
  managedId.value = row.goofishManagedProductId || ''
  managedError.value = ''
}
function validateManaged() {
  managedError.value = /^[0-9]{8,18}$/.test(managedId.value.trim()) ? '' : '请输入 8–18 位数字的闲管家 product_id'
  return !managedError.value
}
async function saveManaged() {
  if (managedSaving.value || !managedAuction.value || !validateManaged()) return
  managedSaving.value = true
  try {
    await AuctionApi.bindManagedProduct(managedAuction.value.id, managedId.value.trim())
    managedAuction.value = undefined
    message.success('已关联，请查看接口信息和处理提示')
    await load()
  } catch (error: any) {
    managedError.value = error?.message || '关联失败，请核对商品编号、店铺授权后重试'
  } finally { managedSaving.value = false }
}
const syncingIds = ref<number[]>([])
const statuses = [
  { value: 0, label: '待人工发布' },
  { value: 1, label: '闲鱼竞拍中' },
  { value: 2, label: '待确认成交' },
  { value: 3, label: '已售出（已入钱包）' },
  { value: 4, label: '流拍' },
  { value: 5, label: '已取消' },
  { value: 6, label: '同步异常' },
  { value: 7, label: '已驳回' }
]
const delistStatuses = [
  { value: 0, label: '未申请' },
  { value: 1, label: '待审核' },
  { value: 2, label: '已同意' },
  { value: 3, label: '已拒绝' }
]
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: undefined,
  status: undefined,
  delistStatus: undefined
})
const queryFormRef = ref()
const loading = ref(false)
const list = ref<Auction[]>([])
const total = ref(0)
const statusLabel = (value: number) =>
  statuses.find((item) => item.value === value)?.label || '处理中'
const statusTagType = (value: number): 'warning' | 'success' | 'danger' | 'info' => {
  if (value === 0) return 'warning'
  if (value === 1 || value === 3) return 'success'
  if (value === 2) return 'warning'
  if (value === 6 || value === 7) return 'danger'
  return 'info'
}
const imageUrls = (row: Auction) => {
  if (row.displayPicUrls?.length) return row.displayPicUrls.filter(Boolean)
  const urls = [row.picUrl, ...(Array.isArray(row.picUrls) ? row.picUrls : [])]
  return [...new Set(urls.filter((url): url is string => Boolean(url?.trim())))]
}
const coverUrl = (row: Auction) => imageUrls(row)[0] || ''
const requestedDurationLabel = (row: Auction) => {
  const submittedAt = new Date(row.createTime).getTime()
  const requestedEndAt = new Date(row.endTime).getTime()
  if (!Number.isFinite(submittedAt) || !Number.isFinite(requestedEndAt)) return '-'
  const durationDays = Math.max(1, Math.round((requestedEndAt - submittedAt) / 86_400_000))
  return `${durationDays} 天`
}
const delistStatusLabel = (value: number) =>
  delistStatuses.find((item) => item.value === value)?.label || '未申请'
const delistTagType = (value: number): 'warning' | 'success' | 'danger' | 'info' => {
  if (value === 1) return 'warning'
  if (value === 2) return 'success'
  if (value === 3) return 'danger'
  return 'info'
}
async function load() {
  loading.value = true
  try {
    const data = await AuctionApi.getPage(query)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function search() {
  query.pageNo = 1
  load()
}
function reset() {
  queryFormRef.value?.resetFields()
  search()
}
async function syncAll() {
  const count = await AuctionApi.syncAll()
  message.success(`已检查 ${count} 条到期竞拍`)
  load()
}
async function sync(row: Auction) {
  if (syncingIds.value.includes(row.id)) return
  syncingIds.value.push(row.id)
  try {
    await AuctionApi.sync(row.id)
    await load()
    const result = list.value.find((item) => item.id === row.id)
    if (result?.syncError?.includes('失败')) message.warning(result.syncError)
    else message.success(result?.syncError || '状态检查完成')
  } finally { syncingIds.value = syncingIds.value.filter((id) => id !== row.id) }
}
const bindDialogVisible = ref(false)
const bindingAuction = ref<Auction>()
const binding = ref(false)
const bindFormRef = ref()
const photosVisible = ref(false)
const jumpVisible = ref(false)
const jumpSaving = ref(false)
const jumpError = ref('')
const jumpForm = reactive({ id: 0, shareText: '' })
function openJump(row: Auction) {
  Object.assign(jumpForm, { id: row.id, shareText: row.shareText || '' })
  jumpError.value = ''
  jumpVisible.value = true
}
async function saveJump() {
  if (jumpSaving.value) return
  const data = { id: jumpForm.id, shareText: jumpForm.shareText.trim() }
  jumpError.value = ''
  const isMiniLink = /^#小程序:\/\/闲鱼\/[^\s]+$/.test(data.shareText)
  if (!isMiniLink && (data.shareText.includes('#小程序://') || !/https?:\/\/(?:m\.tb\.cn|(?:www\.|m\.)?goofish\.com)\/[^\s]+/.test(data.shareText))) {
    jumpError.value = '请填写闲鱼完整分享文案或完整的闲鱼小程序链接'
    return
  }
  jumpSaving.value = true
  try {
    await AuctionApi.updateShareText(data)
    message.success('原始分享文案已保存')
    jumpVisible.value = false
    await load()
  } finally { jumpSaving.value = false }
}
const photosAuction = ref<Auction>()
const photos = ref<string[]>([])
const photosBusy = ref(false)
const photosSaving = ref(false)
function openPhotos(row: Auction) {
  photosAuction.value = row
  photos.value = [...(row.displayPicUrls || [])]
  photosBusy.value = false
  photosVisible.value = true
}
async function savePhotos() {
  if (!photosAuction.value || photosBusy.value || photosSaving.value || !photos.value.length) return
  photosSaving.value = true
  try {
    await AuctionApi.updatePhotos(photosAuction.value.id, photos.value)
    message.success('拍卖展示照片已保存')
    photosVisible.value = false
    await load()
  } finally { photosSaving.value = false }
}
const bindForm = reactive({ goofishUrl: '', displayPicUrls: [] as string[] })
const bindRules = {
  displayPicUrls: [{ type: 'array', required: true, min: 1, max: 9, message: '请上传 1 至 9 张拍卖展示照片', trigger: 'change' }],
  goofishUrl: [{ required: true, message: '请输入闲鱼商品ID或含ID的链接', trigger: 'blur' }]
}
function openBind(row: Auction) {
  bindingAuction.value = row
  bindForm.goofishUrl = ''
  bindForm.displayPicUrls = [...(row.displayPicUrls || [])]
  photosBusy.value = false
  bindDialogVisible.value = true
  nextTick(() => bindFormRef.value?.clearValidate())
}
async function submitBind() {
  if (!bindingAuction.value || photosBusy.value || binding.value) return
  await bindFormRef.value?.validate()
  binding.value = true
  try {
    await AuctionApi.bindGoofish(bindingAuction.value.id, bindForm.goofishUrl.trim(), bindForm.displayPicUrls)
    message.success('闲鱼竞拍已接管，到期后请确认成交或流拍')
    bindDialogVisible.value = false
    load()
  } finally {
    binding.value = false
  }
}
async function rejectPublish(row: Auction) {
  const result = await message.prompt('请输入驳回原因（用户将在小程序中看到）', '驳回送拍申请')
  const reason = result.value?.trim()
  if (!reason) return message.warning('请填写驳回原因')
  await AuctionApi.rejectPublish(row.id, reason)
  message.success('已驳回并将藏品恢复给用户')
  load()
}
async function approveDelist(row: Auction) {
  await message.confirm(
    '请先在闲鱼人工下架该竞拍。确认闲鱼已下架后，系统将恢复藏品给原用户。是否继续？'
  )
  await AuctionApi.approveDelist(row.id)
  message.success('已同意下架，竞拍商品已移除')
  load()
}
async function rejectDelist(row: Auction) {
  const result = await message.prompt('请输入拒绝原因（用户将在小程序中看到）', '拒绝下架申请')
  const reason = result.value?.trim()
  if (!reason) return message.warning('请填写拒绝原因')
  await AuctionApi.rejectDelist(row.id, reason)
  message.success('已拒绝下架申请')
  load()
}
const saleDialogVisible = ref(false)
const saleAuction = ref<Auction>()
const saleSubmitting = ref(false)
const feeRate = ref<number>()
const saleForm = reactive({ grossYuan: 0, remark: '', checked: false })
const feeRateText = computed(() =>
  Number.isInteger(feeRate.value) ? `${feeRate.value}%（参数 auctionfee）` : '未配置，禁止确认'
)
const grossFen = computed(() => Math.round(Number(saleForm.grossYuan || 0) * 100))
const calculatedFee = computed(() =>
  Number.isInteger(feeRate.value) ? Math.round((grossFen.value * feeRate.value!) / 100) : 0
)
const calculatedIncome = computed(() => grossFen.value - calculatedFee.value)
async function openConfirmSale(row: Auction) {
  saleAuction.value = row
  saleForm.grossYuan = row.currentPrice / 100
  saleForm.remark = ''
  saleForm.checked = false
  const value = await ConfigApi.getConfigKey('auctionfee')
  const parsed = Number(value)
  feeRate.value = Number.isInteger(parsed) && parsed >= 0 && parsed <= 99 ? parsed : undefined
  saleDialogVisible.value = true
}
async function submitConfirmSale() {
  if (!saleAuction.value) return
  if (!Number.isInteger(feeRate.value)) return message.error('请先在参数配置中设置 auctionfee（0-99）')
  if (grossFen.value <= 0) return message.warning('请输入实际已付款金额')
  if (!saleForm.remark.trim()) return message.warning('请填写核对说明')
  await message.confirm(`确认将 ¥${(calculatedIncome.value / 100).toFixed(2)} 记入用户 ${saleAuction.value.sellerId} 的钱包？该操作不可重复。`)
  saleSubmitting.value = true
  try {
    await AuctionApi.confirmSale(saleAuction.value.id, grossFen.value, saleForm.remark.trim())
    message.success('成交已确认，卖家实收金额已进入钱包')
    saleDialogVisible.value = false
    load()
  } finally {
    saleSubmitting.value = false
  }
}
async function confirmUnsold(row: Auction) {
  await message.confirm(
    `请先在闲鱼确认“${row.collectionName}”没有成交。确认流拍后，藏品将恢复到用户仓库，且不会产生结算。是否继续？`,
    '确认流拍'
  )
  await AuctionApi.confirmUnsold(row.id)
  message.success('已确认流拍，藏品已恢复给用户')
  load()
}
onMounted(load)

useTodoFilter(query, load, { status: [0, 1, 2, 6], delistStatus: [1] })
</script>
