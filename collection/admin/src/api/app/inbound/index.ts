import request from '@/config/axios'
export const reopen = (parcelId: number, collectionId: number) =>
  request.post({ url: '/app/inbound-parcel/reopen', params: { parcelId, collectionId } })
export const config = () => request.get({ url: '/app/inbound-parcel/config' })
export const list = (params: { tracking?: string; page: number; status?: number }) =>
  request.get({ url: '/app/inbound-parcel/list', params })
export const detail = (id: number) =>
  request.get({ url: '/app/inbound-parcel/get', params: { id } })
export const receive = (id: number) =>
  request.post({ url: '/app/inbound-parcel/receive', params: { id } })
export const correct = (data: {
  id: number
  version: number
  carrier: string
  trackingNo: string
  reason: string
}) => request.post({ url: '/app/inbound-parcel/correct', data })
export const inspect = (data: {
  parcelId: number
  collectionId: number
  status: number
  note: string
  evidence: string
}) => request.post({ url: '/app/inbound-parcel/inspect', data })
