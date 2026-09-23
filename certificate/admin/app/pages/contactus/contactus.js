import React, { Component } from 'react'
import '@styles/contactus.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';

export default class contactus extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 5,
    }
  }

  render() {
    return (
      <div className="contactus">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div className="contactusBlank" />
        <div className="contactusFlexRowCenter">
          <div className="contactusContainer">
            <div className="img" />
            <div className="name">微信号：yishunqianming</div>
            {/* <div className="name" style={{fontSize:14,marginBottom:100}}>手机：19906653273  地址：杭州市江干区下沙经济技术开发区 科技园路新加坡科技园 16幢715室</div> */}
          </div>
          <div className="contactusContainer" style={{ marginLeft: 50 }}>
            <div className="img2" />
            <div className="name">微信公众号：一瞬签名</div>
          </div>
        </div>
        <Footer />
      </div>
    )
  }
}
