import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 快速变现信息 */
export interface FastTrade {
  id: number // 编号
  userId?: number // 用户id
  collectionId?: number // 藏品id
  amount?: number // 数量
  status?: number // 状态
}

// 快速变现 API
export const FastTradeApi = {
  // 查询快速变现分页
  getFastTradePage: async (params: any) => {
    return await request.get({ url: `/app/fast-trade/page`, params })
  },

  // 查询快速变现详情
  getFastTrade: async (id: number) => {
    return await request.get({ url: `/app/fast-trade/get?id=` + id })
  },

  // 新增快速变现
  createFastTrade: async (data: FastTrade) => {
    return await request.post({ url: `/app/fast-trade/create`, data })
  },

  // 修改快速变现
  updateFastTrade: async (data: FastTrade) => {
    return await request.put({ url: `/app/fast-trade/update`, data })
  },

  // 删除快速变现
  deleteFastTrade: async (id: number) => {
    return await request.delete({ url: `/app/fast-trade/delete?id=` + id })
  },

  /** 批量删除快速变现 */
  deleteFastTradeList: async (ids: number[]) => {
    return await request.delete({ url: `/app/fast-trade/delete-list?ids=${ids.join(',')}` })
  },

  // 导出快速变现 Excel
  exportFastTrade: async (params) => {
    return await request.download({ url: `/app/fast-trade/export-excel`, params })
  }
}
