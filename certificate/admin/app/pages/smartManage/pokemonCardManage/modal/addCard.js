import React, { Component } from 'react'
import { Button, Form, Input, message, Modal, Select } from 'antd'
import { postData } from '@http';

const { Option } = Select;

const FormItem = Form.Item;

@Form.create({})

export default class AddCard extends Component {
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
        if (this.props.type === 'edit') {
          // 更新
          const params = {
            ...values,
            id: this.props.values.id,
          }
          postData('smartAdminUpdateCard', params).then((data) => {
            message.success('更新成功');
            this.props.handleOk && this.props.handleOk();
            this.setState({ loading: false });
          }).catch((err) => {
            message.warning(err.message);
            this.setState({ loading: false });
          });
        } else {
          // 新增
          const params = {
            ...values,
          }
          postData('smartAdminAddCard', params).then((data) => {
            message.success('新增成功');
            this.setState({ loading: false });
            this.props.handleOk && this.props.handleOk();
          }).catch((err) => {
            this.setState({ loading: false });
            message.warning(err.message);
          });
        }
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
        bodyStyle={{ height: 400 }}
      >
        <Form onSubmit={this.handleSubmit}>
          <FormItem {...formItemLayout} label="快捷编号" hasFeedback>
            {getFieldDecorator('alias', {
              initialValue: values ? values.alias : '',
              rules: [
                {
                  required: true, message: '快捷编号不能为空',
                },
              ],
            })(<Input placeholder="请输入快捷编号" maxLength={50} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="卡牌编号" hasFeedback>
            {getFieldDecorator('code', {
              initialValue: values ? values.code : '',
              rules: [
                {
                  required: true, message: '卡牌编号不能为空',
                },
              ],
            })(<Input placeholder="请输入卡牌编号" maxLength={50} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="宝可梦名称" hasFeedback>
            {getFieldDecorator('name', {
              initialValue: values ? values.name : '',
              rules: [
                {
                  required: true, message: '宝可梦名称不能为空',
                },
              ],
            })(<Input placeholder="请输入宝可梦名称" maxLength={50} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="宝可梦属性" hasFeedback>
            {getFieldDecorator('attribute', {
              initialValue: values ? values.attribute : '',
              rules: [
                {
                  required: true, message: '宝可梦属性不能为空',
                },
              ],
            })(<Input placeholder="请输入宝可梦属性" maxLength={50} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="稀有度" hasFeedback>
            {getFieldDecorator('rarity', {
              initialValue: values ? values.rarity : '',
              rules: [
                {
                  required: true, message: '稀有度不能为空',
                },
              ],
            })(<Input placeholder="请输入稀有度" maxLength={50} />)}
          </FormItem>
        </Form>
      </Modal>
    )
  }
}
