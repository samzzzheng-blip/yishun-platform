import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 兑换记录信息 */
export interface ExchangeLog {
  id: number // 编号
  exchangeId?: number // 兑换品id
  userId?: number // 用户id
  exchangeName?: string // 兑换品名称
  amount?: number // 兑换数量
  status?: number // 状态
  mobile?: string // 手机号
}

// 兑换记录 API
export const ExchangeLogApi = {
  // 查询兑换记录分页
  getExchangeLogPage: async (params: any) => {
    return await request.get({ url: `/app/exchange-log/page`, params })
  },

  // 查询兑换记录详情
  getExchangeLog: async (id: number) => {
    return await request.get({ url: `/app/exchange-log/get?id=` + id })
  },

  // 新增兑换记录
  createExchangeLog: async (data: ExchangeLog) => {
    return await request.post({ url: `/app/exchange-log/create`, data })
  },

  // 修改兑换记录
  updateExchangeLog: async (data: ExchangeLog) => {
    return await request.put({ url: `/app/exchange-log/update`, data })
  },

  // 删除兑换记录
  deleteExchangeLog: async (id: number) => {
    return await request.delete({ url: `/app/exchange-log/delete?id=` + id })
  },

  /** 批量删除兑换记录 */
  deleteExchangeLogList: async (ids: number[]) => {
    return await request.delete({ url: `/app/exchange-log/delete-list?ids=${ids.join(',')}` })
  },

  // 导出兑换记录 Excel
  exportExchangeLog: async (params) => {
    return await request.download({ url: `/app/exchange-log/export-excel`, params })
  }
}
