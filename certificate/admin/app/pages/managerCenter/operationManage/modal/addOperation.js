
import React, { Component } from 'react';
import { Button, Form, Input, message, Modal } from 'antd';
import { postData } from '@http';
import { regExpConfig } from '@reg';

const FormItem = Form.Item;

@Form.create({})

export default class Index extends Component {
  constructor(props) {
    super(props);
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
          postData('updateOperation', params).then((data) => {
            message.success('更新成功');
            this.props.handleOk && this.props.handleOk();
            this.setState({ loading: false });
          }).catch((err) => {
            message.warning(err.message);
            this.setState({ loading: false });
          });
        } else {
          // 新增
          postData('addOperation', values).then((data) => {
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
          <FormItem {...formItemLayout} label="操作名" hasFeedback>
            {getFieldDecorator('name', {
              initialValue: values ? values.name : '',
              rules: [
                {
                  required: true,
                },
              ],
            })(<Input placeholder="请输入操作名" maxLength={20} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="操作编号" hasFeedback>
            {getFieldDecorator('code', {
              initialValue: values ? values.code : '',
              rules: this.props.type === 'edit' ? [] : [
                {
                  required: true,
                },
              ],
            })(<Input placeholder="请输入操作编号" maxLength={50} disabled={this.props.type === 'edit'} />)}
          </FormItem>
        </Form>
      </Modal>
    )
  }
}
