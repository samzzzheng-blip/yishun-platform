import request from '@/config/axios'

/** 藏品变更记录信息 */
export interface CollectionRecord {
  id: number
  userId?: number
  mobile?: string
  categoryId?: number
  categoryName?: string
  amount?: number
  type?: number
  createTime?: string
}

// 藏品变更记录 API
export const CollectionRecordApi = {
  // 查询藏品变更记录分页
  getCollectionRecordPage: async (params: any) => {
    return await request.get({ url: `/app/collection-record/page`, params })
  }
}