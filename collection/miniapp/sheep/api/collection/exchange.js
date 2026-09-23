import request from '@/sheep/request';

export default {
  exchange: (data) => {
    return request({
      url: '/app/exchange-log/create',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        successMsg: '兑换成功',
      },
    });
  },
  getExchangeList: (params) => {
    return request({
      url: '/app/exchange/list',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  getExchange: (params) => {
    return request({
      url: '/app/exchange/get',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  getExchangeLog: (params) => {
    return request({
      url: '/app/exchange-log/page',
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
		  url: '/app/exchange-log/update',
		  method: 'PUT',
		  data,
		  custom: {
		    showSuccess: false,
		    successMsg: '',
		  },
		});
	},
	getBuyOrder: (params) => {
	  return request({
	    url: '/app/buy-order/page',
	    method: 'GET',
	    params,
	    custom: {
	      showLoading: false,
	      showError: false,
	    },
	  });
	},
	getSellOrder: (params) => {
	  return request({
	    url: '/app/sell-order/page',
	    method: 'GET',
	    params,
	    custom: {
	      showLoading: false,
	      showError: false,
	    },
	  });
	},
};
