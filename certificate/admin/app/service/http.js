import { message } from 'antd';

export const commonParams = {}
import axios from 'axios'
import api from './api';
import { browserHistory } from 'react-router'

export const SMART_TOKEN = 'smart-token'
export const SMART_PHONE = 'smart-phone'
export const ADMIN_TOKEN = 'token'
const WINDOW_TYPE_ADMIN = 'administrator'
const WINDOW_TYPE_SMART = 'smart'

const commonHeaders = {
  'Content-Type': 'application/json; charset=UTF-8',
  Accept: 'application/json',
}

const instance = axios.create({
  timeout: 30000,
  withCredentials: false,
  Access: true,
});

/**
 * 拦截响应response，并做一些错误处理
 */
instance.interceptors.response.use((response) => {
  if (response.data) {
    if (response.data.status) {
      if (response.data.status === 'done') {
        // 图片上传处理
        return Promise.resolve(response.data.url)
      }
      return Promise.reject('上传失败')
    }
    if (response.data.code === 200) {
      return Promise.resolve(response.data);
    }
    if (response.data.code === 401) {
      // 未登录
      if (window.type === WINDOW_TYPE_ADMIN) {
        if(window.location.pathname!=='/login') {
          const next=/^\/rate(Manage|Workflow|Intake)$/.test(window.location.pathname)?'?returnTo='+encodeURIComponent(window.location.pathname+window.location.search):''
          browserHistory.replace('/login'+next);
        }
        localStorage.setItem(ADMIN_TOKEN, '');
      } else if (window.type === WINDOW_TYPE_SMART) {
        localStorage.setItem(SMART_TOKEN, '');
        localStorage.setItem(SMART_PHONE, '');
        browserHistory.replace('/smarthome')
        message.error('token已过期');
      }
    }
    return Promise.reject({ message: response.data.msg });
  }
  throw new Error('未知的错误');
}, (err) => {
  if (err && err.response) {
    switch (err.response.status) {
      case 400:
        err.message = '请求参数错误'
        break

      case 401:
        err.message = '未授权，请登录'
        break

      case 403:
        err.message = '跨域拒绝访问'
        break

      case 404:
        err.message = '请求地址出错'
        break

      case 408:
        err.message = '请求超时'
        break

      case 500:
        err.message = '服务器内部错误'
        break

      case 501:
        err.message = '服务未实现'
        break

      case 502:
        err.message = '网关错误'
        break

      case 503:
        err.message = '服务不可用'
        break

      case 504:
        err.message = '网关超时'
        break

      case 505:
        err.message = 'HTTP版本不受支持'
        break

      default:
    }
  }
  return Promise.reject(err)
})

export default axios

function getToken(url) {
  window.type = url.indexOf('/api/smart/') > -1 ? WINDOW_TYPE_SMART : WINDOW_TYPE_ADMIN
  return window.type === WINDOW_TYPE_SMART ? localStorage.getItem(SMART_TOKEN) : localStorage.getItem(ADMIN_TOKEN);
}

/**
 * get请求
 * @param urlLink
 * @param param
 * @returns {Promise<AxiosResponse>}
 */
export function getData(urlLink, param, timeout = 30000) {
  const url = api[urlLink];
  const data = Object.assign({}, commonParams, param)
  instance.defaults.timeout = timeout
  instance.defaults.headers = Object.assign({}, commonHeaders, {
    Authorization: getToken(url),
  })
  return instance.get(url, {
    params: data,
  })
    .then(result => Promise.resolve(result.data))
    .catch((error) => {
      if (error) {
        return Promise.reject(error)
      }
    });
}

/**
 * post请求
 * @param urlLink
 * @param param
 * @returns {Promise<AxiosResponse>}
 */
export function postData(urlLink, param, timeout = 30000) {
  const url = api[urlLink];
  const data = Object.assign({}, commonParams, param);
  instance.defaults.timeout = timeout
  instance.defaults.headers = Object.assign({}, commonHeaders, {
    Authorization: getToken(url),
  })
  return instance.post(url, JSON.stringify(data))
    .then(result => Promise.resolve(result.data))
    .catch((error) => {
      if (error) {
        return Promise.reject(error)
      }
    });
}

export function uploadFile(urlLink, param, file, timeout = 120000) {
  const url = api[urlLink]
  const data = Object.assign({}, commonParams, param)
  instance.defaults.timeout = timeout
  instance.defaults.headers = Object.assign({}, commonHeaders, {
    Authorization: getToken(url),
    'Content-Type': 'multipart/form-data',
  })
  const formData = new FormData();
  formData.append('file', file);
  const entries = Object.entries(data)
  if (entries.length > 0) {
    entries.forEach(([key, value]) => {
      formData.append(key, value);
    });
  }
  return instance.post(url, formData)
    .then(result => Promise.resolve(result))
    .catch((error) => {
      if (error) {
        return Promise.reject(error)
      }
    });
}

export function getFullUrl(urlLink) {
  return api[urlLink];
}
