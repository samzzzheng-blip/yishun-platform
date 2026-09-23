import React, { Component } from 'react'
import '@styles/sailulu.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';

export default class sailulu extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 3,
    }
  }

  render() {
    return (
      <div className="sailulu">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div style={{ position: 'relative' }}>
          <div style={{ position: 'absolute' }} className="sailuluMore" onClick={() => { window.open('https://detail.tmall.com/item.htm?spm=a1z10.3-b-s.w4011-23523516113.29.43761489oculpH&id=639026212036&rn=7d85951d6837ce4cdbcadf86dda3f642&abbucket=6&skuId=4586663096299') }}>立即送评</div>
        </div>
        <div className="sailuluFitContainer">
          <div className="sailuluBanner1" />
        </div>
        {/*  <div className="sailuluBanner2" /> */}
        {/*  <div className="sailuluBanner3" /> */}
        {/*  <div style={{margin:20}} className="sailuluThemeWord">是不是觉得他们失去了原有的色彩，不必担心，一瞬签名帮你解决！</div> */}
        {/*  <div className="sailuluBanner4" /> */}
        {/*  <div style={{margin:20}} className="sailuluThemeWord">将赛璐璐塑封在一瞬签名卡砖内收纳的空间更大，主体物更加的明星{"\n"}赛璐璐胶片线稿都是不可复制的，可以说是孤品，并且它们是实际动漫制作使用的原材料，堪比动漫中的一帧。赛璐璐卡砖能够更好体现赛璐璐的美观。</div> */}
        {/*  <div className="sailuluBanner5" /> */}
        {/*  <div style={{margin:20}} className="sailuluThemeWord">透明的材质就会形成不同的背景色，效果就完全不同，可根据自身喜爱的颜色搭配装饰，随着心情更换背景色都不是什么事</div> */}
        {/*  <div style={{marginLeft:15,marginBottom:50}} className="sailuluRedWord">注：卡装均为透明材料，赛璐璐如果是透明纸，他的效果就会更加美观</div> */}
        <Footer />
      </div>
    )
  }
}
