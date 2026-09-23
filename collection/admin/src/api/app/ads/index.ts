import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 广告信息 */
export interface Ads {
          id: number; // 编号
          picUrl: string; // 图片地址
          status?: number; // 状态
          targetProductId?: number | null;
          targetProductName?: string;
  }

// 广告 API
export const AdsApi = {
  getProductOptions: async (params: { keyword: string; pageNo: number; pageSize: number }) => {
    return await request.get({ url: '/app/ads/product-options', params })
  },
  // 查询广告分页
  getAdsPage: async (params: any) => {
    return await request.get({ url: `/app/ads/page`, params })
  },

  // 查询广告详情
  getAds: async (id: number) => {
    return await request.get({ url: `/app/ads/get?id=` + id })
  },

  // 新增广告
  createAds: async (data: Ads) => {
    return await request.post({ url: `/app/ads/create`, data })
  },

  // 修改广告
  updateAds: async (data: Ads) => {
    return await request.put({ url: `/app/ads/update`, data })
  },

  // 删除广告
  deleteAds: async (id: number) => {
    return await request.delete({ url: `/app/ads/delete?id=` + id })
  },

  /** 批量删除广告 */
  deleteAdsList: async (ids: number[]) => {
    return await request.delete({ url: `/app/ads/delete-list?ids=${ids.join(',')}` })
  },

  // 导出广告 Excel
  exportAds: async (params) => {
    return await request.download({ url: `/app/ads/export-excel`, params })
  },
}