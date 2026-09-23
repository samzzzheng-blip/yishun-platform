import request from '@/sheep/request';

export default {
	submit: (data) => {
	  return request({
	    url: '/app/transfer/create',
	    method: 'POST',
	    data,
	    custom: {
	      showSuccess: true,
	      successMsg: '转移成功',
	    },
	  });
	},
	searchUser: (params) => {
		return request({
		  url: '/app/transfer/search-user',
		  method: 'GET',
		  params,
		  custom: {
		    showSuccess: false,
		    successMsg: '',
		  },
		});
	},
	getBack: (data) => {
		return request({
		  url: '/app/getback/create',
		  method: 'POST',
		  data,
		  custom: {
		    showSuccess: true,
		    successMsg: '提交成功',
		  },
		});
	},
	getDeliver: (params) => {
		return request({
		  url: '/app/getback/get-deliver',
		  method: 'GET',
		  params,
		  custom: {
		    showSuccess: false,
		    successMsg: '',
		  },
		});
	},
	updateDeliver: (data) => {
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
}