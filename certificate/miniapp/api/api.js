// api.js 
//request请求
const app = getApp();
const API_BASE_URL = app.globalData.baseUrl;
const request = (url, method, data) => {
  let accessToken = "";
  var promise = new Promise((resolve, reject) => {
    //网络请求
    wx.request({
      url: API_BASE_URL + url,
      data: data,
      method: method,
      header: {
        'Content-Type': 'application/json',
        'client': 32,
        'version': '1.0.0',
        'accessToken': accessToken
      },
      success: function(res) {
        //服务器返回数据
        if (res.statusCode && res.statusCode == 200) {
          resolve(res.data);
        } else {
          //返回错误提示信息
          wx.showToast({
            title: res.msg || '请求失败请稍后重试',
            icon: 'none',
            duration: 1000
          })
          wx.stopPullDownRefresh() //刷新完成后停止下拉刷新
        }
      },
      fail: function(e) {
        wx.showToast({
          title: '无法连接服务器',
          icon: 'loading',
          duration: 1000
        })
        wx.getNetworkType({
          success: function (res) {
            if (res.networkType == 'none') { //无网络
              reject({ net: false });
            } else {
              reject({ net: '网络错误' });
            }
          }
        });
        wx.stopPullDownRefresh() //刷新完成后停止下拉刷新
      }
    })
  });
  return promise;
}

module.exports = {
  queryCert: (data) => {
    return request('base/queryCert', 'POST', data);
  }
}