import React, { Component } from 'react'
import '@styles/cardstarregrade.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';
import { browserHistory } from 'react-router';

export default class cardstarregrade extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 3,
    }
  }

  render() {
    return (
      <div className="cardstarItem3">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div>
          <div style={{ marginTop: 10, textAlign: 'center' }} className="cardstarItem3ThemeWordLarge">
            重新评级
          </div>
          <div style={{ marginTop: 10, marginBottom: 10, textAlign: 'center' }} className="cardstarItem3ThemeWord">
            将原始球星卡提交给一瞬签名
          </div>
          <div className="cardstarItem3Navigate">
            <div style={{ flex: 2, paddingLeft: 30, flexDirection: 'row', alignItems: 'center' }} className="cardstarItem3FlexRow" onClick={() => browserHistory.goBack()}>
              <div className="cardstarItem3Back" />
              <div style={{ cursor: 'pointer' }} className="cardstarItem3WhiteTitle" >
                返回上一层
              </div>
            </div>
            <div style={{ flex: 2, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem3WhiteTitleBig">
              提交类型
            </div>
            <div style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem3WhiteTitle" onClick={() => browserHistory.replace('/cardstarStand')}>评级标准</div>
            <div style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem3WhiteTitle" onClick={() => browserHistory.replace('/cardstarExchange')}>更换卡具</div>
            <div style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem3WhiteTitle" onClick={() => browserHistory.replace('/cardstarReGrade')}>重新评级</div>
          </div>
        </div>
        <div className="cardstarFitContainer">
          <div className="cardstarItem3FlexRow" style={{ marginTop: 50, marginBottom: 80 }}>
            <div style={{ flex: 1 }}>
              <div style={{ marginTop: 20, paddingLeft: 30 }} className="cardstarItem3BlackWordBig">
                在什么情况下需要重新评级？
              </div>
              <div style={{ marginTop: 20, paddingLeft: 30 }} className="cardstarItem3BlackWord">
                &nbsp;&nbsp;&nbsp;&nbsp;
                重新评级是一项服务，可让您有机会获得自己认为可能值得更高等级的一瞬签名评级卡。要使卡片有资格获得审核，您必须将其提交给当前的一瞬签名卡具。
              </div>
              <div style={{ marginTop: 20, paddingLeft: 30 }} className="cardstarItem3BlackWordBig">
                为什么使用重新评级？
              </div>
              <div style={{ marginTop: 20, paddingLeft: 30 }} className="cardstarItem3BlackWord">
                &nbsp;&nbsp;&nbsp;&nbsp;
                如果您的交易卡不升级，则不会丢失原始标签和等级。
                {'\n'} &nbsp;&nbsp;&nbsp;&nbsp;
                重新评级可能会增加您交易卡的价值。
                {'\n'} &nbsp;&nbsp;&nbsp;&nbsp;
                认证编号保持不变。
              </div>
            </div>
            <div style={{ flex: 0.6, justifyContent: 'center', display: 'flex', marginTop: 10 }}>
              <div className="cardstarItem3Banner1" />
            </div>
          </div>
        </div>
        <Footer />
      </div>
    )
  }
}

