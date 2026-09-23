import React, { Component } from 'react'
import { Button, Form, Input, message, Modal, Select } from 'antd'
import { postData } from '@http';
import { regExpConfig } from '@reg';
import md5 from 'js-md5';

const { Option } = Select;

const FormItem = Form.Item;

@Form.create({})

export default class UpdateUser extends Component {
  constructor(props) {
    super(props)
    this.state = {
      loading: false,
    };
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  // 组件已经加载到dom中
  componentDidMount() {
    this.props.form.resetFields();
  }

  handleSubmit(e) {
    e.preventDefault()
    this.props.form.validateFields((errors, values) => {
      if (errors) {
        return;
      }
      this.setState({ loading: true }, () => {
        // 更新
        const params = {
          id: this.props.values.id,
        }
        if (this.props.type === 'edit') {
          params.phone = values.phone
        } else if (this.props.type === 'editPwd') {
          if (values.password !== values.password2) {
            message.warning('密码不一致')
            this.setState({ loading: false });
            return;
          }
          params.pwd = md5(values.password)
        }
        postData('smartAdminUpdateUser', params).then((data) => {
          message.success('更新成功');
          this.props.handleOk && this.props.handleOk();
          this.setState({ loading: false });
        }).catch((err) => {
          message.warning(err.message);
          this.setState({ loading: false });
        });
      })
    })
  }

  footer() {
    return (
      <div>
        <Button type="primary" onClick={this.handleSubmit} loading={this.state.loading}>确定</Button>
        <Button onClick={this.props.onCancel}>取消</Button>
      </div>
    )
  }

  render() {
    const {
      visible, onCancel, title, values,
    } = this.props;
    const { getFieldDecorator } = this.props.form;
    const formItemLayout = {
      labelCol: { span: 5 },
      wrapperCol: { span: 17 },
    };
    return (
      <Modal
        visible={visible}
        title={title}
        footer={this.footer()}
        onCancel={onCancel}
      >
        <Form onSubmit={this.handleSubmit}>
          {
            this.props.type === 'edit' ? (
              <FormItem {...formItemLayout} label="手机号" hasFeedback>
                {getFieldDecorator('phone', {
                  initialValue: values ? values.phone : '',
                  rules: [
                    { pattern: regExpConfig.phoneNo, message: '手机号格式不正确' },
                  ],
                })(<Input placeholder="请输入手机号" maxLength={11} />)}
              </FormItem>
            ) : null
          }
          {
            this.props.type === 'editPwd' ? (
              <div>
                <FormItem {...formItemLayout} label="密码" hasFeedback>
                  {getFieldDecorator('password', {
                    initialValue: '',
                    rules: [
                      {
                        required: true, min: 6, max: 16, message: '密码为6-16个字符',
                      },
                      { pattern: regExpConfig.pwd, message: '密码由6-16位数字或者字母组成' },
                    ],
                  })(<Input placeholder="请输入密码" type="password" maxLength={16} />)}
                </FormItem>
                <FormItem {...formItemLayout} label="确认密码" hasFeedback>
                  {getFieldDecorator('password2', {
                    initialValue: '',
                    rules: [
                      {
                        required: true, min: 6, max: 16, message: '密码为6-16个字符',
                      },
                      { pattern: regExpConfig.pwd, message: '密码由6-16位数字或者字母组成' },
                    ],
                  })(<Input placeholder="请输入确认密码" type="password" maxLength={16} />)}
                </FormItem>
              </div>
            ) : null
          }

        </Form>
      </Modal>
    )
  }
}
