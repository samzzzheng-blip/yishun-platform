import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

export interface GetbackItem {
  id: number
  name?: string
  picUrl?: string
  picUrls?: string[]
}

/** 取回信息 */
export interface Getback {
  id: number // 编号
  userId?: number // 用户id
  collectionId?: number // 藏品id
  amount?: number // 数量
  status?: number // 状态
  expressCompany?: string
  deliverCode?: string
  receiverName?: string // 收件人名称
  receiverMobile?: string // 收件人手机
  receiverAreaId?: number // 收件人地区编号
  receiverDetailAddress?: string // 收件人详细地址
  items?: GetbackItem[] // 取回的藏品
}

// 取回 API
export const GetbackApi = {
  // 查询取回分页
  getGetbackPage: async (params: any) => {
    return await request.get({ url: `/app/getback/page`, params })
  },

  // 查询取回详情
  getGetback: async (id: number) => {
    return await request.get({ url: `/app/getback/get?id=` + id })
  },

  // 新增取回
  createGetback: async (data: Getback) => {
    return await request.post({ url: `/app/getback/create`, data })
  },

  // 修改取回
  updateGetback: async (data: Getback) => {
    return await request.put({ url: `/app/getback/update`, data })
  },

  // 取消尚未发货的取回申请，并恢复藏品库存
  cancelGetback: async (id: number) => {
    return await request.put({ url: `/app/getback/cancel?id=${id}` })
  },

  // 删除取回
  deleteGetback: async (id: number) => {
    return await request.delete({ url: `/app/getback/delete?id=` + id })
  },

  /** 批量删除取回 */
  deleteGetbackList: async (ids: number[]) => {
    return await request.delete({ url: `/app/getback/delete-list?ids=${ids.join(',')}` })
  },

  // 导出取回 Excel
  exportGetback: async (params) => {
    return await request.download({ url: `/app/getback/export-excel`, params })
  }
}
