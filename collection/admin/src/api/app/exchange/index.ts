import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 兑换品信息 */
export interface Exchange {
          id: number; // 编号
          picUrl: string; // 图片
          name?: string; // 兑换品名称
          status?: number; // 状态
          stock: number; // 数量
  }

// 兑换品 API
export const ExchangeApi = {
  // 查询兑换品分页
  getExchangePage: async (params: any) => {
    return await request.get({ url: `/app/exchange/page`, params })
  },

  // 查询兑换品详情
  getExchange: async (id: number) => {
    return await request.get({ url: `/app/exchange/get?id=` + id })
  },

  // 新增兑换品
  createExchange: async (data: Exchange) => {
    return await request.post({ url: `/app/exchange/create`, data })
  },

  // 修改兑换品
  updateExchange: async (data: Exchange) => {
    return await request.put({ url: `/app/exchange/update`, data })
  },

  // 删除兑换品
  deleteExchange: async (id: number) => {
    return await request.delete({ url: `/app/exchange/delete?id=` + id })
  },

  /** 批量删除兑换品 */
  deleteExchangeList: async (ids: number[]) => {
    return await request.delete({ url: `/app/exchange/delete-list?ids=${ids.join(',')}` })
  },

  // 导出兑换品 Excel
  exportExchange: async (params) => {
    return await request.download({ url: `/app/exchange/export-excel`, params })
  },
}