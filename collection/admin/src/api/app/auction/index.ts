import request from '@/config/axios'

export interface Auction {
  id: number
  sellerId: number
  collectionId: number
  collectionName: string
  categoryName?: string
  picUrl?: string
  picUrls?: string[]
  displayPicUrls?: string[]
  currentPrice: number
  startPrice: number
  minIncrement: number
  bidCount: number
  goofishProductId?: string
  goofishManagedProductId?: string
  goofishDetail?: string
  goofishUrl?: string
  shareText?: string
  syncError?: string
  lastSyncTime?: string
  status: number
  reviewTime?: string
  reviewRejectReason?: string
  delistStatus: number
  delistApplyTime?: number
  delistAuditTime?: number
  delistRejectReason?: string
  endTime: string
  settlementStatus?: number
  grossAmount?: number
  feeRate?: number
  feeAmount?: number
  sellerIncome?: number
  createTime: string
}

export const AuctionApi = {
  bindManagedProduct: (id: number, productId: string) =>
    request.post({ url: '/app/auction/bind-managed-product', params: { id, productId } }),
  updateShareText: (data: { id: number; shareText: string }) =>
    request.post({ url: '/app/auction/share-text', data }),
  getPage: (params: any) => request.get({ url: '/app/auction/page', params }),
  syncAll: () => request.post({ url: '/app/auction/close-expired' }),
  sync: (id: number) => request.post({ url: '/app/auction/sync', params: { id } }),
  bindGoofish: (id: number, goofishUrl: string, displayPicUrls: string[]) =>
    request.post({ url: '/app/auction/bind-goofish', data: { id, goofishUrl, displayPicUrls } }),
  updatePhotos: (id: number, displayPicUrls: string[]) =>
    request.post({ url: '/app/auction/display-photos', data: { id, displayPicUrls } }),
  rejectPublish: (id: number, reason: string) =>
    request.post({ url: '/app/auction/reject-publish', data: { id, reason } }),
  approveDelist: (id: number) =>
    request.post({ url: '/app/auction/approve-delist', params: { id } }),
  rejectDelist: (id: number, reason: string) =>
    request.post({ url: '/app/auction/reject-delist', data: { id, reason } }),
  confirmSale: (id: number, grossAmount: number, remark: string) =>
    request.post({ url: '/app/auction/confirm-sale', data: { id, grossAmount, remark } }),
  confirmUnsold: (id: number) =>
    request.post({ url: '/app/auction/confirm-unsold', params: { id } })
}
