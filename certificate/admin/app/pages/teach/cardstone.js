import React, { Component } from 'react'
import '@styles/cardstone.less'
import Header from '../common/header';
import Footer from '../common/footer';
import Nav from '../common/nav';

export default class cardstone extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      selectNav: 3,
    }
  }

  render() {
    return (
      <div className="cardstone">
        <Header />
        <Nav selectNav={this.state.selectNav} />
        <div className="cardstoneBanner1" />
        <div style={{ position: 'relative' }}>
          <div style={{ position: 'absolute' }} className="cardstoneMore" onClick={() => { window.open('https://detail.tmall.com/item.htm?spm=a1z10.3-b-s.w4011-23523516113.29.43761489oculpH&id=639026212036&rn=7d85951d6837ce4cdbcadf86dda3f642&abbucket=6&skuId=4586663096299') }}>立即送评</div>
        </div>
        <div className="cardstoneFitContainer">
          {/* <div style={{marginLeft:30,marginTop:30}} className="cardstoneFlexRow"> */}
          {/*  <div style={{marginTop:20}} className="cardstoneBlackTitle">更换2张效果图</div> */}
          {/*  <div style={{flex:1}} /> */}
          {/*  <div className="cardstoneLabel"/> */}
          {/* </div> */}

          <div className="cardstoneFitContainer">
            <div className="cardstoneBanner2" />
            <div className="cardstoneBanner3" />
            <div className="cardstoneBanner4" />
          </div>

          {/* <div style={{marginLeft:20,marginTop:5}} className="cardstoneFlexRow"> */}
          {/*  <div style={{flex:1}} className="cardstoneBanner3" /> */}
          {/*  <div style={{flex:1}}> */}
          {/*    <div style={{marginTop:50}} className="cardstoneThemeWordBig">李沁签名评级卡砖</div> */}
          {/*    <div className="cardstoneBanner4" /> */}
          {/*    <div style={{marginRight:20}} className="cardstoneThemeWordSmall"> */}
          {/*      &nbsp;&nbsp;&nbsp;&nbsp; */}
          {/*      李沁的评分：9.7，百科浏览次数已高达5KW以上了，很多小伙伴就被迷惑不该在9.9么，我们以百科浏览次数被基础分，演员的积分会有所下降调整，同时也会考虑IDOL的代表作和个人实力，到底是实力派、偶像派还是两者结合体，这都能反映当前IDOL们的收藏价值！ */}
          {/*    </div> */}
          {/*  </div> */}

          {/* </div> */}
          {/* <div style={{marginLeft:10,marginTop:25}} className="cardstoneFlexRow"> */}
          {/*  <div style={{flex:1}}> */}
          {/*    <div className="cardstoneThemeWordSmall"> */}
          {/*      &nbsp;&nbsp;&nbsp;&nbsp; */}
          {/*      右侧是著名导演，在国内还是香港地区都是一流的水准，代表作：《无间道》《头文字D》《中国机长？。那么他的百科浏览次数处于300W，这也是我们前期的一个打分，经过调整后，我们考虑到IDOL们所处的领域以及他们的代表作品等作为评分因素进行一个加分项目。像刘伟强导演这水准是妥妥的上9.0以上的。以上所说的几部代表作品可以说是经典中的经典。 */}
          {/*    </div> */}
          {/*    <div style={{marginTop:10}} className="cardstoneBanner5" /> */}

          {/*  </div> */}
          {/*  <div style={{flex:1}} className="cardstoneBanner6" /> */}

          {/* </div> */}
          {/* <div className="cardstoneBanner7" /> */}
          {/* <div style={{marginBottom:80}} className="cardstoneBanner8" /> */}
        </div>


        <Footer />
      </div>
    )
  }
}
