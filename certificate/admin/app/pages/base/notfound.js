import React, { Component } from 'react'
import { Link, browserHistory } from 'react-router'
import { Progress, Button } from 'antd'

// 声明组件  并对外输出
export default class notfound extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props) {
    super(props)
    this.state = {
      // activeTab: 'pop' ,
    }
  }

  render() {
    return (
      <div className="developing notfound">
        <Progress
          type="circle"
          percent={100}
          format={() => '404'}
          width={200}
          status="active"
        />

        <div className="link ptbig">
          <p className="mbbig"><Link to="/">去首页</Link></p>
          {/* <p className="mbbig"><Link to="/login">go login</Link></p> */}
          {/* <Button type="primary" onClick={() => browserHistory.goBack()}>go back</Button> */}
        </div>
      </div>
    )
  }
}
