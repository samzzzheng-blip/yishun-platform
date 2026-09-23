import request from '@/config/axios'

export interface StoragePlan {
  id: number
  minCount: number
  maxCount: number
  monthlyPrice: number
  yearlyPrice: number
  createTime?: string
  updateTime?: string
}

export const StoragePlanApi = {
  getStoragePlanPage: async (params: any) => {
    return await request.get({ url: `/app/storage-plan/list`, params })
  },

  getStoragePlan: async (id: number) => {
    return await request.get({ url: `/app/storage-plan/get?id=` + id })
  },

  createStoragePlan: async (data: StoragePlan) => {
    return await request.post({ url: `/app/storage-plan/add`, data })
  },

  updateStoragePlan: async (data: StoragePlan) => {
    return await request.post({ url: `/app/storage-plan/update`, data })
  },

  deleteStoragePlan: async (id: number) => {
    return await request.post({ url: `/app/storage-plan/delete`, data: id })
  }
}