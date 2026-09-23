import React, { Component } from 'react'
import '@styles/cardstarexchange.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';
import { browserHistory } from 'react-router';

export default class cardstarexchange extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 3,
    }
  }

  render() {
    return (
      <div className="cardstarItem1">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div style={{ flex: 1 }}>
          <div style={{ marginTop: 10, textAlign: 'center' }} className="cardstarItem1ThemeWordLarge">
            更换卡具
          </div>
          <div style={{ marginTop: 10, marginBottom: 10, textAlign: 'center' }} className="cardstarItem1ThemeWord">
            将原始球星卡提交给一瞬签名
          </div>
          <div className="cardstarItem1Navigate">
            <div style={{ flex: 2, paddingLeft: 30, flexDirection: 'row', alignItems: 'center' }} className="cardstarItem1FlexRow">
              <div className="cardstarItem1Back" />
              <div style={{ cursor: 'pointer' }} className="cardstarItem1WhiteTitle" onClick={() => browserHistory.goBack()}>
                返回上一层
              </div>
            </div>
            <div style={{ flex: 2, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem1WhiteTitleBig">
              提交类型
            </div>
            <div style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem1WhiteTitle" onClick={() => browserHistory.replace('/cardstarStand')}>评级标准</div>
            <div style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem1WhiteTitle" onClick={() => browserHistory.replace('/cardstarExchange')}>更换卡具</div>
            <div style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem1WhiteTitle" onClick={() => browserHistory.replace('/cardstarReGrade')}>重新评级</div>
          </div>
        </div>
        <div className="cardstarItem1FitContainer">
          <div className="cardstarItem1FlexRow" style={{ marginTop: 30 }}>
            <div style={{ flex: 1 }}>
              <div style={{ marginTop: 20, paddingLeft: 30 }} className="cardstarItem1ThemeWord">
                什么是更换卡具
              </div>
              <div style={{ marginTop: 20, paddingLeft: 30 }} className="cardstarItem1BlackWord">
                &nbsp;&nbsp;&nbsp;&nbsp;
                更换卡具是针对您以前由其他公司评级为一瞬签名卡具的卡片。一瞬签名将评估其当前卡具中的卡。如果一瞬签名认为该卡值得在客户指定的最低等级或更高的最低要求上更换卡具使用，则该卡将从其卡具中取出并放入一瞬签名卡具中。无论结果如何，都将收取评价价格。
                {'\n'}{'\n'} &nbsp;&nbsp;&nbsp;&nbsp;
                还有其他第三方评级公司，但只有一个行业领导者。我们为想要提高安全性并提高其先前经过身份验证和评级的交易卡的价值的收集者提供跨界服务。转向最受尊敬的高端品牌一瞬签名可以做到这一点。
              </div>
            </div>
            <div style={{ flex: 1 }}>
              <div className="cardstarItem1Banner1" />
            </div>
          </div>
          <div style={{ marginTop: 30 }} className="cardstarItem1FlexRow">
            <div style={{ flex: 1, marginTop: 20 }}>
              <div className="cardstarItem1Banner2" />
            </div>
            <div style={{ flex: 1.5, paddingRight: 80 }}>
              <div style={{ marginTop: 30 }} className="cardstarItem1ThemeWord">
                卡具损坏是否能够更换卡具
              </div>
              <div style={{ marginTop: 20 }} className="cardstarItem1BlackWord">
                &nbsp;&nbsp;&nbsp;&nbsp;
                卡具损坏时可更换卡具，评级卡的评分会根据现状重新评级：在卡具损坏时影响球星卡、
                整体美观，这会影响到评分情况。
              </div>
            </div>
          </div>
          <div style={{ flex: 1, marginTop: 30 }}>
            <div style={{ textAlign: 'center', alignSelf: 'center' }} className="cardstarItem1ThemeWord">
              关于最低等级
            </div>
            <div style={{ marginTop: 20, paddingLeft: 30, paddingRight: 30 }} className="cardstarItem1BlackWord">
              &nbsp;&nbsp;&nbsp;&nbsp;
              最低等级是您愿意从一瞬签名接受的最低等级，任何较低的等级都不会转让给一瞬签名卡具。一瞬签名会将您的商品归类到现有的卡具中，并且只有在您的商品达到或超过最低等级时，才会将其删除并将其封装在一瞬签名卡具中。如果您的产品不愿意接受最低等级要求，则会以原始的形式退还给您。
            </div>
          </div>
          <div style={{ marginTop: 30, marginBottom: 30 }} className="cardstarItem1Banner3" />
          <div style={{ marginBottom: 100 }} className="cardstarItem1DescContainer">
            <div style={{
              flex: 1, alignItems: 'center', justifyContent: 'center', display: 'flex', marginRight: 30,
            }}
            >
              <div className="cardstarItem1BlackWord">
                &nbsp;&nbsp;&nbsp;&nbsp;
                该项目不符合一瞬签名的最低10级标准，并退回原始持有人手中
                {'\n'}{'\n'}
                &nbsp;&nbsp;&nbsp;&nbsp;
                即使项目不符合最低等级要求，也会收取全额评级费
              </div>
            </div>
            <div style={{
              flex: 1, marginLeft: 30, alignItems: 'center', justifyContent: 'center', display: 'flex',
            }}
            >
              <div className="cardstarItem1BlackWord">
                &nbsp;&nbsp;&nbsp;&nbsp;
                项目达到或超过最低10级
                {'\n'}{'\n'}
                &nbsp;&nbsp;&nbsp;&nbsp;
                该卡封装在一瞬签名支架中，并收取完全定额费用
              </div>
            </div>
          </div>
        </div>
        <Footer />
      </div>
    )
  }
}

