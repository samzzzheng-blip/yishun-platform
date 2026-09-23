import request from '@/sheep/request';

export default {
  getPage: (params) => {
    return request({
      url: '/app/ykj-order/page',
      method: 'GET',
      params,
      custom: {
        auth: true,
        showLoading: false,
        showError: false,
      },
    });
  },
};
