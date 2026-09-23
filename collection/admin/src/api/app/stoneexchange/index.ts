import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 能量石兑换信息 */
export interface StoneExchange {
  id: number // 编号
  userId?: number // 用户id
}

// 能量石兑换 API
export const StoneExchangeApi = {
  addStone: async (data: any) => {
    return await request.post({ url: `/app/stone-exchange/add`, data })
  },
  // 查询能量石兑换分页
  getStoneExchangePage: async (params: any) => {
    return await request.get({ url: `/app/stone-exchange/page`, params })
  },

  // 查询能量石兑换详情
  getStoneExchange: async (id: number) => {
    return await request.get({ url: `/app/stone-exchange/get?id=` + id })
  },

  // 新增能量石兑换
  createStoneExchange: async (data: StoneExchange) => {
    return await request.post({ url: `/app/stone-exchange/create`, data })
  },

  // 修改能量石兑换
  updateStoneExchange: async (data: StoneExchange) => {
    return await request.put({ url: `/app/stone-exchange/update`, data })
  },

  // 删除能量石兑换
  deleteStoneExchange: async (id: number) => {
    return await request.delete({ url: `/app/stone-exchange/delete?id=` + id })
  },

  /** 批量删除能量石兑换 */
  deleteStoneExchangeList: async (ids: number[]) => {
    return await request.delete({ url: `/app/stone-exchange/delete-list?ids=${ids.join(',')}` })
  },

  // 导出能量石兑换 Excel
  exportStoneExchange: async (params) => {
    return await request.download({ url: `/app/stone-exchange/export-excel`, params })
  }
}
