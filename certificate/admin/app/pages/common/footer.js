
import React, { Component } from 'react'
import '@styles/footer.less'
import { browserHistory } from 'react-router';

export default class Footer extends Component {
  constructor(props, context) {
    super(props)
  }

  goIcp() {
    window.open('https://beian.miit.gov.cn');
  }

  render() {
    return (
      <footer className="footer">
        <div className="logodiv">
          <div className="logo1" onClick={() => window.open('https://space.bilibili.com/452087614?from=search&seid=3298360001528888937')} />
          <div className="logo2" onClick={() => window.open('https://weibo.com/u/7405872123')} />
          <div className="logo3" onClick={() => browserHistory.push('/contactus')} />
          <div className="logo4" onClick={() => window.open(' https://v.douyin.com/eLsvPtG/')} />
        </div>
        <div className="line1" />
        <div className="line2" />
        <div onClick={() => this.goIcp()}>杭州常胜贸易有限公司      浙ICP备19026381号</div>
      </footer>
    )
  }
}
