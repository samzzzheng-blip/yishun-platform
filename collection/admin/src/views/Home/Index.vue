<template>
  <section class="workbench">
    <header class="workbench-header">
      <div><h1>待办工作台</h1><p>把需要人工处理的事项，集中在这里。</p></div>
      <div class="refresh-area">
        <span role="status">{{ updatedAt ? `更新于 ${updatedAt}` : loading ? '正在获取待办' : '尚未更新' }}</span>
        <el-button :loading="loading" @click="refresh"><Icon icon="ep:refresh" class="mr-1" />刷新待办</el-button>
      </div>
    </header>
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="mb-4" />
    <el-alert v-else-if="failedCount" :title="`${failedCount} 类事项暂时无法查询，请刷新重试；它们不计入已确认的待办。`" type="warning" show-icon :closable="false" class="mb-4" />
    <div class="queue-surface" :aria-busy="loading">
      <div class="queue-toolbar">
        <div class="queue-heading"><h2>处理清单</h2><span v-if="!loading && !error">{{ activeCount }} 类事项有待处理</span></div>
        <span class="read-hint">已读、已处理自动隐藏</span>
      </div>
      <div class="queue-filters" aria-label="待办分类">
        <button v-for="group in groups" :key="group" :class="{ selected: selectedGroup === group }" :aria-pressed="selectedGroup === group" @click="selectedGroup = group">{{ group }}</button>
      </div>
      <div v-if="loading && !loaded" class="queue-loading"><el-skeleton :rows="6" animated /></div>
      <el-empty v-else-if="!items.length && !error" description="当前账号暂无可查看的业务待办，请联系管理员配置业务权限。" />
      <el-empty v-else-if="!visibleRows.length && !error" description="当前分类没有未读待办" />
      <div v-else class="queue-list">
        <article v-for="item in visibleRows" :key="item.key" class="queue-row">
          <div class="queue-content">
            <div class="queue-title"><h3>{{ item.title }}</h3><el-tag v-if="item.urgent && item.count" type="danger" size="small" effect="plain">优先核查</el-tag></div>
            <p>{{ item.description }}</p>
          </div>
          <div class="queue-count" aria-live="polite">
            <template v-if="item.available"><strong :class="{ pending: item.count }">{{ item.count }}</strong><span>{{ item.unit }}</span></template>
            <span v-else class="queue-unavailable">查询失败</span>
          </div>
          <div class="queue-actions">
            <el-button :type="item.count ? 'primary' : 'default'" :plain="!item.urgent" :disabled="!item.path || loading || !!error" @click="openQueue(item)">{{ item.path ? '查看处理' : '未配置菜单' }}<Icon icon="ep:arrow-right" class="ml-1" /></el-button>
            <el-button link :disabled="!item.available || !item.count || loading || !!error || reading !== ''" :loading="reading === item.key" @click="readQueue(item)">标记本批已读</el-button>
          </div>
        </article>
      </div>
      <footer>仅显示当前账号未读且待处理的事项。标记已读会隐藏本批提醒，不改变业务状态；新增或更新的事项会再次显示。“查看处理”进入该类全部待处理记录。</footer>
    </div>
  </section>
</template>
<script setup lang="ts">
import { computed, onActivated, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CACHE_KEY, useCache } from '@/hooks/web/useCache'
import { getPending, markRead, type QueueCount } from '@/api/app/workbench'
import { queues, groups, findMenuPath, type QueueDefinition } from './queues'
defineOptions({ name: 'Index' })
const router = useRouter()
const { wsCache } = useCache()
const loading = ref(false), loaded = ref(false), error = ref(''), updatedAt = ref('')
const selectedGroup = ref('全部'), reading = ref('')
const counts = ref<QueueCount[]>([])
const items = computed(() => queues.flatMap(definition => {
  const count = counts.value.find(item => item.key === definition.key)
  return count ? [{ ...definition, ...count, path: findMenuPath(wsCache.get(CACHE_KEY.ROLE_ROUTERS) || [], definition.component) }] : []
}))
const failedCount = computed(() => items.value.filter(i => !i.available).length)
const activeCount = computed(() => items.value.filter(i => i.available && (i.count || 0) > 0).length)
const visibleRows = computed(() => items.value.filter(i => (selectedGroup.value === '全部' || i.group === selectedGroup.value) && (!i.available || (i.count || 0) > 0)))
let lastLoad = 0, timer: ReturnType<typeof setInterval> | undefined
async function refresh() {
  if (loading.value || reading.value) return
  loading.value = true
  error.value = ''
  try {
    counts.value = await getPending()
    updatedAt.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
    loaded.value = true
    lastLoad = Date.now()
  } catch { error.value = '待办获取失败，当前显示的数据可能已过期。请点击“刷新待办”重试。' }
  finally { loading.value = false }
}
async function readQueue(item: QueueCount) {
  if (reading.value || loading.value || !item.available) return
  reading.value = item.key
  try {
    await markRead(item.key, [...item.snapshot])
    reading.value = ''
    await refresh()
  } catch {
    error.value = '标记已读失败，请刷新后重试；未隐藏当前提醒。'
  } finally { reading.value = '' }
}
function openQueue(item: QueueDefinition & { path?: string }) {
  if (item.path) router.push({ path: item.path, query: item.query })
}
const refreshWhenVisible = () => {
  if (document.visibilityState === 'visible' && router.currentRoute.value.name === 'Index' && Date.now() - lastLoad > 30000) refresh()
}
onMounted(() => { refresh(); timer = setInterval(refreshWhenVisible, 60000); document.addEventListener('visibilitychange', refreshWhenVisible) })
onActivated(() => { if (loaded.value) refresh() })
onBeforeUnmount(() => { clearInterval(timer); document.removeEventListener('visibilitychange', refreshWhenVisible) })
</script>
<style scoped>
.workbench { max-width: 1200px; margin: 8px auto 32px; color: var(--el-text-color-primary); }
.workbench-header { display: flex; justify-content: space-between; align-items: center; gap: 24px; margin-bottom: 28px; }
h1 { font-size: 26px; line-height: 1.4; font-weight: 600; margin: 0 0 8px; }
.workbench-header p, .queue-content p { margin: 0; color: var(--el-text-color-regular); line-height: 1.6; }
.refresh-area { display: flex; align-items: center; gap: 16px; font-size: 13px; color: var(--el-text-color-regular); }
.queue-surface { background: var(--el-bg-color); border: 1px solid var(--el-border-color-light); border-radius: 8px; overflow: hidden; }
.queue-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 20px 24px 12px; }
.queue-heading { display: flex; align-items: baseline; gap: 16px; }
h2 { margin: 0; font-size: 17px; font-weight: 600; }
.queue-heading span { font-size: 13px; color: var(--el-text-color-regular); }
.queue-filters { display: flex; flex-wrap: wrap; gap: 8px; padding: 4px 24px 20px; border-bottom: 1px solid var(--el-border-color-light); }
.queue-filters button { border: 1px solid transparent; border-radius: 4px; background: transparent; color: var(--el-text-color-regular); padding: 7px 12px; font: inherit; font-size: 14px; cursor: pointer; }
.queue-filters button:hover { background: var(--el-fill-color-light); }
.queue-filters button.selected { background: var(--el-color-primary-light-9); color: var(--el-color-primary); border-color: var(--el-color-primary-light-7); }
.queue-filters button:focus-visible { outline: 2px solid var(--el-color-primary); outline-offset: 2px; }
.queue-row { display: grid; grid-template-columns: minmax(0, 1fr) 100px 124px; gap: 24px; align-items: center; padding: 20px 24px; border-bottom: 1px solid var(--el-border-color-lighter); }
.queue-title { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 5px; }
h3 { margin: 0; font-size: 15px; font-weight: 600; }
.queue-content p { font-size: 13px; }
.queue-actions { display: flex; flex-direction: column; align-items: stretch; gap: 10px; }
.queue-actions .el-button + .el-button { margin-left: 0; }
.read-hint { font-size: 13px; color: var(--el-text-color-regular); }
.queue-count { display: flex; justify-content: flex-end; align-items: baseline; gap: 6px; font-variant-numeric: tabular-nums; }
.queue-count strong { font-size: 22px; font-weight: 600; color: var(--el-text-color-secondary); }
.queue-count strong.pending { color: var(--el-text-color-primary); }
.queue-count span { font-size: 13px; color: var(--el-text-color-regular); }
.queue-count .queue-unavailable { color: var(--el-color-danger); }
footer { padding: 16px 24px; color: var(--el-text-color-regular); font-size: 12px; line-height: 1.6; }
.queue-loading { padding: 24px; }
@media (max-width: 640px) {
  .workbench-header { align-items: flex-start; flex-direction: column; gap: 14px; margin-bottom: 20px; }
  .refresh-area { width: 100%; justify-content: space-between; }
  .queue-toolbar { padding: 16px; align-items: flex-start; }
  .queue-heading { flex-direction: column; gap: 4px; }
  .queue-filters { padding: 0 12px 12px; gap: 4px; }
  .queue-row { grid-template-columns: minmax(0, 1fr) auto; padding: 16px; gap: 12px; }
  .queue-content { grid-column: 1 / -1; }
  .queue-count { justify-content: flex-start; }
  footer { padding: 16px; }
}
</style>
