
import React, { Component } from 'react'
// import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { browserHistory } from 'react-router'
import { message, LocaleProvider, Alert, Button } from 'antd'
import zhCN from 'antd/lib/locale-provider/zh_CN'
import '@styles/base.less'

import Header from './app/header'
import LeftNav from './app/leftNav'
// import TabList from './app/tabList'
import menuData from '../../configs/menu';
import { getData } from '@http';
import { augmentRatingMenus, hasRatingMenu, canEditRating } from '../../utils/ratingNavigation';

@connect((state, props) => ({}))
export default class App extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      isLeftNavMini: false, // 左侧导航菜单是否mini模式
      leftNav: [], // 左侧菜单列表
      topMenuReskey: 'platformManage', // 默认管理平台
      gMenuList: [], // 当前用户菜单列表
      idRenderChild: false, // 是否加载子组件
      // isHideNav: false, // 是否隐藏左侧菜单
      isIframe: false, // 是否隐藏头部
      mobileNavOpen:false,
      isMobile:window.innerWidth<=640,
    }
    this.startInit = this.startInit.bind(this);
  }

  // 组件已经加载到dom中
  componentDidMount() {
    // antd的message组件 的全局配置
    message.config({
      duration: 3,
    })
    this.loadPermissions()
    window.addEventListener('resize',this.resize)
  }
  resize = () => this.setState({isMobile:window.innerWidth<=640})
  componentWillUnmount() {window.removeEventListener('resize',this.resize)}

  loadPermissions = async () => {
    this.setState({permissionError:'',idRenderChild:false})
    const keys=['gMenuList','gAddMenuList','gUpdateMenuList','gDeleteMenuList','gExportMenuList']
    keys.concat(['leftNav']).forEach(key=>sessionStorage.removeItem(key))
    try {
      const results=await Promise.all([1,2,3,4,5].map(operationId=>getData('queryRoleMenuList',{operationId})))
      results.forEach((data,index)=>sessionStorage.setItem(keys[index],JSON.stringify(data.list||[])))
      const nav=augmentRatingMenus(results[0].list||[],canEditRating(sessionStorage))
      if(!nav.length) throw new Error('当前账号尚未配置可访问的后台菜单，请联系管理员。')
      sessionStorage.setItem('gMenuList',JSON.stringify(nav))
      sessionStorage.setItem('leftNav',JSON.stringify(nav))
      sessionStorage.setItem('topMenuReskey',nav[0].resKey)
      this.startInit()
      if(this.props.location.pathname==='/manage'&&hasRatingMenu(nav)) browserHistory.replace('/rateManage')
    } catch(err) {this.setState({permissionError:err.message||'权限加载失败，请重试'})}
  }

  startInit() {
    // 初始化左侧菜单是mini模式还是正常模式
    if (sessionStorage.getItem('isLeftNavMini') === 'false') {
      this.setState({
        isLeftNavMini: false,
      })
    }
    if (sessionStorage.getItem('isLeftNavMini') === 'true') {
      this.setState({
        isLeftNavMini: true,
      })
    }
    this.init()
  }

  componentWillMount() {

  }

  componentWillReceiveProps(nextProps) {
    if(nextProps.location.pathname!==this.props.location.pathname) this.setState({mobileNavOpen:false})
  }

  init() {
    const { query } = this.props.location
    this.setState({ gMenuList: JSON.parse(sessionStorage.getItem('gMenuList')) })
    this.getMenuId(JSON.parse(sessionStorage.getItem('gMenuList')), this.props.location.pathname.replace('/', ''))
    // 初始化比较当前的顶级菜单属于哪个
    const { topMenuReskey } = this.state
    if (topMenuReskey !== sessionStorage.getItem('topMenuReskey')) {
      this.setState({ topMenuReskey: sessionStorage.getItem('topMenuReskey') })
    }
    this.setState({
      idRenderChild: true,
      // isLeftNavMini: false,
    })

    if (query.mode === 'iframe' || query.key) {
      this.setState({
        isIframe: true,
      })
    } else {
      this.setState({
        isIframe: false,
      })
    }
  }

  // 获取菜单id
  getMenuId = (nav, pathname) => {
    this.topMenuReskeyFlag = '' // 顶级菜单分类
    this.topMenuReskeyChild = [] // 顶级菜单的孩子，也就是当前要显示在左侧页面的菜单
    this.flag = false // 用来保存顶级菜单的标志
    // console.log(nav)
    if (nav && nav.length > 0) {
      this.compare(nav, pathname)
    }
  }

  // 比较方法
  compare(children, pathname) {
    children.map((item) => {
      // console.log(item.resKey)
      if (item.resKey.indexOf('platform') > -1) {
        if (!this.flag && (sessionStorage.getItem('topMenuReskey') !== 'set$')) {
          this.topMenuReskeyFlag = item.resKey
          this.topMenuReskeyChild = item.children
        }
      }
      // eslint-disable-next-line
      const _resKey = `${item.resKey.replace(/[\$\.\?\+\^\[\]\(\)\{\}\|\\\/]/g, '\\$&').replace(/\*\*/g, '[\\w|\\W]+').replace(/\*/g, '[^\\/]+')}$`
      if (new RegExp(_resKey).test(pathname)) {
        // console.log(item.id)
        this.flag = true
        sessionStorage.setItem('menuId', item.id)
        // debugger
        sessionStorage.setItem('topMenuReskey', this.topMenuReskeyFlag)
        this.setState({ /* menuId: item.id,  */topMenuReskey: this.topMenuReskeyFlag })
        return null
      } else if (item.children) {
        this.compare(item.children, pathname)
      }
      return null
    })
  }

  // 左侧是否mini
  isLeftNavMini = (val) => {
    this.setState({
      isLeftNavMini: val,
    }, () => {
      sessionStorage.setItem('isLeftNavMini', val)
    })
  }

  // 顶级菜单点击事件的切换
  topMenuClick = (item, index) => {
    // console.log(item)
    if (!item.children) {
      message.info('顶级菜单至少要有一个下级菜单')
      return
    }
    // sessionStorage.setItem('leftNav', JSON.stringify(item.children))
    // this.setState({ leftNav: item.children })
    sessionStorage.setItem('topMenuReskey', item.resKey)
    this.setState({ topMenuReskey: item.resKey })
    // if (index === 3) {
    //   this.set = true
    // } else {
    //   this.set = false
    // }

    if (item.resKey === 'controlCenter') {
      let hasIndex = false
      item.children.map((i) => {
        if (i.resKey === 'screen$/default') {
          hasIndex = true
        }
      })
      if (hasIndex) {
        browserHistory.push(item.children[0].resKey)
      } else {
        browserHistory.push('mission$/my$')
      }
    } else if (item.children[0] && item.children[0] && item.children[0].children && item.children[0].children[0]) {
      browserHistory.push(item.children[0].children[0].resKey)
    } else {
      browserHistory.push(item.children[0].resKey)
    }
  }

  render() {
    const { location, children } = this.props
    const workflowMode=['/rateIntake','/rateWorkflow','/rateTemporary'].includes(location.pathname)
    const {
      gMenuList, idRenderChild, /* isHideNav, */ isIframe, topMenuReskey, leftNav, isLeftNavMini,
    } = this.state
    // console.log(isIframe)
    return (
      <LocaleProvider locale={zhCN}>
        <div id="container" className={this.state.mobileNavOpen?'grading-mobile-nav-open':''}>
          {this.state.permissionError&&<div style={{padding:24}}><Alert type="error" message={this.state.permissionError}/><Button onClick={this.loadPermissions}>重新加载权限</Button></div>}
          {
            idRenderChild && !isIframe ? <Header
              gMenuList={gMenuList}
              topMenuClick={this.topMenuClick}
              topMenuReskey={this.state.topMenuReskey}
              workflowMode={workflowMode}
              mobileNavOpen={this.state.mobileNavOpen}
              onMobileMenuToggle={()=>this.setState({mobileNavOpen:!this.state.mobileNavOpen})}
            /> : null
          }

          <div className={isIframe ? 'boxed isIframe' : 'boxed'}>
            {workflowMode&&this.state.mobileNavOpen&&<button className="grading-mobile-shade" aria-label="关闭后台菜单" onClick={()=>this.setState({mobileNavOpen:false})}/>}
            <div className={isLeftNavMini ? 'boxed boxed-mini' : 'boxed'}>
              <div id="content-container" className="content-container">
                <div id="page-content">
                  {idRenderChild ? children : null}
                </div>
              </div>
            </div>
            {
              idRenderChild ?
                <LeftNav
                  location={location}
                  leftNavMode={this.isLeftNavMini}
                  leftNav={leftNav}
                  topMenuReskey={topMenuReskey}
                  mobileExpanded={workflowMode&&this.state.isMobile}
                /> : null
            }
          </div>
        </div>
      </LocaleProvider>
    )
  }
}
