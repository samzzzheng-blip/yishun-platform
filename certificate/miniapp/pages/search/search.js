// pages/search.js
const api = require("../../api/api.js");

Page({

  /**
   * 页面的初始数据
   */
  data: {
    inputCert:""
  },
  bindCertInput: function(e) {
    this.data.inputCert = e.detail.value;
  },
  search: function() {
    if (this.clickSearch) {
      return
    }
    if (this.data.inputCert.length == 0) {
      wx.showToast({
        title: '请输入查询码',
        icon: 'none'
      });
      return;
    }
    this.clickSearch = true
    let params = {
      certNumber: this.data.inputCert
    };
    api.queryCert(params).then(res=>{
      this.clickSearch = false
      if (res.code == 200) {
        if (res.data != null) {
          wx.navigateTo({
            url: '/pages/cert/certinfo?certNo='+this.data.inputCert+'&result='+JSON.stringify(res.data)
          })
          setTimeout(()=>{
            this.setData({
              inputCert: ''
            })
          },1000);
        } else {
          wx.showToast({
            title: '没有查询到证书信息',
            icon: 'none'
          });
        }
      } else {
        wx.showToast({
          title: res.msg,
          icon: 'none'
        });
      }
    }).catch(e=>{
      this.clickSearch = false
      wx.showToast({
        title: e.message||'服务繁忙',
        icon: 'none'
      });
    })
  },
  onShareAppMessage: function( options ){
    　　// 设置菜单中的转发按钮触发转发事件时的转发内容
      let shareObj = {
    　　　　title: "一瞬签名编码查询",        // 默认是小程序的名称(可以写slogan等)
    　　　　path: '/pages/search/search',        // 默认是当前页面，必须是以‘/’开头的完整路径
    　　　　imageUrl: '',     //自定义图片路径，可以是本地文件路径、代码包文件路径或者网络图片路径，支持PNG及JPG，不传入 imageUrl 则使用默认截图。显示图片长宽比是 5:4
    　　}
    　　// 返回shareObj
    　　return shareObj;
    }
})