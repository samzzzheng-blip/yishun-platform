import React, { Component } from 'react'
import '@styles/preview.less'
import { Player } from 'video-react';
import 'video-react/dist/video-react.css'

export default class preview extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      url: '',
      type: 1,
      loading: false,
    }
  }

  componentDidMount() {
    this.setState({
      loading: true,
    })
    const storedParams = sessionStorage.getItem('previewParams');
    if (storedParams) {
      const params = JSON.parse(storedParams)
      this.setState({
        loading: false,
        url: params.href,
        type: params.type || 1,
      })
    }
  }

  render() {
    return (
      <div className="preview" onContextMenu={e => e.preventDefault()}>
        {
          this.state.loading ? null : this.state.type === 1 ?
            <img src={this.state.url} className="media" alt="img" />
            :
            <Player autoPlay aspectRatio="16:9" muted playsInline>
              <source src={this.state.url} />
            </Player>
        }
        <div className="water" />
      </div>
    )
  }
}
