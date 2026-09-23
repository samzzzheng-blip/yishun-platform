
import React, { Component } from 'react'
import '@styles/header.less'
import { browserHistory } from 'react-router'

export default class Header extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.goLogin = this.goLogin.bind(this);
  }

  goLogin() {
    browserHistory.push('/login');
  }
  render() {
    return (
      <header className="header">
        <div className="logo" />
      </header>
    )
  }
}
