import request from '@/sheep/request';

export default {
  createBuyOrder: (data) => {
    return request({
      url: '/app/buy-order/create',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        successMsg: '提交成功',
      },
    });
  },
  getTradeInfo: (params) => {
    return request({
      url: '/app/collection-category/get-trade-info',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  getNotify: (params) => {
	  return request({
	    url: '/system/notify-message/my-page',
	    method: 'GET',
	    params,
	    custom: {
	      showLoading: false,
	      showError: false,
		  auth: 'nologin'
	    },
	  });
  },
  readMessages: (ids) => request({
    url: '/system/notify-message/update-read',
    method: 'PUT',
    params: { ids: ids.join(',') },
    custom: { showLoading: false, showSuccess: false, showError: false },
  }),
  readAll: (data) => {
  	return request({
  	  url: '/system/notify-message/update-all-read',
  	  method: 'PUT',
  	  data,
  	  custom: {
  	    showSuccess: false,
  	    successMsg: '',
  	  },
  	});
  },
  
};
