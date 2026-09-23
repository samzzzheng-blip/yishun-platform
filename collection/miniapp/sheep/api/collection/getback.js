import request from '@/sheep/request';

export default {
  getLogistics: (id) => request({ url: '/app/getback/logistics', method: 'GET', params: { id }, custom: { showLoading: false, showError: false } }),
  getGetbackList: (params) => {
    return request({
      url: '/app/getback/list',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  confirmDeliver: (data) => {
    return request({
      url: '/app/getback/update',
      method: 'PUT',
      data,
      custom: {
        showSuccess: false,
        successMsg: '',
      },
    });
  },
};