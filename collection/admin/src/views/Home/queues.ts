export interface QueueDefinition {
  key: string
  title: string
  group: string
  description: string
  component: string
  query: Record<string, string>
  unit: string
  urgent?: boolean
}
const queue = (key: string, title: string, group: string, description: string, component: string, status: number, extra: Record<string, string> = {}, urgent = false, unit = '条'): QueueDefinition =>
  ({ key, title, group, description, component, query: { todo: key, status: String(status), ...extra }, urgent, unit })
export const queues: QueueDefinition[] = [
  queue('yikoujia-conflict', '双端成交冲突', '异常核查', '核对小程序与闲鱼成交记录，确认商品归属和后续处理。', 'app/yikoujia/index', 6, {}, true),
  queue('withdraw-failed', '提现失败', '异常核查', '查看转账失败原因并核对资金状态，避免重复付款。', 'mall/trade/brokerage/withdraw/index', 21, {}, true),
  queue('auction-failed', '竞拍发布失败', '异常核查', '检查失败原因，重新关联闲鱼商品或驳回申请。', 'app/auction/index', 6, {}, true),
  queue('collection-review', '藏品待审核', '入库审核', '核对藏品信息与实物；关联包裹的藏品请在包裹中审核。', 'app/collection/index', 0),
  queue('parcel-receive', '包裹待收货', '入库审核', '确认实物已送达后登记收货；未送达的包裹继续等待。', 'app/collection/index', 0, { panel: 'inbound', parcelStatus: '0' }, false, '个'),
  queue('parcel-inspect', '包裹待核对', '入库审核', '逐件核对已收货包裹，审核入库或处理异常件。', 'app/collection/index', 0, { panel: 'inbound', parcelStatus: '1' }, false, '个'),
  queue('getback-ship', '取回待发货', '发货处理', '核对藏品和收件信息，发货后填写快递单号。', 'app/getback/index', 0),
  queue('exchange-ship', '兑换待发货', '发货处理', '核对兑换内容并安排发货。', 'app/exchangelog/index', 0),
  queue('fasttrade-review', '快速变现待审核', '交易处理', '核对藏品和报价，确认是否接受变现申请。', 'app/fasttrade/index', 0),
  queue('yikoujia-publish', '一口价待上架', '交易处理', '核对价格、图片及描述后上架。', 'app/yikoujia/index', 0),
  queue('yikoujia-settle', '一口价待结算', '交易处理', '核对已售寄售商品，确认后结算给藏品持有人。', 'app/yikoujia/index', 4, { pendingSettlement: 'true' }),
  queue('auction-review', '竞拍待审核 / 发布', '交易处理', '审核送拍申请，关联闲鱼竞拍商品。', 'app/auction/index', 0),
  queue('auction-confirm', '竞拍待确认成交', '交易处理', '核实闲鱼最终成交结果，确认成交或流拍。', 'app/auction/index', 2),
  queue('auction-delist', '竞拍下架待审核', '交易处理', '核对用户的下架申请，批准或说明驳回原因。', 'app/auction/index', 1, { delistStatus: '1' }),
  queue('withdraw-review', '提现待审核', '资金审核', '核对提现申请及资金来源后审批。', 'mall/trade/brokerage/withdraw/index', 0)
]
export const groups = ['全部', '异常核查', '入库审核', '发货处理', '交易处理', '资金审核']
export function findMenuPath(menus: any[], component: string, parent = ''): string | undefined {
  for (const menu of menus || []) {
    const path = menu.path?.startsWith('/') ? menu.path : `${parent}/${menu.path || ''}`.replace(/\/+/g, '/')
    if (menu.component?.replace(/^\//, '').replace(/\.vue$/, '') === component) return path
    const child = findMenuPath(menu.children, component, path)
    if (child) return child
  }
}
export function todoFilters(query: Record<string, unknown>, allowed: Record<string, readonly (number | boolean)[]>) {
  if (typeof query.todo !== 'string') return {}
  const result: Record<string, number | boolean | undefined> = {}
  for (const [key, values] of Object.entries(allowed)) {
    const raw = query[key]
    const value = raw === 'true' ? true : raw === 'false' ? false : typeof raw === 'string' && /^\d+$/.test(raw) ? Number(raw) : undefined
    result[key] = value !== undefined && values.includes(value) ? value : undefined
  }
  return result
}

export function applyTodoQuery(params: Record<string, any>, query: Record<string, unknown>, allowed: Record<string, readonly (number | boolean)[]>) {
  if (typeof query.todo !== 'string') return false
  // A cached keyword/user/date filter must not hide rows behind the workbench count.
  for (const key of Object.keys(params)) if (key !== 'pageNo' && key !== 'pageSize') params[key] = undefined
  Object.assign(params, todoFilters(query, allowed), { pageNo: 1 })
  return true
}
