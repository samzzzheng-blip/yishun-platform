import request from '@/sheep/request';
const call = (path, method = 'GET', data = {}) =>
  request({
    url: '/app/inbound-parcel/' + path,
    method,
    ...(method === 'GET' ? { params: data } : { data }),
    custom: { showLoading: false, showError: path !== 'config', auth: true },
  });
export default {
  config: () => call('config'),
  list: (page = 1) => call('list', 'GET', { page }),
  states: () => call('collection-states'),
  detail: (id) => call('get', 'GET', { id }),
  available: (parcelId) => call('available', 'GET', parcelId ? { parcelId } : {}),
  save: (data) => call('save', 'POST', data),
  cancel: (id) => call('cancel?id=' + encodeURIComponent(id), 'POST'),
};
