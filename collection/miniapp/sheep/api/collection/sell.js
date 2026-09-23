import request from '@/sheep/request';

export default {
  createYikoujia: (data) => {
    return request({
      url: '/app/yikoujia/create',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        successMsg: '提交成功',
      },
    });
  },
  createFastTrade: (data) => {
    return request({
      url: '/app/fast-trade/create',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        successMsg: '提交成功',
      },
    });
  },
  getUserTradeInfo: (params) => {
    return request({
      url: '/app/sell-order/get-user-trade-info',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
		auth: 'nologin'
      },
    });
  },
  getGoodPage: (params) => {
    return request({
      url: '/app/yikoujia/page',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  getMySellList: (params) => {
    return request({
      url: '/app/yikoujia/my-list',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  delistYikoujia: (id) => {
    return request({
      url: '/app/yikoujia/delist',
      method: 'POST',
      params: { id },
      custom: {
        showLoading: false,
        showError: true,
      },
    });
  },
  getGoodDetail: (params) => {
    return request({
      url: '/app/yikoujia/get',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  createYkjOrder: (data) => {
    return request({
      url: '/app/ykj-order/create',
      method: 'POST',
      data,
      custom: {
        showSuccess: false,
        showError: true,
      },
    });
  },
};
