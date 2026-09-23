import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 藏品登记信息 */
export interface Collection {
  id: number // 藏品编号
  name?: string // 品名
  userId?: number // 用户id
  categoryId?: number // 分类id
  picUrl?: string // 图片地址
  status?: number // 状态
  stock?: number // 数量
}

// 藏品登记 API
export const CollectionApi = {
  // 查询藏品登记分页
  getCollectionPage: async (params: any) => {
    return await request.get({ url: `/app/collection/page`, params })
  },

  // 查询藏品登记详情
  getCollection: async (id: number) => {
    return await request.get({ url: `/app/collection/get?id=` + id })
  },

  // 新增藏品登记
  createCollection: async (data: Collection) => {
    return await request.post({ url: `/app/collection/create`, data })
  },

  // 修改藏品登记
  updateCollection: async (data: Collection) => {
    return await request.put({ url: `/app/collection/update`, data })
  },

  // 审核藏品登记
  auditCollection: async (data: Collection) => {
    return await request.put({ url: `/app/collection/audit`, data })
  },

  // 删除藏品登记
  deleteCollection: async (id: number) => {
    return await request.delete({ url: `/app/collection/delete?id=` + id })
  },

  /** 批量删除藏品登记 */
  deleteCollectionList: async (ids: number[]) => {
    return await request.delete({ url: `/app/collection/delete-list?ids=${ids.join(',')}` })
  },

  // 导出藏品登记 Excel
  exportCollection: async (params) => {
    return await request.download({ url: `/app/collection/export-excel`, params })
  }
}
