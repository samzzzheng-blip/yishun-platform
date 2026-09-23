
import React, { Component } from 'react'
import { connect } from 'react-redux'
import { browserHistory/* , Link */ } from 'react-router'
import { Spin, Form, Icon, Input, Button, Row, Col, message } from 'antd'
import { regExpConfig } from '@reg'
import { brandName } from '@config'
import { clearGformCache2 } from '@actions/common'
import QueuiAnim from 'rc-queue-anim'
import md5 from 'js-md5';

// import '@styles/base.less'
import '@styles/login.less'
import { postData } from '@http';
import { safeRatingReturn } from '../../utils/ratingNavigation';

const FormItem = Form.Item

@connect((state, props) => ({
  config: state.config,
  loginResponse: state.loginResponse,
}))
@Form.create({
  onFieldsChange(props, items) {},
})

export default class Login extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props, context) {
    super(props)
    this.state = {
      loading: false,
      isCertificates: false,
      show: true,
    }
  }

  componentWillMount() {
    this.props.dispatch(clearGformCache2({}))
  }
  componentDidMount() {
    if(safeRatingReturn(this.props.location.query.returnTo)!=='/manage') {
      document.documentElement.classList.add('rating-auth-document')
      this.viewport=document.querySelector('meta[name="viewport"]')
      if(this.viewport) {this.oldViewport=this.viewport.getAttribute('content');this.viewport.setAttribute('content','width=device-width, initial-scale=1')}
    }
  }
  componentWillUnmount() {
    document.documentElement.classList.remove('rating-auth-document')
    if(this.viewport) {if(this.oldViewport===null)this.viewport.removeAttribute('content');else this.viewport.setAttribute('content',this.oldViewport)}
  }

  // #region 收缩业务代码功能

  handleSubmit(e, isCertificates) {
    e.preventDefault()
    this.props.form.validateFields((err, values) => {
      if (!err) {
        const query = this.props.form.getFieldsValue();
        query.password = md5(query.password);
        this.setState({ loading: true })
        postData('login', query).then((data) => {
          this.setState({ loading: false });
          localStorage.setItem('token', data.token);
          localStorage.setItem('username', data.userName);
          localStorage.setItem('role', data.role)
          browserHistory.replace(safeRatingReturn(this.props.location.query.returnTo));
        }).catch((err) => {
          message.warning(err.message);
          this.setState({ loading: false })
        });
      }
    })
  }

  // #endregion

  render() {
    const { getFieldDecorator } = this.props.form
    console.log(this.props.loginResponse)
    return (
      <div className="login-container">
        <div className="flexcolumn">
          <div className="login-header" key="header">
            <div className="slogan">
              <QueuiAnim className="flexcolumn" type={['right', 'left']} key="p">
                {
                  this.state.show ? [
                    <p key="0" className="title">{brandName}
                    </p>,
                  ] : null
                }
              </QueuiAnim>
            </div>
            {/* <Logo /> */}
          </div>
          <div className="login-main">
            <QueuiAnim delay={300} type="bottom" key="row">
              {
                this.state.show ? [
                  <Row key="row0">
                    <Col span={8} />
                    <Col span={8}>
                      <Spin spinning={this.state.loading}>
                        <Form onSubmit={e => this.handleSubmit(e, this.state.isCertificates)}>
                          {!this.state.isCertificates ?
                            (<div>
                              <FormItem hasFeedback>
                                {getFieldDecorator('username', {
                                  rules: [
                                    {
                                      required: true, min: 4, max: 10, message: '用户名为4-10个字符',
                                    },
                                    { pattern: regExpConfig.policeNo, message: '账号4-10位数字或字母组成' },
                                  ],
                                })(<Input addonBefore={<Icon type="user" />} placeholder="请输入用户名" type="text" />)}
                              </FormItem>
                              <FormItem hasFeedback>
                                {getFieldDecorator('password', {
                                  rules: [
                                    {
                                      required: true, min: 6, max: 16, message: '密码为6-16个字符',
                                    },
                                    { pattern: regExpConfig.pwd, message: '密码由6-16位数字或者字母组成' },
                                  ],
                                })(<Input addonBefore={<Icon type="lock" />} placeholder="请输入密码" type="password" />)}
                              </FormItem>
                              <FormItem>
                                <Button type="primary" htmlType="submit" className="cert-btn">登录</Button>
                              </FormItem>
                            </div>) :
                            <FormItem>
                              <Button type="primary" htmlType="submit">证书登录</Button>
                            </FormItem>
                          }
                        </Form>
                      </Spin>
                    </Col>
                    <Col span={8} />
                  </Row>,
                ] : null
              }
            </QueuiAnim>
          </div>
        </div>
      </div>
    )
  }
}
