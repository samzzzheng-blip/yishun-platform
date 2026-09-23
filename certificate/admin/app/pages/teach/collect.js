import React, { Component } from 'react'
import '@styles/collect.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';

export default class collect extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 3,
      descSelectIndex: 0,
    }
  }

  _clickTitle(index) {
    this.setState({
      descSelectIndex: index,
    });
  }

  _renderArrow(index) {
    const arrowView = index === this.state.descSelectIndex ? (
      <div className="collectArrowSelect" />
    ) : null;
    return arrowView
  }

  _renderDescTitles() {
    return (
      <div className="collectDescTitleContainer">
        <div className="collectDescTitleImgContainer">
          <div className="collectDescTitle1" onClick={() => this._clickTitle(0)} >
            {this._renderArrow(0)}
          </div>
          <div className="collectDescTitle2" onClick={() => this._clickTitle(1)}>
            {this._renderArrow(1)}
          </div>
          <div className="collectDescTitle3" onClick={() => this._clickTitle(2)}>
            {this._renderArrow(2)}
          </div>
        </div>
      </div>
    )
  }

  _renderDescPrice() {
    const priceInfo = [
      { number: 0, price: 299 },
      { number: 5, price: 249 },
      { number: 20, price: 199 },
      { number: 50, price: 149 },
      { number: 200, price: 99 },
    ];
    const priceInfoView = priceInfo.map((info, index) => {
      const numberText = `${info.number}张以上`
      const color = info.number > 0 ? 'white' : 'transparent'
      return (
        <div className="collectDescPriceItem">
          <div style={{ color: color, fontSize: 20, fontWeight: 'bold', marginTop: 40 }}>{numberText}</div>
          <div className="collectPriceContainer">
            <div style={{ color: 'white', fontSize: 50, fontWeight: 'bold' }}>{info.price}</div>
            <div>
              <div style={{ color: 'white', fontSize: 14, fontWeight: 'bold', alignSelf: 'flex-start' }}>¥</div>
              <div style={{
                color: 'white', fontSize: 20, fontWeight: 'bold', alignSelf: 'flex-end', marginTop: 15,
              }}
              >/张</div>
            </div>
          </div>
          <div className="collectDescPriceImg" />
        </div>
      )
    })
    return (
      <div className="collectDescContent">
        {/* <div className="collectPriceTitleContainer"> */}
        {/*  <div style={{color:'white',fontSize:40,marginRight:10,fontWeight:'bold'}}>分级价格</div> */}
        {/*  <div style={{alignSelf:'center'}}> */}
        {/*    <div style={{color:'white',fontSize:14}}>我们常用的服务级别的每张评级卡费用</div> */}
        {/*    <div style={{color:'white',fontSize:20,fontWeight:'bold'}}>评级时间3-5天</div> */}
        {/*  </div> */}
        {/* </div> */}
        {/* <div className="collectDescPriceLabelContainer"> */}
        {/*  {priceInfoView} */}
        {/* </div> */}
        <div className="collectPrice1" />
        <div className="collectPrice2" />
        <div className="collectPrice3" />
      </div>
    )
  }

  _renderDescRule() {
    return (
      <div className="collectRuleContainer" />
    )
  }

  _renderDescFeature() {
    return (
      <div className="collectDescFeatureContainer">
        <div className="collectDescFeatureSample0">
          <div className="collectDescFeatureText0">图示一</div>
          <div className="collectDescFeatureSampleImg" />
        </div>
        <div className="collectDescFeatureText1">
          &nbsp;&nbsp;&nbsp;&nbsp;
          如图示一存在的问题：
          {'\n'}
          &nbsp;&nbsp;&nbsp;&nbsp;
          问题1：很多收藏都暴露在空气之中，那么对于纸质收藏品的KILLER是空气中的氧气与水分，也少不了我们平时摩擦所留下的痕迹。纸张大都数以木材为原料制成的，所以纸张里含有许多木材纤维素，纤维素本来就是白色的，空气中放置久了，就与空气中的氧气结合变成黄色。
          {'\n'}
          &nbsp;&nbsp;&nbsp;&nbsp;
          问题2：纸质收藏品也是非常容易受潮，褶皱，在空气中难免会受潮。
          {'\n'}
          &nbsp;&nbsp;&nbsp;&nbsp;
          问题3：边缘受损、折痕、中心过度摩擦等破坏元素存在。
        </div>
        <div className="collectDescFeatureModuleImg" />
        <div className="collectDescFeatureText2">
          &nbsp;&nbsp;&nbsp;&nbsp;
          一瞬签名卡砖特点：自身素质极高能够给予卡纸类的收藏品存在保护作用，卡砖可塑封将收藏品与空气隔离，这也达到了与水分分离，可谓一举多得，提供了多重保护性。
          {'\n'}
          &nbsp;&nbsp;&nbsp;&nbsp;
          无论是高空坠落还是在地面摩擦这一点都不影响你的收藏品。
          {'\n'}
          &nbsp;&nbsp;&nbsp;&nbsp;
          一瞬签名卡砖服务也是非常迅速，能够及时给顾客一个良好的态度。
        </div>
      </div>
    )
  }

  _renderDescContents() {
    let contentView;
    if (this.state.descSelectIndex === 0) {
      contentView = this._renderDescPrice();
    } else if (this.state.descSelectIndex === 1) {
      contentView = this._renderDescRule();
    } else {
      contentView = this._renderDescFeature();
    }
    return (
      <div>
        {contentView}
      </div>
    )
  }

  render() {
    return (
      <div className="collect">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div className="collectContainer1">
          <div className="collectGrayBg" />
          <div style={{ marginTop: 20, marginBottom: 20, zIndex: 2 }} className="collectFontThemeTitle">一瞬签名一起记录您的收藏品的价值</div>
          <div className="collectCardContainer">
            {/* <div className="collectCardContainerBg2"/> */}
            {/* <div className="collectCard1"/> */}
            {/* <div className="collectCard2"/> */}
            {/* <div className="collectCard3"/> */}
            {/* <div className="collectCard4"/> */}
            {/* <div className="collectCard5"/> */}
            {/* <div className="collectCard6"/> */}
          </div>
          <div className="collectCardFitContainer">
            <div style={{ marginTop: 40 }} className="collectFontWhiteTitle">它是真实的，它有什么价值？</div>
            <div style={{ marginTop: 10 }} className="collectFontWhiteTitle">这些代表了收藏家的终极问题。</div>
            <div style={{ marginTop: 10 }} className="collectFontWhiteContent">一瞬签名长期以来一直在回答收藏品的真实性和等级问题。现在，一瞬签名通过为收藏家提供对其一瞬签名认证的收藏产品进行专业，独立的评估，从而完成任务。</div>
            <div className="collectFontWhiteTitle" style={{ marginTop: 30, marginBottom: 30, fontSize: 28 }} >通过一瞬签名评估保护您的收藏</div>
          </div>
        </div>
        <div className="collectDescShadow">
          {this._renderDescTitles()}
          {this._renderDescContents()}
        </div>
        <Footer />
      </div>
    )
  }
}
