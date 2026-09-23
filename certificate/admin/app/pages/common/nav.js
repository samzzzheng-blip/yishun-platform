
import React, { Component } from 'react'
import '@styles/nav.less'
import { browserHistory } from 'react-router';

export default class Nav extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props);
    this.state = {
      selectNav: this.props.selectNav,
      data: [
        {
          id: 1,
          title: '首页',
          link: '/',
          type: 'router',
        },
        {
          id: 2,
          title: '证书查询',
          link: '/home',
          type: 'router',
        },
        {
          id: 3,
          title: '评级服务',
          link: '/teach',
          type: 'router',
          showArrow: true,
        },
        {
          id: 4,
          title: '关于一瞬',
          link: '/aboutus',
          type: 'router',
        },
        {
          id: 5,
          title: '联系我们',
          link: '/contactus',
          type: 'router',
        },
        {
          id: 6,
          title: '购买商品',
          link: 'https://younongsp.tmall.com/?spm=a1z10.3-b.1997427721.d4918089.590f1489kYtZ6R',
        },
      ],
      menuIsOpen: false,
      menuIsOpen2: false,
    }
  }

  _handleClick = (item) => {
    if (item.type && item.type === 'router') {
      browserHistory.push(item.link);
    } else {
      window.open(item.link);
    }
  }

  _handleMouseOver = () => {
    this.setState({ menuIsOpen: true })
  }

  _handleMouseLeave = () => {
    this.setState({ menuIsOpen: false })
  }

  _handleMouseOver2 = () => {
    this.setState({ menuIsOpen: true, menuIsOpen2: true })
  }

  _handleMouseLeave2 = () => {
    this.setState({ menuIsOpen: false, menuIsOpen2: false })
  }

  _navigate(path) {
    browserHistory.push(path);
    this._handleMouseOver2()
  }

  render() {
    const arrowText = this.state.menuIsOpen ? '▼' : '▶'
    const arrowText2 = '▶'
    const navView = this.state.data.map((item, index) => {
      const color = this.state.selectNav === item.id ? 'red' : '#FFFDFD';
      const arrowView = item.showArrow ?
        (
          <div style={{ color: color, fontSize: 11, alignSelf: 'center', marginLeft: 4 }}>{arrowText}</div>
        ) : null;
      const menuProps = index == 2 ? {
        onMouseOver: this._handleMouseOver,
        onMouseLeave: this._handleMouseLeave,
      } : null
      return (
        <div
          {...menuProps}
          key={index}
          className="navItem"
          onClick={() => this._handleClick(item)}
        >
          <div style={{ color: color }}>{item.title}</div>
          {arrowView}
        </div>
      )
    });
    return (
      <div>
        <nav className="nav">
          {navView}
        </nav>
        <div
          className="navMenu"
          style={{ display: this.state.menuIsOpen ? 'block' : 'none' }}
          onMouseOver={this._handleMouseOver}
          onMouseLeave={this._handleMouseLeave}
        >
          <ul className="ul">
            <li className="li"
              onMouseOver={this._handleMouseOver2}
              onMouseLeave={this._handleMouseLeave2}
              onClick={() => this._navigate('/cardstar')}
            >球星卡砖<span style={{ fontSize: 11, marginLeft: 5 }}>{arrowText2}</span></li>
            <li className="li" onClick={() => this._navigate('/cardstone')}>评级卡砖</li>
            <li className="li" onClick={() => this._navigate('/sailulu')}>赛璐璐卡砖</li>
            <li className="li" onClick={() => this._navigate('/letterworm')}>信虫卡砖</li>
            <li className="li" onClick={() => this._navigate('/diy')}>DIY卡砖</li>
          </ul>
        </div>
        <div
          className="navMenu2"
          style={{ display: this.state.menuIsOpen2 ? 'block' : 'none' }}
          onMouseOver={this._handleMouseOver2}
          onMouseLeave={this._handleMouseLeave2}
        >
          <ul className="ul">
            <li className="li" onClick={() => this._navigate('/cardstarStand')}>评分标准</li>
            <li className="li" onClick={() => this._navigate('/cardstarExchange')}>更换卡具</li>
            <li className="li" onClick={() => this._navigate('/cardstarReGrade')}>重新评级</li>
          </ul>
        </div>
      </div>

    );
  }
}
