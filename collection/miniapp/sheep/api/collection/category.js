import request from '@/sheep/request';

export default {
  getCollectionCategory: (params) => {
    return request({
      url: '/app/collection-category/list',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  getMyCollectionCategory: (params) => {
    return request({
      url: '/app/collection-category/my-list',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
		auth: 'nologin'
      },
    });
  },
  createCategory: (data) => {
    return request({
      url: '/app/collection-category/create',
      method: 'POST',
      data,
      custom: {
        showSuccess: false,
        successMsg: false,
      },
    });
  },
  getLatestPrice: (params) => {
    return request({
      url: '/app/collection-category/get-latest-price',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  getDealOrderList: (params) => {
    return request({
      url: '/app/deal-order/list',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  cancelBuy: (data) => {
    return request({
      url: '/app/buy-order/cancel',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        successMsg: '撤单成功，钱款已退回余额',
      },
    });
  },
  cancelSell: (data) => {
    return request({
      url: '/app/sell-order/cancel',
      method: 'POST',
      data,
      custom: {
        showSuccess: true,
        successMsg: '撤单成功',
      },
    });
  },
  deleteCategory: (params) => {
    return request({
      url: '/app/collection-category/delete',
      method: 'DELETE',
      params,
      custom: {
        showLoading: false,
        showError: true,
      },
    });
  },
  changeCategory: (data) => {
    return request({
      url: '/app/collection-category/change-category',
      method: 'PUT',
      data,
      custom: {
        showSuccess: true,
        successMsg: '类型修改成功',
      },
    });
  },
};
