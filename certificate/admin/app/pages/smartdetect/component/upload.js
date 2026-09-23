import React, { Component, createRef } from 'react'
import { Button, message, Modal, Upload } from 'antd';
import '@smartstyles/upload.less'
import { postData } from '../../../service/http';
import api from '@service/api';

export default class SmartUpload extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props);
    this.uploadRef = createRef()
    this.showImgRef = createRef()
    this.state = {
      showDialog: false,
      imgList: [],
      historyImgs: [],
    }
  }

  componentDidMount() {
    this.queryHistoryUploadImg()
  }

  uploadImg = (type, info) => {
    let fileList = [...info.fileList];
    // 1. Limit the number of uploaded files
    // Only to show one recent uploaded files, and old ones will be replaced by the new
    fileList = fileList.slice(-1);
    // 2. Read from response and show file link
    fileList = fileList.map((file) => {
      if (file.response) {
        // Component will show file.url as link
        file.url = file.response.url;
        file.name = file.response.name;
      }
      return file;
    });
    if (type === 'imgUrl' && fileList && fileList.length > 0 && fileList[0].url) {
      this.setState({ imgList: fileList });
      // 查询历史图片
      this.queryHistoryUploadImg()
    }
  };

  async queryHistoryUploadImg() {
    postData('smartGetUploadImgHistory', { type: this.props.step }).then((data) => {
      if (data) {
        this.setState({
          historyImgs: data,
        })
      }
    }).catch((err) => {
      console.log(err)
    })
  }

  deleteHistoryImg(id, index) {
    postData('smartDeleteHistoryImg', { id }).then((data) => {
      this.queryHistoryUploadImg()
    })
  }

  drawWrap() {
    if (this.state.imgList.length === 0) {
      message.error('请先上传图片')
      return
    }
    if (this.props.drawWrap) {
      const _this = this
      const img = new Image();
      img.onload = () => {
        _this.props.drawWrap(_this.state.imgList[0].url, _this.props.step, img.width, img.height)
      };
      img.crossOrigin = 'anonymous';
      img.src = this.state.imgList[0].url;
    }
  }

  goNext() {
    if (this.state.imgList.length === 0) {
      message.error('请先上传图片')
      return
    }
    if (this.props.goNext) {
      this.props.goNext(this.props.step, this.state.imgList[0].url)
    }
  }

  goBack() {
    if (this.props.goBack) {
      this.props.goBack()
    }
  }


  render() {
    const uploadProps = {
      action: api.smartUploadCardImg, // 上传地址
      multiple: false,
      name: 'file',
      headers: {
        Authorization: localStorage.getItem('smart-token'),
      },
      data: {
        type: this.props.step,
      },
    };
    const imgProps = {
      key: this.props.step,
      onChange: this.uploadImg.bind(this, 'imgUrl'),
      accept: 'image/*',
      listType: 'text',
      showUploadList: false,
      ...uploadProps,
    };
    return (
      <div className="upload-box">
        <div className="upload-header">{this.props.step === 1 ? '选择图片（正面）' : '选择图片（背面）'}</div>
        <div className="upload-body">
          <div className="upload-img-box">
            <div className="upload-img-dash">
              <Upload {...imgProps} ref={this.uploadRef}>
                <Button className="base-btn upload-btnUpload"
                  style={{ display: this.state.imgList.length > 0 ? 'none' : '' }}
                >上传图片</Button>
              </Upload>
            </div>
            {
              this.state.imgList.length > 0 ? (
                <img
                  ref={this.showImgRef}
                  className="upload-img-dash"
                  src={this.state.imgList[0].url}
                  alt=""
                  onClick={() => this.reUploadImg()}
                />
              ) : null
            }
          </div>
          {
            this.state.historyImgs.length > 0 ? (
              <div className="upload-history-img-box">
                <div className="upload-history-img-title">历史图片</div>
                <div className="upload-history-img-wrap">
                  {
                    this.state.historyImgs.map((item, index) => (
                      <div className="upload-history-img-item-box" key={index}>
                        <img className="upload-history-img"
                          src={item.url}
                          alt=""
                          key={item.url}
                        />
                        <div className="upload-history-img-name">{item.name}</div>
                        <div className="upload-history-img-btn-box">
                          <span className="upload-history-img-btn" onClick={() => this.selectHistoryImg(item)}>选择</span>
                          <span className="upload-history-img-btn" style={{ marginLeft: '10px' }} onClick={() => this.deleteHistoryImg(item.id)}>删除</span>
                        </div>
                      </div>
                    ))
                  }
                </div>
              </div>
            ) : null
          }

        </div>
        <div className="upload-img-tip">图片清晰度会直接影响评分结果</div>
        <div className="upload-footLine" />
        <div className="upload-footer">
          {this.props.step === 2 ? <Button className="base-btn upload-btnNext" style={{ marginRight: '20px' }} onClick={() => this.goBack()}>上一步</Button> : null}
          <Button className="base-btn upload-btnDrawWrap" onClick={() => this.drawWrap()}>手动校正</Button>
          <Button className="base-btn upload-btnNext" onClick={() => this.goNext()}>下一步</Button>
        </div>
      </div>
    )
  }

  reUploadImg() {
    if (this.uploadRef && this.uploadRef.current) {
      const input = document.querySelector('.upload-img-dash input[type=file]');
      if (input) {
        input.click()
      }
    }
  }

  selectHistoryImg(item) {
    Modal.confirm({
      title: '选择这张图片上传吗？',
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        this.setState({
          imgList: [item],
        })
      },
    })
  }

  updateWrapImg(url) {
    if (!url) {
      this.setState({
        imgList: [],
      })
    } else {
      this.setState({
        imgList: [
          { url: url },
        ],
      })
    }
    this.queryHistoryUploadImg()
  }
}
