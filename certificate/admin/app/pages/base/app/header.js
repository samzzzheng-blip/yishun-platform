
import React, { Component } from 'react'
import { /* Link, */ browserHistory } from 'react-router'
import { Menu, Dropdown, Button, Modal, message, Icon, Row, Col } from 'antd'
import { brandName } from '@config'
import { getData } from '../../../service/http';


const { confirm } = Modal

export default class Header extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      // loading: false,
      editPasswordMadalIsOpen: false,
    }
    this.handleLogout = this.handleLogout.bind(this)
  }

  // 组件已经加载到dom中
  componentDidMount() {

  }

  // 登出
  handleLogout() {
    // const { config } = this.props
    // const self = this
    confirm({
      title: '提示',
      content: '确认退出登录吗？',
      onOk() {
        getData('logout', {}).then((data) => {
          message.success('退出登陆成功');
          sessionStorage.setItem('userinfo', '');
          localStorage.removeItem('token');
          ['gMenuList','gAddMenuList','gUpdateMenuList','gDeleteMenuList','gExportMenuList','leftNav'].forEach(key=>sessionStorage.removeItem(key));
          browserHistory.push('/login');
        }).catch((err) => {
          message.error(err.message);
        });
      },
    })
  }

  // 取消修改密码弹窗
  cancel = () => {
    this.setState({ editPasswordMadalIsOpen: false })
  }

  // 确认修改密码弹窗
  handleOk = () => {
    this.setState({ editPasswordMadalIsOpen: false })
  }

  // 修改密码弹窗显示
  editPasswordOpen = () => {
    this.setState({ editPasswordMadalIsOpen: true })
  }

  logoClick = () => {
    // const nav = JSON.parse(sessionStorage.getItem('gMenuList'))
    // if (nav[0] && nav[0].children && nav[0].children[0].children && nav[0].children[0].children[0] && nav[0].children[0].children[0].resKey) {
    //   hashHistory.push(nav[0].children[0].children[0].resKey)
    //   sessionStorage.setItem('topMenuReskey', nav[0].resKey)
    // }
    // if (nav[0] && nav[0].children && nav[0].children[0].resKey) {
    //   hashHistory.push(nav[0].children[0].resKey)
    // } else {
    //   hashHistory.push('/')
    // }
    // console.log(nav)
    // hashHistory.push()
  }

  render() {
    const username = localStorage.getItem('username')

    // console.log(JSON.parse(sessionStorage.getItem('userinfo')))
    const { gMenuList, topMenuReskey } = this.props
    const topKey = topMenuReskey
    return (
      <header id="navbar">
        <div id="navbar-container" className="boxed">
          <Row className="row">
            <Col span={20}>
              <div className="navbar-brand" title={brandName} onClick={this.logoClick}>
                <span className="brand-title">
                  <span className="brand-text"><span className="logo" />{brandName}</span>
                </span>
              </div>
              <nav className="topMenus hide">
                {
                  gMenuList && gMenuList.map((item, index) => (<span
                    className={item.resKey === topKey ? 'topMenu on' : 'topMenu'}
                    key={item.resKey}
                    onClick={() => this.props.topMenuClick(item, index)}
                  >{item.resName}</span>))
                }
              </nav>
            </Col>
            <Col span={4} className="col">
              <ul>
                {this.props.workflowMode&&<li className="grading-menu-toggle"><Button icon="menu" aria-label="后台菜单" aria-controls="mainnav-container" aria-expanded={!!this.props.mobileNavOpen} onClick={this.props.onMobileMenuToggle}>菜单</Button></li>}
                <li>
                  <a className="ant-dropdown-link"><Icon type="user" />您好,{username}</a>
                </li>
                <li>
                  <Button type="primary" size="small" onClick={this.handleLogout}>退出登录</Button>
                </li>
              </ul>
            </Col>
          </Row>
        </div>
      </header>
    )
  }
}
