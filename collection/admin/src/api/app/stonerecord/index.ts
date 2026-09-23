import request from '@/config/axios'

export interface StoneRecord {
  id: number
  userId: number
  mobile: string
  amount: number
  type: number
  createTime: string
  updateTime: string
}

export const StoneRecordApi = {
  getStoneRecordPage: async (params: any) => {
    return await request.get({ url: `/app/stone-record/list`, params })
  }
}
