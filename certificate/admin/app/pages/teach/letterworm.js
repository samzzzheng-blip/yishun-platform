import React, { Component } from 'react'
import '@styles/letterworm.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';

export default class letterworm extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 3,
    }
  }

  render() {
    return (
      <div className="letterworm">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div className="letterwormBanner1" />
        <div style={{ position: 'relative' }}>
          <div style={{ position: 'absolute' }} className="letterwormMore" onClick={() => { window.open('https://detail.tmall.com/item.htm?spm=a1z10.3-b-s.w4011-23523516113.29.43761489oculpH&id=639026212036&rn=7d85951d6837ce4cdbcadf86dda3f642&abbucket=6&skuId=4586663096299') }}>立即送评</div>
        </div>
        <div className="letterwormFitContainer">
          <div className="letterwormBanner2" />
          <div className="letterwormBanner3" />
        </div>

        {/* <div style={{marginLeft:100,marginTop:20,marginBottom:20}} className="letterwormThemeTitle">信虫？是信里的虫子吗？</div> */}
        {/* <div className="letterwormFlexRow"> */}
        {/*  <div style={{flex:1,marginLeft: 30}} className="letterwormThemeSmallWord"> */}
        {/*    &nbsp;&nbsp;&nbsp;&nbsp; */}
        {/*    何为是信虫？可能很多人都答不上来，甚至没有听说过这个词，因为在生活中几乎接触不到，就算在网上，关于"信虫"一词的具体涵义，也很难找到准确的答案。 */}
        {/*    {"\n"}&nbsp;&nbsp;&nbsp;&nbsp; */}
        {/*    那信虫到底是什么呢？在中国信虫群体之间有这样朦胧又具体的解释：通过寄信或发送电子邮件的方式联系名人，并从名人的回信中获得签名等纪念收藏品，或通过寄信、发送电子邮件向名人索取签名的方式，来手机签名作为收藏，这类群体都叫做信虫。 */}
        {/*    {"\n"}&nbsp;&nbsp;&nbsp;&nbsp; */}
        {/*    粉丝邮件是由崇拜者或"粉丝"发给公众人物，尤其是名人的邮件。作为对粉丝的支持和赞赏的回报，公众人物可以发送签名海报、照片、回信或便条感谢粉丝的鼓励、礼物和支持。粉丝邮件可以通过邮件、电子邮件、社交媒体和其他平台发送给公众人物，让粉丝和用户可以与自己喜爱的公众人物交流。 */}
        {/*    {"\n"}&nbsp;&nbsp;&nbsp;&nbsp; */}
        {/*    总之，信虫可以说是一类爱好群体，也可以说是一种收藏途径，主体是粉丝和名人，"一封信、一张签名、一份礼物、一件收藏......"这些载体互相传递，它们见证了粉丝的感情，见证了名人对粉丝的哎，见证了一件藏品的诞生，也见证了一个时代，信虫"涉猎"的范围囊括了体育、娱乐、文艺等社会各界公众人物，设置包括宗教及各国政要，这就是信虫！ */}
        {/*  </div> */}
        {/*  <div className="letterwormBanner2"/> */}
        {/* </div> */}
        {/* <div style={{marginTop:100,marginBottom:50,marginLeft:50,flex:1}} className="letterwormFlexRow"> */}
        {/*  <div className="letterwormWordLeft"> */}
        {/*    <div style={{textAlign:'center'}} className="letterwormThemeWord">罗杰 · 费德勒</div> */}
        {/*    <div style={{textAlign:'center'}} className="letterwormThemeWord">右边图是官方回信</div> */}
        {/*    <div className="letterwormThemeSmallWord" style={{fontSize:14,marginTop:30}}> */}
        {/*      &nbsp;&nbsp;&nbsp;&nbsp; */}
        {/*      一瞬签名独家信虫卡砖，正面为官方回信的收藏品，背面可提供照片或者LOGO，看个人喜爱可diy背面，正上方是一瞬签名元素，带有IDLO的信息和类型，并且是通过笔迹鉴定的哦！同时赋予一瞬签名的防伪商标，可到官方网址进行查询。 */}
        {/*    </div> */}
        {/*  </div> */}
        {/*  <div style={{flex:1}} className="letterwormBanner3"/> */}
        {/* </div> */}
        <Footer />
      </div>
    )
  }
}
