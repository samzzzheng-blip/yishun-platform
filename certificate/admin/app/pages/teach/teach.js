import React, { Component } from 'react'
import '@styles/teach.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';
import { browserHistory } from 'react-router';

export default class teach extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 3,
    }
  }

  render() {
    return (
      <div className="teach">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div className="teachBanner1Container">
          <div className="teachBanner1" />
          <div className="teachTitle" />
          <div className="teachMoreContianer" >
            <div className="teachKnowMore" onClick={() => { browserHistory.push('/collect'); }}>了解更多</div>
            <div style={{ marginTop: 20 }} className="teachKnowMore" onClick={() => { window.open('https://detail.tmall.com/item.htm?spm=a1z10.3-b-s.w4011-23523516113.29.43761489oculpH&id=639026212036&rn=7d85951d6837ce4cdbcadf86dda3f642&abbucket=6&skuId=4586663096299') }}>立即送评</div>
          </div>
        </div>
        <div className="teachBannerFitContainer">
          <div className="teachBanner2Container" onClick={() => browserHistory.push('/cardstar')}>
            <div className="teachBanner2A" />
          </div>
          <div className="teachBanner3Container" onClick={() => browserHistory.push('/cardstone')}>
            <div className="teachBanner3A" />
          </div>
          <div className="teachBanner4Container" onClick={() => browserHistory.push('/sailulu')}>
            <div className="teachBanner4A" />
          </div>
          <div className="teachBanner5Container" onClick={() => browserHistory.push('/letterworm')}>
            <div className="teachBanner5A" />
          </div>
          <div className="teachBanner6Container" onClick={() => { browserHistory.push('/diy'); }}>
            <div className="teachBanner6A" />
          </div>
        </div>
        <Footer />
      </div>
    )
  }
}
