import request from '@/sheep/request';

export default {
  getPage: (params) =>
    request({ url: '/app/auction/page', method: 'GET', params, custom: { showLoading: false } }),
  getDetail: (params) => request({ url: '/app/auction/get', method: 'GET', params }),
  getMyPage: (params) => request({ url: '/app/auction/my-page', method: 'GET', params }),
  create: (data) =>
    request({
      url: '/app/auction/create',
      method: 'POST',
      data,
      custom: { showSuccess: true, successMsg: '送拍申请已提交，等待人工发布' },
    }),
  cancel: (id) =>
    request({
      url: '/app/auction/cancel',
      method: 'DELETE',
      params: { id },
      custom: { showSuccess: true, successMsg: '已取消' },
    }),
  requestDelist: (id) =>
    request({
      url: '/app/auction/request-delist',
      method: 'POST',
      params: { id },
      custom: { showSuccess: true, successMsg: '下架申请已提交' },
    }),
};
