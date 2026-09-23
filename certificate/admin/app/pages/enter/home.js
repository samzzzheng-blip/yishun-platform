import React, { Component } from 'react'
import '@styles/home.less'
import Header from '../common/header'
import Footer from '../common/footer';
import Nav from '../common/nav';
import { message, Spin, Icon } from 'antd';
import { postData, getFullUrl } from '@http';
import { browserHistory } from 'react-router';

export default class home extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props);
    const locationState = props.location.state;
    const searchParams = new URLSearchParams(props.location.search)
    this.state = {
      selectNav: 2,
      queryText: searchParams && searchParams.get('certNum') ? searchParams.get('certNum') : '',
      showResult: locationState && locationState.certInfo != null,
      certInfo: locationState && locationState.certInfo ? locationState.certInfo : null,
      loading: false,
    }
    this._btnClick = this._btnClick.bind(this);
    this._handleChange = this._handleChange.bind(this);
    this._keyDown = this._keyDown.bind(this);

    // 参数访问
    if (!this.state.showResult && this.state.queryText) {
      this._btnClick()
    }
  }

  getUrlParams(name, str) {
    const reg = new RegExp(`(^|&)${name}=([^&]*)(&|$)`);
    const r = str.substr(1).match(reg);
    if (r != null) return decodeURIComponent(r[2]); return null;
  }

  _handleChange(e) {
    this.setState({
      queryText: e.target.value,
    })
  }

  _check() {
    if (this.state.queryText === null || this.state.queryText.trim() === '') {
      message.warning('请输入查询码')
      return false;
    }
    return true;
  }

  _btnClick() {
    if (!this._check()) {
      return;
    }
    const params = {
      certNumber: this.state.queryText,
    };
    this.setState({
      loading: true,
    });
    postData('queryCert', params).then((data) => {
      if (data && data.certNumber) {
        this.setState({
          showResult: true,
          certInfo: data,
        });
      } else {
        this.setState({
          showResult: false,
          certInfo: null,
        });
        message.warning('没有查询到该证书信息');
      }
      this.setState({
        loading: false,
      });
      // 修改页面url，加上搜索参数
      const location = window.location;
      const searchParams = new URLSearchParams(location.search);
      searchParams.set('certNum', params.certNumber);
      browserHistory.replace({
        pathname: location.pathname,
        search: `?${searchParams.toString()}`,
      });
    }).catch((err) => {
      message.warning(err.message);
      this.setState({
        loading: false,
      });
    });
  }

  _keyDown(e) {
    if (e.keyCode == 13) {
      // 回车事件
      this._btnClick();
    }
  }

  _jumpBlankHref = (href, type) => {
    sessionStorage.setItem('previewParams', JSON.stringify({ href, type }));
    // window.open(`${window.location.protocol}//${window.location.host}/public/index.html?time=${new Date().getTime()}`)
    window.open('/preview', '_blank');
  }

  _renderEvidenceVideoView() {
    const videoView = this.state.certInfo && this.state.certInfo.evidenceVideoImgUrl ? (
      <div className="evidenceVideoContainer">
        <img src={this.state.certInfo.evidenceVideoImgUrl} className="evidenceVideo" onClick={() => this._jumpBlankHref(this.state.certInfo.evidenceVideo, 2)} />
        <Icon type="play-circle" className="playicon" theme="filled" onClick={() => this._jumpBlankHref(this.state.certInfo.evidenceVideo, 2)} />
      </div>
    ) : null;
    return videoView;
  }

  _renderEvidenceImgView() {
    const marginLeftStyle = this.state.certInfo && this.state.certInfo.evidenceVideoImgUrl ? { marginLeft: 20 } : { marginLeft: 0 };
    const imgView = this.state.certInfo && this.state.certInfo.evidenceImg ? (
      <img src={this.state.certInfo.evidenceImg} className="evidenceImg" style={marginLeftStyle} onClick={() => this._jumpBlankHref(this.state.certInfo.evidenceImg, 1)} />
    ) : null;
    return imgView;
  }

  _renderRateContent() {
    const scoreView = this.state.certInfo && this.state.certInfo.score ? (
      <div className="item">
          总评分：{this.state.certInfo.score}
      </div>
    ) : null;
    const remarkView = this.state.certInfo && this.state.certInfo.remark ? (
      <div className="item">
          备注：{this.state.certInfo.remark}
      </div>
    ) : null;
    const contentView = this.state.certInfo ? (
      <div>
        <div className="preciousContent" style={{ display: this.state.showResult ? '' : 'none' }}>
          <div className="photoContainer">
            <img src={this.state.certInfo.imgUrl} className="photo" onClick={() => this._jumpBlankHref(this.state.certInfo.imgUrl)} onContextMenu={e => e.preventDefault()} />
          </div>
          <div className="textContent">
            <div className="item">
                名称：{this.state.certInfo.rateName}
            </div>
            <div className="item">
                表面：{this.state.certInfo.surface}
            </div>
            <div className="item">
              居中：{this.state.certInfo.center}
            </div>
            <div className="item">
                边缘：{this.state.certInfo.edge}
            </div>
            <div className="item">
                角落：{this.state.certInfo.corner}
            </div>
            {scoreView}
            {remarkView}
          </div>
        </div>
      </div>
    ) : null;
    return contentView;
  }

  _renderCartoonContent() {
    const scoreView = this.state.certInfo && this.state.certInfo.score ? (
      <div className="item">
          评级：{this.state.certInfo.score}
      </div>
    ) : null;
    const remarkView = this.state.certInfo && this.state.certInfo.remark ? (
      <div className="item">
          备注：{this.state.certInfo.remark}
      </div>
    ) : null;
    const contentView = this.state.certInfo ? (
      <div>
        <div className="preciousContent" style={{ display: this.state.showResult ? '' : 'none' }}>
          <div className="photoContainer">
            <img src={this.state.certInfo.imgUrl} className="photo" onClick={() => this._jumpBlankHref(this.state.certInfo.imgUrl)} onContextMenu={e => e.preventDefault()} />
          </div>
          <div className="textContent">
            <div className="item">
                角色名称：{this.state.certInfo.roleName}
            </div>
            <div className="item">
                载体：{this.state.certInfo.itemType}
            </div>
            <div className="item">
                动漫名称：{this.state.certInfo.cartoonName}
            </div>
            <div className="item">
                原作者：{this.state.certInfo.author}
            </div>
            <div className="item">
                制作公司：{this.state.certInfo.company}
            </div>
            {scoreView}
            {remarkView}
          </div>
        </div>
        {
          this.state.certInfo.evidenceVideoImgUrl || this.state.certInfo.evidenceImg ? (
            <div className="evidenceContainer">
              <div className="evidenceBorder1" />
              <div className="evidenceBorder2">
                <div className="evidenceHeader">
                  <div className="iconImg" />
                  证据：
                </div>
                <div className="evidenceContent" onContextMenu={e => e.preventDefault()}>
                  {this._renderEvidenceVideoView()}
                  {this._renderEvidenceImgView()}
                </div>
              </div>
            </div>
          ) : null
        }
      </div>
    ) : null;
    return contentView;
  }

  _renderContent() {
    const contentView = this.state.certInfo ? (
      <div>
        <div className="preciousContent" style={{ display: this.state.showResult ? '' : 'none' }}>
          <div className="photoContainer">
            <img src={this.state.certInfo.imgUrl} className="photo" onClick={() => this._jumpBlankHref(this.state.certInfo.imgUrl, 1)} onContextMenu={e => e.preventDefault()} />
          </div>
          <div className="textContent">
            {
              this.state.certInfo.certNumber ? (
                <div className="item">
                  证书编码：{this.state.certInfo.certNumber}
                </div>
              ) : null
            }
            {
              this.state.certInfo.itemType ? (
                <div className="item">
                  载体：{this.state.certInfo.itemType}
                </div>
              ) : null
            }
            {
              this.state.certInfo.signer ? (
                <div className="item">
                  姓名：{this.state.certInfo.signer}
                </div>
              ) : null
            }
            {
              this.state.certInfo.publishTime ? (
                <div className="item">
                  时间：{this.state.certInfo.publishTime}
                </div>
              ) : null
            }
            {
              this.state.certInfo.publishCity ? (
                <div className="item">
                  地点：{this.state.certInfo.publishCity}
                </div>
              ) : null
            }
            {
              this.state.certInfo.publishActivity ? (
                <div className="item">
                  卡品：{this.state.certInfo.publishActivity}
                </div>
              ) : null
            }
            {
              this.state.certInfo.publishSign ? (
                <div className="item">
                  亲笔签名：{this.state.certInfo.publishSign}
                </div>
              ) : null
            }
            {
              this.state.certInfo.score ? (
                <div className="item">
                  评级：{this.state.certInfo.score}
                </div>
              ) : null
            }
            {
              this.state.certInfo.remark ? (
                <div className="item">
                  备注：{this.state.certInfo.remark}
                </div>
              ) : null
            }
          </div>
        </div>
        {
          this.state.certInfo.evidenceVideoImgUrl || this.state.certInfo.evidenceImg ? (
            <div className="evidenceContainer">
              <div className="evidenceBorder1" />
              <div className="evidenceBorder2">
                <div className="evidenceHeader">
                  <div className="iconImg" />
                  证据：
                </div>
                <div className="evidenceContent" onContextMenu={e => e.preventDefault()}>
                  {this._renderEvidenceVideoView()}
                  {this._renderEvidenceImgView()}
                </div>
              </div>
            </div>
          ) : null
        }
      </div>
    ) : null;
    return contentView;
  }


  render() {
    const loading = this.state.loading ? (
      <div className="loading">
        <Spin size="small" tip="Loading..." />
      </div>
    ) : null;
    const content = this.state.certInfo && this.state.certInfo.rateName ? this._renderRateContent() : this.state.certInfo && this.state.certInfo.roleName ? this._renderCartoonContent() : this._renderContent();
    return (
      <div className="home">
        {loading}
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div className="homeContent">
          <div className="homeSearch">
            <div className="line" />
            <div className="searchArea">
              <input className="inputbox"
                placeholder="输入查询码"
                maxLength={50}
                autoFocus
                onKeyDown={this._keyDown}
                value={this.state.queryText}
                onChange={this._handleChange}
              />
              <div className="searchicon" onClick={() => this._btnClick()} />
            </div>
          </div>
          {content}
        </div>
        <Footer />
      </div>
    )
  }
}
