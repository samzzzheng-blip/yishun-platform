import request from '@/config/axios'
export interface QueueCount { key: string; count: number | null; available: boolean; snapshot: string[] }
export const getPending = (): Promise<QueueCount[]> => request.get({ url: '/app/workbench/pending' })
export const markRead = (key: string, snapshot: string[]) => request.post({ url: '/app/workbench/read', data: { key, snapshot } })
