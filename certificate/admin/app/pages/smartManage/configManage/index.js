import React, { Component } from 'react'
import { Button, Form, Input, Layout, message, Modal } from 'antd'
import { getData, postData } from '@http';

const { confirm } = Modal
const FormItem = Form.Item
const { Content } = Layout

@Form.create({})
// 声明组件  并对外输出
export default class ConfigManage extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props) {
    super(props);
    this.state = {
      aiSet: {},
    }
  }

  // 组件即将加载
  componentWillMount() {

  }

  // 组件已经加载到dom中
  componentDidMount() {
    this.getData();
  }

  // 获取数据
  getData() {
    getData('smartAdminQueryAiSet').then((data) => {
      if (data) {
        this.setState({
          aiSet: data,
        });
      }
    }).catch((err) => {
      message.warning(err.message);
    });
  }

  // 更新数据
  handleSubmit = (e) => {
    e.preventDefault();
    this.props.form.validateFields((errors, values) => {
      if (errors) {
        return;
      }
      if (!values.aiQuestion) {
        message.warning('提问词不能为空')
        return
      }
      if (!values.aiAskUrl) {
        message.warning('访问接口不能为空')
        return
      }
      const params = {
        aiQuestion: values.aiQuestion,
        aiSystemSet: values.aiSystemSet || '',
        aiAskUrl: values.aiAskUrl,
      }
      postData('smartAdminUpdateAiSet', params).then((data) => {
        message.success('更新成功')
      })
    })
  }

  render() {
    const { getFieldDecorator } = this.props.form
    const formItemLayout = {
      labelCol: {
        span: 2,
        offset: 0,
      },
      wrapperCol: {
        span: 16,
        offset: 0,
      },
    };
    const tailFormItemLayout = {
      wrapperCol: {
        span: 2,
        offset: 2,
      },
    };
    return (
      <div className="page page-scrollfix page-usermanage">
        <Layout>
          <Layout className="page-body">
            <Content>
              <h3 className="page-title">
                AI设置管理
              </h3>
              <Form style={{ marginTop: 30 }} onSubmit={this.handleSubmit}>
                <Form.Item
                  {...formItemLayout}
                  label="系统提示词"
                >
                  {getFieldDecorator('aiSystemSet', {
                    initialValue: this.state.aiSet ? this.state.aiSet.aiSystemSet : '',
                    rules: [
                      {
                        required: false,
                      },
                    ],
                  })(<Input.TextArea rows={8} placeholder="请输入" />)}
                </Form.Item>
                <Form.Item
                  {...formItemLayout}
                  label="提问词"
                >
                  {getFieldDecorator('aiQuestion', {
                    initialValue: this.state.aiSet ? this.state.aiSet.aiQuestion : '',
                    rules: [
                      {
                        required: true,
                      },
                    ],
                  })(<Input.TextArea rows={8} placeholder="请输入" />)}
                </Form.Item>
                <Form.Item
                  {...formItemLayout}
                  label="访问接口"
                >
                  {getFieldDecorator('aiAskUrl', {
                    initialValue: this.state.aiSet ? this.state.aiSet.aiAskUrl : '',
                    rules: [
                      {
                        required: true,
                      },
                    ],
                  })(<Input placeholder="请输入" />)}
                </Form.Item>
                <Form.Item {...tailFormItemLayout}>
                  <Button type="primary" htmlType="submit">提交</Button>
                </Form.Item>
              </Form>
            </Content>
          </Layout>
        </Layout>
      </div>
    )
  }
}
