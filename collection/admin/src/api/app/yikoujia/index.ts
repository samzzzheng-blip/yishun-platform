import request from '@/config/axios'

/** 一口价信息 */
export interface Yikoujia {
  id: number
  status: number
  name?: string
  userId?: number
  price?: number
  productId?: string
  collectionId?: number
  picUrl?: string[]
  createTime?: string
  saleChannel?: 'MINIAPP' | 'GOOFISH' | 'CONFLICT'
  soldAt?: string
  goofishStatus?: number
  lastSyncTime?: string
  syncRemark?: string
}

export interface GoofishImportPreview {
  productId: string
  title?: string
  price: number
  stock: number
  content?: string
  images: string[]
  sellerName?: string
  productStatus: number
  localStatus: number
  alreadyImported: boolean
}

export interface PageResult<T> {
  list: T[]
  total: number
}

const isLocalPreview = import.meta.env.DEV && import.meta.env.VITE_LOCAL_PREVIEW === 'true'

let localPreviewRows: Yikoujia[] = [
  {
    id: 663,
    status: 0,
    name: '本地预览 · 待上架藏品',
    userId: 674,
    price: 20000,
    productId: 'preview-663',
    picUrl: [],
    createTime: '2026-08-17 15:19:44'
  },
  {
    id: 661,
    status: 3,
    name: '本地预览 · 闲鱼在售商品',
    userId: 515,
    price: 10000,
    productId: '220656347074629',
    picUrl: [],
    createTime: '2026-08-16 02:30:51'
  }
]

const previewPage = (params: any) => {
  const keyword = String(params?.keyword || '').trim()
  const status = params?.status
  const rows = localPreviewRows.filter(
    (item) =>
      (!keyword || item.name?.includes(keyword) || item.productId?.includes(keyword)) &&
      (status === undefined || status === null || status === '' || item.status === Number(status))
  )
  return { list: rows, total: rows.length }
}

// 一口价 API
export const YikoujiaApi = {
  updateImageOrder: async (id: number, originalPicUrl: string[], picUrl: string[]) => {
    if (isLocalPreview) {
      localPreviewRows = localPreviewRows.map(item => item.id === id ? { ...item, picUrl } : item)
      return true
    }
    return await request.put({ url: '/app/yikoujia/image-order', data: { id, originalPicUrl, picUrl } })
  },
  // 查询一口价分页
  getYikoujiaPage: async (params: any) => {
    if (isLocalPreview) return previewPage(params)
    return await request.get({ url: `/app/yikoujia/page`, params })
  },

  // 查询一口价详情
  getYikoujia: async (id: number) => {
    if (isLocalPreview) return localPreviewRows.find((item) => item.id === id)
    return await request.get({ url: `/app/yikoujia/get?id=` + id })
  },

  // 新增一口价
  createYikoujia: async (data: Yikoujia) => {
    if (isLocalPreview) {
      localPreviewRows.unshift({ ...data, id: Date.now(), picUrl: data.picUrl || [] })
      return localPreviewRows[0].id
    }
    return await request.post({ url: `/app/yikoujia/create`, data })
  },

  // 修改一口价
  updateYikoujia: async (data: Yikoujia) => {
    if (isLocalPreview) {
      localPreviewRows = localPreviewRows.map((item) =>
        item.id === data.id ? { ...item, ...data } : item
      )
      return true
    }
    return await request.put({ url: `/app/yikoujia/update`, data })
  },

  previewGoofishProduct: async (source: string) => {
    if (isLocalPreview) {
      const productId = source.match(/\d{8,}/)?.[0] || '220656347074629'
      return {
        productId,
        title: '本地预览 · 闲鱼导入商品',
        price: 19900,
        stock: 1,
        content: '这是本机界面模拟数据，不会读取或修改服务器数据库。',
        images: [],
        sellerName: '本地预览账号',
        productStatus: 22,
        localStatus: 3,
        alreadyImported: localPreviewRows.some((item) => item.productId === productId)
      }
    }
    return await request.post<GoofishImportPreview>({
      url: `/app/yikoujia/import-preview`,
      data: { source }
    })
  },

  getGoofishProducts: async (params: { pageNo: number; pageSize: number; source?: string }) => {
    if (isLocalPreview) {
      const productId = params.source?.match(/\d{8,20}/)?.[0]
      const matches = !params.source || productId === '220656347074629'
      return {
        list: matches ? [
          {
            productId: '220656347074629',
            title: '本地预览 · 已授权店铺商品',
            price: 19900,
            stock: 1,
            content: '本地界面模拟数据',
            images: [],
            sellerName: '一瞬签名会',
            productStatus: 22,
            localStatus: 3,
            alreadyImported: false
          }
        ] : [],
        total: matches ? 1 : 0
      } as PageResult<GoofishImportPreview>
    }
    return await request.get<PageResult<GoofishImportPreview>>({
      url: `/app/yikoujia/goofish-products`,
      params
    })
  },

  importGoofishProduct: async (data: { source: string; selfOperated: boolean; userId?: number }) => {
    if (isLocalPreview) {
      const productId = data.source.match(/\d{8,}/)?.[0] || `preview-${Date.now()}`
      const id = Date.now()
      localPreviewRows.unshift({
        id,
        status: 3,
        name: '本地预览 · 新导入商品',
        userId: data.userId,
        price: 19900,
        productId,
        picUrl: [],
        createTime: new Date().toLocaleString('zh-CN', { hour12: false })
      })
      return id
    }
    return await request.post<number>({ url: `/app/yikoujia/import-confirm`, data })
  },

  // 删除一口价
  deleteYikoujia: async (id: number) => {
    if (isLocalPreview) {
      localPreviewRows = localPreviewRows.filter((item) => item.id !== id)
      return true
    }
    return await request.delete({ url: `/app/yikoujia/delete?id=` + id })
  },

  /** 批量删除一口价 */
  deleteYikoujiaList: async (ids: number[]) => {
    if (isLocalPreview) {
      localPreviewRows = localPreviewRows.filter((item) => !ids.includes(item.id))
      return true
    }
    return await request.delete({ url: `/app/yikoujia/delete-list?ids=${ids.join(',')}` })
  },

  // 导出一口价 Excel
  exportYikoujia: async (params) => {
    return await request.download({ url: `/app/yikoujia/export-excel`, params })
  },

  // 测试
  test: async (params: any) => {
    return await request.get({ url: `/app/yikoujia/test`, params })
  }
}
