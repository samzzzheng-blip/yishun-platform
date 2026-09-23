import request from '@/sheep/request';
export function walletRecordQuery(params) {
  return Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');
}
export default {
  page: params => request({ url: `/app/wallet-record/page?${walletRecordQuery(params)}`, method: 'GET', custom: { showLoading: false } }),
  get: id => request({ url: '/app/wallet-record/get', method: 'GET', params: { id } }),
};
