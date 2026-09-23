import React, { Component } from 'react'
import '@styles/cardstarstand.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';
import { browserHistory } from 'react-router';

export default class cardstarstand extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 3,
      selectScoreIndex: 0,
    }
  }

  _renderScoreContent(index) {
    let contentView = null;
    if (index === 0) {
      contentView = (
        <div className="cardStarStandContainer1">
          <div className="cardStarStandPic1_1" />
          <div className="cardStarStandWord1_1" />
          <div className="cardStarStandPic1_2" />
          <div className="cardStarStandWord1_2" />
        </div>
      )
    } else if (index === 1) {
      contentView = (
        <div className="cardStarStandContainer2">
          <div className="cardStarStandPics2">
            <div className="cardStarStandPicRow">
              <div className="cardStarStandPic2_1" />
              <div className="cardStarStandPic2_2" />
            </div>
            <div className="cardStarStandPic2_3" />
          </div>
          <div className="cardStarStandUl2" />
          <div className="cardStarStandWord2" />
        </div>
      )
    } else if (index === 2) {
      contentView = (
        <div className="cardStarStandContainer3">
          <div className="cardStarStandPics3">
            <div className="cardStarStandPic3_1" />
            <div className="cardStarStandPic3_2" />
          </div>
          <div className="cardStarStandUl3" />
          <div className="cardStarStandWord3" />
        </div>
      )
    } else if (index === 3) {
      contentView = (
        <div className="cardStarStandContainer4">
          <div className="cardStarStandPics4">
            <div className="cardStarStandPicRow">
              <div className="cardStarStandPic4_1" />
              <div className="cardStarStandPic4_2" />
            </div>
            <div className="cardStarStandPic4Word" />
          </div>
          <div className="cardStarStandWord4_1" />
          <div className="cardStarStandWord4_2" />
        </div>
      )
    } else if (index === 4) {
      contentView = (
        <div className="cardStarStandContainer5">
          <div className="cardStarStandPics5">
            <div className="cardStarStandPic5_1" />
            <div className="cardStarStandPic5Word" />
          </div>
          <div className="cardStarStandWord5_1" />
          <div className="cardStarStandWord5_2" />
        </div>
      )
    }

    return contentView;
  }

  _clickScoreTitle(index) {
    this.setState({
      selectScoreIndex: index,
    })
  }

  _renderStandScore() {
    const scoreTitles = ['评分计算法则', '表面评分', '居中评分', '边缘评分', '角落评分'];
    const titleView = scoreTitles.map((title, index) => {
      const tabMargin = this.state.selectScoreIndex === index ? 8 : 6;
      const textSize = this.state.selectScoreIndex === index ? 22 : 18;
      return (
        <div onClick={() => this._clickScoreTitle(index)} className="cardStarStandScoreTitle" style={{ fontSize: textSize, padding: tabMargin }}>{title}</div>
      )
    });
    return (
      <div>
        <div className="cardStarStandScoreTitleContainer">
          {titleView}
        </div>
        {this._renderScoreContent(this.state.selectScoreIndex)}
      </div>
    );
  }

  render() {
    return (
      <div className="cardstarItem2">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div>
          <div style={{ marginTop: 10, textAlign: 'center' }} className="cardstarItem2ThemeWordLarge">
            评级标准
          </div>
          <div style={{ marginTop: 10, marginBottom: 10, textAlign: 'center' }} className="cardstarItem2ThemeWord">
            将原始球星卡提交给一瞬签名
          </div>
          <div className="cardstarItem2Navigate">
            <div style={{ flex: 2, paddingLeft: 30, flexDirection: 'row', alignItems: 'center' }} className="cardstarItem2FlexRow">
              <div className="cardstarItem2Back" />
              <div style={{ cursor: 'pointer' }} className="cardstarItem2WhiteTitle" onClick={() => browserHistory.goBack()}>
                返回上一层
              </div>
            </div>
            <div style={{ flex: 2, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem2WhiteTitleBig">
              提交类型
            </div>
            <div style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem2WhiteTitle" onClick={() => browserHistory.replace('/cardstarStand')}>评级标准</div>
            <div style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem2WhiteTitle" onClick={() => browserHistory.replace('/cardstarExchange')}>更换卡具</div>
            <div style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }} className="cardstarItem2WhiteTitle" onClick={() => browserHistory.replace('/cardstarReGrade')}>重新评级</div>

          </div>
        </div>
        <div className="cardstarItem2FitContainer">
          <div className="cardstarItem2FlexRow">
            <div style={{ flex: 1 }}>
              <div style={{ marginTop: 20, paddingLeft: 30 }} className="cardstarItem2BlackWordBig">
                什么是交易卡身份验证和评级？
              </div>
              <div style={{ marginTop: 20, paddingLeft: 30 }} className="cardstarItem2BlackWord">
                &nbsp;&nbsp;&nbsp;&nbsp;
                一瞬签名是全球最大和最受尊敬的第三方认证、评级、收藏俱乐部公司，用于交易卡和纪念品。
                {'\n'}{'\n'} &nbsp;&nbsp;&nbsp;&nbsp;
                认证是验证交易卡的真实性或真实的过程。分级是使用一瞬签名球星卡的10分分级量表评估交易卡的质量和状况。只有在交易卡被认为是真实的之后才能进行评级。
                {'\n'}{'\n'} &nbsp;&nbsp;&nbsp;&nbsp;
                无论是老式，现代还是TCG，一瞬签名都会在整个爱好中对卡片进行身份验证和分级。在下面详细了解我们的评分流程，内容丰富的内容和收集资源。
              </div>
            </div>
            <div style={{ flex: 1, justifyContent: 'center', display: 'flex', marginTop: 30 }}>
              <div className="cardstarItem2Banner1" />
            </div>
          </div>
          <div style={{
            textAlign: 'center', flex: 1, marginTop: 10, paddingTop: 30, paddingBottom: 30,
          }}
            className="cardstarItem2ThemeWordLarge"
          >
            球星卡认证与评级流程
          </div>
          <div style={{ flex: 1, paddingBottom: 20 }} className="cardstarItem2FlexRow">
            <div style={{ marginRight: 20 }} className="cardstarItem2Banner2" />
            <div style={{ marginRight: 20 }} className="cardstarItem2Banner3" />
            <div style={{ marginRight: 20 }} className="cardstarItem2Banner4" />
          </div>
          <div style={{ flex: 1, paddingBottom: 20 }} className="cardstarItem2FlexRow">
            <div className="cardstarItem2FlexCenter" >
              <div style={{ marginTop: 20, textAlign: 'center' }} className="cardstarItem2ThemeWordLarge">第一步</div>
              <div style={{ marginTop: 20, textAlign: 'center', paddingLeft: 30, paddingRight: 50 }} className="cardstarItem2BlackWordSmall">认证方式{'\n'}一系列一瞬签名评级员会检查您的卡的真实性</div>
            </div>
            <div className="cardstarItem2FlexCenter" >
              <div style={{ marginTop: 20, textAlign: 'center' }} className="cardstarItem2ThemeWordLarge">第二步</div>
              <div style={{ marginTop: 20, textAlign: 'center', paddingLeft: 30, paddingRight: 50 }} className="cardstarItem2BlackWordSmall">等级{'\n'}如果您的卡通过身份验证，则一瞬签名会以1-10的等级对每张卡的状况进行评分，最好为10。</div>
            </div>
            <div className="cardstarItem2FlexCenter" >
              <div style={{ marginTop: 20, textAlign: 'center' }} className="cardstarItem2ThemeWordLarge">第三步</div>
              <div style={{ marginTop: 20, textAlign: 'center', paddingLeft: 30, paddingRight: 50 }} className="cardstarItem2BlackWordSmall">封装形式{'\n'}一瞬签名将每张卡封装在自己的密封，卡的等级和认证编号显示在一瞬签名标签上。</div>
            </div>
          </div>
          <div style={{ marginTop: 80, textAlign: 'center' }} className="cardstarItem2ThemeWordLarge">评级卡评分细节</div>
        </div>
        {this._renderStandScore()}
        <div className="cardstarItem2FitContainer">
          <div className="cardstarItem2FlexRow" style={{ marginTop: 80 }}>
            <div style={{ flex: 0.7 }}>
              <div className="cardstarItem2Banner6" />
            </div>
            <div style={{ flex: 1, paddingRight: 30, marginTop: 50 }}>
              <div style={{ marginTop: 20, paddingLeft: 30 }} className="cardstarItem2BlackWord">
                找出您的一瞬签名分级交易卡的价值
              </div>
              <div style={{ marginTop: 20, paddingLeft: 30 }} className="cardstarItem2BlackWordSmall">
                &nbsp;&nbsp;&nbsp;&nbsp;
                了解有关一瞬签名提供的三种重要资源的更多信息，这些资源可帮助您确定交易卡的价值。无论您是寻找复古，现代还是非运动型交易卡的价值，一瞬签名都能为您提供答案。
              </div>
            </div>
          </div>
          <div className="cardstarItem2FlexRow" style={{ marginBottom: 80, marginTop: 120 }}>
            <div style={{ flex: 1, paddingLeft: 30 }}>
              <div style={{ marginTop: 20, paddingLeft: 10 }} className="cardstarItem2BlackWord">
                4.与我们保持联系
              </div>
              <div style={{ marginTop: 20, paddingLeft: 30 }} className="cardstarItem2BlackWordSmall">
                &nbsp;&nbsp;&nbsp;&nbsp;
                在社交媒体上与我们保持联系将帮助您随时了解我们的最新促销，活动，赠品和其他特别优惠。立即关注我们！
              </div>
            </div>
            <div style={{ flex: 1, justifyContent: 'center', display: 'flex', marginTop: 30 }}>
              <div className="cardstarItem2Banner7" />
            </div>
          </div>
        </div>
        <Footer />
      </div>
    )
  }
}

