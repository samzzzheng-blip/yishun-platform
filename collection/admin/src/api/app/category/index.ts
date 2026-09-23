import { getCategory } from '@/api/mall/product/category'
import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 藏品分类信息 */
export interface CollectionCategory {
  id: number // 分类编号
  name?: string // 分类名称
  userId: number // 自定义分类的用户id，系统自带的为0
}

// 藏品分类 API
export const CollectionCategoryApi = {
  // 查询藏品分类分页
  getCollectionCategoryPage: async (params: any) => {
    return await request.get({ url: `/app/collection-category/page`, params })
  },

  getCategoryList: async (id: number) => {
    return await request.get({ url: `/app/collection-category/list?id=` + id })
  },

  getCategoryListByUser: async (id: string) => {
    return await request.get({ url: `/app/collection-category/list-by-user?id=` + id })
  },

  // 查询藏品分类详情
  getCollectionCategory: async (id: number) => {
    return await request.get({ url: `/app/collection-category/get?id=` + id })
  },

  // 新增藏品分类
  createCollectionCategory: async (data: CollectionCategory) => {
    return await request.post({ url: `/app/collection-category/create`, data })
  },

  // 修改藏品分类
  updateCollectionCategory: async (data: CollectionCategory) => {
    return await request.put({ url: `/app/collection-category/update`, data })
  },

  // 删除藏品分类
  deleteCollectionCategory: async (id: number) => {
    return await request.delete({ url: `/app/collection-category/delete?id=` + id })
  },

  /** 批量删除藏品分类 */
  deleteCollectionCategoryList: async (ids: number[]) => {
    return await request.delete({
      url: `/app/collection-category/delete-list?ids=${ids.join(',')}`
    })
  },

  // 导出藏品分类 Excel
  exportCollectionCategory: async (params) => {
    return await request.download({ url: `/app/collection-category/export-excel`, params })
  }
}
