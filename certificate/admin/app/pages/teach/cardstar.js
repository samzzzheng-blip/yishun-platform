import React, { Component } from 'react'
import '@styles/cardstar.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';
import { browserHistory } from 'react-router';

export default class cardstar extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 3,
    }
  }

  render() {
    return (
      <div className="cardstar">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div className="cardstarBanner1" />
        <div style={{ marginTop: 30, textAlign: 'center' }} className="cardstarThemeWord">
          提交类型
        </div>
        <div style={{
          position: 'relative', marginTop: 10, display: 'flex', flexDirection: 'row', justifyContent: 'center',
        }}
        >
          <div style={{ textAlign: 'center' }} className="cardstarThemeWord">
            以下是一瞬签名的三种主要服务类型
          </div>
          <div style={{ position: 'absolute' }} className="cardstarMore" onClick={() => { window.open('https://detail.tmall.com/item.htm?spm=a1z10.3-b-s.w4011-23523516113.29.43761489oculpH&id=639026212036&rn=7d85951d6837ce4cdbcadf86dda3f642&abbucket=6&skuId=4586663096299') }}>立即送评</div>
        </div>
        <div className="cardstarFitContainer">
          <div style={{ marginTop: 45, paddingBottom: 30 }} className="cardstarFlexRow">
            <div className="cardstarBanner2" onClick={() => browserHistory.push('/cardstarStand')}>
              <div className="cardstarIcon2" />
              <div className="cardstarThemeWord">评分标准</div>
            </div>
            <div className="cardstarBanner3" onClick={() => browserHistory.push('/cardstarExchange')}>
              <div className="cardstarIcon3" />
              <div className="cardstarThemeWord">更换卡具</div>
            </div>
            <div className="cardstarBanner4" onClick={() => browserHistory.push('/cardstarReGrade')}>
              <div className="cardstarIcon4" />
              <div className="cardstarThemeWord">重新评级</div>
            </div>
          </div>
          <div className="cardstarBanner5" />
          <div className="cardstarBanner6" />
        </div>

        <Footer />
      </div>
    )
  }
}
