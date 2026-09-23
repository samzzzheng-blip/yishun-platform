import React, { Component } from 'react'
import '@styles/diy.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';

export default class diy extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 3,
    }
  }

  render() {
    return (
      <div className="diy">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div className="diyBanner1" />
        <div style={{ margin: 20 }} className="diyThemeWordBig">
          曾经有一张张珍爱的照片、卡片摆在我面前，但是我没有好好珍惜，等到损坏了我才后悔莫及，尘世间最痛苦的事莫过于此，如果上天再给我一次机会，我会好好保护它们。 如果爱有期限的话，我希望是一辈子。
        </div>
        <div className="diyFitContainer">
          <div className="diyBanner3" />
          <div className="diyBanner4" />
        </div>
        <div className="diyDescContainer">
          <div style={{ flex: 1, textAlign: 'center' }} className="diyThemeWordSmall">
            一瞬签名提供两种尺寸卡砖
          </div>
          <div style={{ marginTop: 20 }} className="diyFlexRow">
            <div style={{ textAlign: 'center' }} className="diyThemeWordSmall">
              卡砖尺寸12*21CM
              {'\n'}
              卡纸尺寸：10.2*15.2 （标准6寸)
            </div>
            <div style={{ flex: 1 }} />
            <div style={{ textAlign: 'center' }} className="diyThemeWordSmall">
              卡砖尺寸8*13.5CM
              {'\n'}
              卡纸尺寸：8.9*6.4 （如卡纸较小需裁剪)
            </div>
          </div>
          <div style={{ marginTop: 30, marginBottom: 80 }} className="diyThemeWord">
            无论是收藏还是纪念，卡砖是你最好选择，卡砖塑封可隔离空气中的氧气、水分子等，保护自己珍爱的照片、卡牌可避免褶皱、氧化发黄等常规问题。
          </div>
        </div>
        <Footer />
      </div>
    )
  }
}
