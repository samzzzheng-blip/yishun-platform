import request from '@/sheep/request';

export default {
  deleteRegistration: (id) => request({ url: '/app/collection/delete', method: 'DELETE', params: { id } }),
  createCollection: (data) => {
    return request({
      url: '/app/collection/create',
      method: 'POST',
      data,
      custom: {
        showLoading: true,
      },
    });
  },
  getCollection: (params) => {
    return request({
      url: '/app/collection/list',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  getAdsList: (params) => {
    return request({
      url: '/app/ads/list',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  exchangeStone: (data) => {
    return request({
      url: '/app/stone-exchange/create',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        successMsg: '兑换成功',
      },
    });
  },
  getConfigs: (params) => {
	  return request({
	    url: '/infra/config/page',
	    method: 'GET',
	    params,
	    custom: {
	      showLoading: false,
	      showError: false,
	    },
	  });
  },
  getCollectionById:(params) => {
    return request({
	    url: '/app/collection/get',
	    method: 'GET',
	    params,
	    custom: {
	      showLoading: false,
	      showError: false,
	    },
	  });
  },
  updateCollection: (data) => {
    return request({
      url: '/app/collection/update',
      method: 'PUT',
      data,
      custom: {
        showSuccess: true,
        successMsg: '提交成功',
      },
    });
  },
  getStorageCapacity: (params) => {
    return request({
      url: '/app/collection/storage-capacity',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  }
};
