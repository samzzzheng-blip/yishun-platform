import React, { Component } from 'react'
import '@styles/search.less'
import { message, Spin, Icon } from 'antd';
import { postData } from '@http';
import { browserHistory } from 'react-router';
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';

export default class search extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      certInfo: null,
      loading: false,
      selectNav: 1,
      showMiniCode: false,
    }
    this._btnClick = this._btnClick.bind(this);
    this._handleChange = this._handleChange.bind(this);
    this._keyDown = this._keyDown.bind(this);
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

        const path = {
          pathname: '/home',
          state: { certInfo: data },
          search: `?certNum=${params.certNumber}`,
        };
        browserHistory.push(path);
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

  _renderAdvert() {
    return (
      <div className="advertContainer">
        <div className="advert1" onClick={() => { this.setState({ showMiniCode: true }) }} />
        <div className="advert2" onClick={() => { this.setState({ showMiniCode: true }) }} />
      </div>
    )
  }

  _renderMiniCode() {
    return this.state.showMiniCode ? (
      <div className="minicodeContainer" onClick={() => { this.setState({ showMiniCode: false }) }}>
        <div className="minicodeImg" onClick={e => e.stopPropagation()} />
        <div className="minicodeTip">微信扫码进小程序</div>
      </div>
    ) : null
  }

  render() {
    const loading = this.state.loading ? (
      <div className="loading">
        <Spin size="small" tip="Loading..." />
      </div>
    ) : null;
    return (
      <div className="search">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div className="searchBody">
          {loading}
          <div className="logo" />
          <div className="searchArea">
            <input className="inputbox"
              placeholder="输入查询码"
              onChange={this._handleChange}
              value={this.state.queryText}
              onKeyDown={this._keyDown}
              maxLength={50}
              autoFocus
            />
            <div className="searchicon" onClick={() => this._btnClick()} />
          </div>
          {/* <div className="help" onClick={()=>alert("help")}>需要帮助?</div> */}
        </div>
        {this._renderAdvert()}
        {this._renderMiniCode()}
        <Footer />
      </div>
    )
  }
}
