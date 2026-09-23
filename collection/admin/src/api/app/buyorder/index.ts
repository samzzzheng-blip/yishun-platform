import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 批量交易买单信息 */
export interface BuyOrder {
  }

// 批量交易买单 API
export const BuyOrderApi = {
  // 查询批量交易买单分页
  getBuyOrderPage: async (params: any) => {
    return await request.get({ url: `/app/buy-order/page`, params })
  },

  // 查询批量交易买单详情
  getBuyOrder: async (id: number) => {
    return await request.get({ url: `/app/buy-order/get?id=` + id })
  },

  // 新增批量交易买单
  createBuyOrder: async (data: BuyOrder) => {
    return await request.post({ url: `/app/buy-order/create`, data })
  },

  // 修改批量交易买单
  updateBuyOrder: async (data: BuyOrder) => {
    return await request.put({ url: `/app/buy-order/update`, data })
  },

  // 删除批量交易买单
  deleteBuyOrder: async (id: number) => {
    return await request.delete({ url: `/app/buy-order/delete?id=` + id })
  },

  /** 批量删除批量交易买单 */
  deleteBuyOrderList: async (ids: number[]) => {
    return await request.delete({ url: `/app/buy-order/delete-list?ids=${ids.join(',')}` })
  },

  // 导出批量交易买单 Excel
  exportBuyOrder: async (params) => {
    return await request.download({ url: `/app/buy-order/export-excel`, params })
  },
}