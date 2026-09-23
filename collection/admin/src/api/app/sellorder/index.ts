import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 批量交易卖单信息 */
export interface SellOrder {
  }

// 批量交易卖单 API
export const SellOrderApi = {
  // 查询批量交易卖单分页
  getSellOrderPage: async (params: any) => {
    return await request.get({ url: `/app/sell-order/page`, params })
  },

  // 查询批量交易卖单详情
  getSellOrder: async (id: number) => {
    return await request.get({ url: `/app/sell-order/get?id=` + id })
  },

  // 新增批量交易卖单
  createSellOrder: async (data: SellOrder) => {
    return await request.post({ url: `/app/sell-order/create`, data })
  },

  // 修改批量交易卖单
  updateSellOrder: async (data: SellOrder) => {
    return await request.put({ url: `/app/sell-order/update`, data })
  },

  // 删除批量交易卖单
  deleteSellOrder: async (id: number) => {
    return await request.delete({ url: `/app/sell-order/delete?id=` + id })
  },

  /** 批量删除批量交易卖单 */
  deleteSellOrderList: async (ids: number[]) => {
    return await request.delete({ url: `/app/sell-order/delete-list?ids=${ids.join(',')}` })
  },

  // 导出批量交易卖单 Excel
  exportSellOrder: async (params) => {
    return await request.download({ url: `/app/sell-order/export-excel`, params })
  },
}