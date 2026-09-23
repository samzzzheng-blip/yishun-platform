

export function parseQueryString(url) {
  const obj = {}
  if (url.indexOf('?') !== -1) {
    const str = url.split('?')[1]
    const strs = str.split('&')
    strs.map((item, i) => {
      const arr = strs[i].split('=')
      /* eslint-disable */
      obj[arr[0]] = arr[1]
    })
  }
  return obj
}

/* -----------------------------------------------------------------------------*/

/* -------------- 存储当前页面的菜单id到sessionStorage的menuId属性上 --------------*/
// 比较方法
function compare(children, pathname) {
  for (let i = 0; i < children.length; i += 1) {
    const item = children[i]
    /* eslint-disable no-useless-escape */
    const _resKey = `${item.resKey.replace(/[\$\.\?\+\^\[\]\(\)\{\}\|\\\/]/g, '\\$&').replace(/\*\*/g, '[\\w|\\W]+').replace(/\*/g, '[^\\/]+')}$`
    /* eslint-enable no-useless-escape */
    if (new RegExp(_resKey).test(pathname)) {
      sessionStorage.setItem('menuId', item.id)
      return true
    } else if (item.children) {
      if (compare(item.children, pathname)) return true
    }
  }
  return false
}

// 获取菜单id
export const getMenuId = (navs, pathname) => {
  if (navs && navs.length > 0) {
    compare(navs, pathname)
  }
}
/* -----------------------------------------------------------------------------*/


// 进入路由的判断
export const isLogin = (nextState, replaceState) => {
  if (!localStorage.getItem('token')) {
    replaceState('/login?returnTo=' + encodeURIComponent(nextState.location.pathname + (nextState.location.search || '')))
  }
}


// 异步请求需要走redux的方式
export const createAjaxAction = (createdApi, startAction, endAction) => (request = {}, resolve, reject, config) => (dispatch) => {
  if (startAction) dispatch(startAction({ req: request, res: {} }))
  const _resolve = (response) => {
    if (endAction) dispatch(endAction({ req: request, res: response }))
    if (resolve) resolve(response)
  }
  return createdApi(request, _resolve, reject, config)
}
