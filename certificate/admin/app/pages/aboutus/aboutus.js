import React, { Component } from 'react'
import '@styles/aboutus.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';

export default class aboutus extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 4,
    }
  }

  render() {
    return (
      <div className="aboutus">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div className="aboutusBlank" />
        <div className="aboutus2">
          <div className="aboutusContainer">
            <div className="title">创始人寄语：</div>
            <div className="content">世界上面的亲笔签名公司有很多，但是发展到现在几乎还是没有公司可以对出售的亲笔签名解释的很清楚。</div><div className="content">证书上总是说这个是亲笔签名，或者笔迹上判断正确，或者我们100%保证真品这样的词汇。</div>
            <div className="content">但是由于签名市场上的丑闻较多，证书的可信度就大打折扣。亲笔签名产业难以发展的原因是买家</div>
            <div className="content">无法判断买到的是否是真迹，所以一瞬签名诞生了，一瞬签名对于亲笔签名的判断将大幅度减少</div>
            <div className="content">人为干预的因素，只以现场图或者证据为核心。一瞬签名对于亲笔签名的判断是严苛的，即使你的物品</div>
            <div className="content">出现在了现场除非有签名动作，或者合适的场景，否则依然不能列入为一瞬签名。即使是我们团队</div>
            <div className="content">或者信任的人拿去后台签完名，然后拿出来对着明星拍照一张，都不能开具出一瞬签名的证书。</div>
            <div className="content">无法出具一瞬签名的证书不代表它不是亲笔签名，只是一瞬签名的概念就是有准确证据的亲笔签名。</div>
            <div className="content">一瞬签名并不会出于笔迹去判断一个物品是否亲笔签名。许多IP签名的场景现场比较混乱，</div>
            <div className="content">所以笔迹有时候存在断笔歪歪扭扭都是正常的情况。且不同年代不同场景名人的个人心情都会影响笔迹。</div>
            <div className="content">以往的证书都是一些文字说明，一瞬签名的证书加入了签名证据图片，并且在网站查询结果中可以看到</div>
            <div className="content">更高清的现场图或者整个签名过程视频。一瞬签名更具收藏价值。市场上带签名瞬间的收藏价值也是比普通</div>
            <div className="content">的亲笔签名高出好几倍，一瞬签名让交易更加简单，人们不必去担心真假的问题，在收藏的时候也收藏了</div>
            <div className="content">一个美好时刻。如果有相关信息，一瞬签名会写明时间地点活动等元素。所以一瞬签名是有故事的亲笔签名。</div>
            <div className="content" />
            <div className="content" />
          </div>
        </div>
        <Footer />
      </div>
    )
  }
}
