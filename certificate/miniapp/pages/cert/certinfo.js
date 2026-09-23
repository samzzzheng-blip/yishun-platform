// pages/cert/certinfo.js
const api = require("../../api/api.js");
const utils = require("../../utils/util.js");

Page({

  /**
   * 页面的初始数据
   */
  data: {
    certInfo:null,
    inputCert: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    if (options.result) {
      this.setData({
        certInfo: JSON.parse(options.result),
        inputCert: options.certNo
      })
    } else if (options.certNo) {
      this.setData({
        inputCert: options.certNo
      })
      this.search();
    }
  },
  showImage: function(e) {
    let url = e.currentTarget.dataset.url;
    console.log(url)
    if (!utils.isEmpty(url)) {
      wx.previewImage({
        current: url,
        urls: [url]
      })
      // wx.navigateTo({
      //   url: '/pages/gallery/gallery?url='+encodeURIComponent(url),
      // })
    }
  },
  playVideo: function(e) {
    let url = e.currentTarget.dataset.url;
    console.log(url)
    if (!utils.isEmpty(url)) {
      wx.navigateTo({
        url: '/pages/video/video?url='+encodeURIComponent(url),
      })
    }
    
  },
  bindCertInput: function(e) {
    this.data.inputCert = e.detail.value;
  },
  search: function() {
    if (utils.isEmpty(this.data.inputCert.length)) {
      wx.showToast({
        title: '请输入查询码',
        icon: 'none'
      });
      return;
    }
    let params = {
      certNumber: this.data.inputCert
    };
    api.queryCert(params).then(res=>{
      if (res.code == 200) {
        if (res.data != null) {
          this.setData({
            certInfo:res.data,
            inputCert: this.data.inputCert
          });
        } else {
          wx.showToast({
            title: "没有查询到证书信息",
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
    　　　　path: '/pages/cert/certinfo?certNo='+this.data.inputCert,        // 默认是当前页面，必须是以‘/’开头的完整路径
    　　　　imageUrl: '',     //自定义图片路径，可以是本地文件路径、代码包文件路径或者网络图片路径，支持PNG及JPG，不传入 imageUrl 则使用默认截图。显示图片长宽比是 5:4
    　　}
    　　// 返回shareObj
    　　return shareObj;
    }
})