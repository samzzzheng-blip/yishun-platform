import { watch } from 'vue'
import { useRoute } from 'vue-router'
import { applyTodoQuery } from '@/views/Home/queues'
/** Apply workbench filters on first entry and when revisiting a cached list. */
export function useTodoFilter(params: Record<string, any>, reload: () => unknown, allowed: Record<string, readonly (number | boolean)[]>) {
  const route = useRoute()
  const ownName = route.name
  const apply = () => {
    if (route.name !== ownName || typeof route.query.todo !== 'string') return false
    return applyTodoQuery(params, route.query, allowed)
  }
  apply()
  watch(() => route.fullPath, () => { if (apply()) reload() })
}
