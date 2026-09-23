import request from '@/sheep/request';

export default {
  getStoragePlanList: (params) => {
    return request({
      url: '/app/storage-plan/list',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
      },
    });
  },
  purchaseStoragePlan: (data) => {
    return request({
      url: '/app/storage-plan/purchase',
      method: 'POST',
      data,
      custom: {
        showLoading: true,
        showSuccess: true,
        successMsg: '购买成功',
      },
    });
  },
  getStoragePlan: (params) => {
    return request({
      url: '/app/storage-plan/get',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
};
