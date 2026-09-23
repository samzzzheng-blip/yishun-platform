import React, { Component } from 'react'
import { Button, Form, Input, message, Modal, Select } from 'antd'
import { postData } from '@http';

const { Option } = Select;

const FormItem = Form.Item;

@Form.create({})

export default class UpdateScore extends Component {
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
        const params = Object.assign({}, this.props.values, values)
        postData('smartAdminUpdateScore', params).then((data) => {
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
      labelCol: { span: 8 },
      wrapperCol: { span: 16 },
    };
    return (
      <Modal
        visible={visible}
        title={title}
        footer={this.footer()}
        onCancel={onCancel}
        width={window.innerHeight * 1.35}
      >
        <div className="update-audit-body">
          <img src={values.frontUrl} className="update-audit-img" style={{ marginRight: '20px' }} />
          <img src={values.backUrl} className="update-audit-img" style={{ marginRight: '20px' }} />
          <Form onSubmit={this.handleSubmit}>
            <div style={{ flex: 1, display: 'flex', flexDirection: 'row', flexWrap: 'wrap' }}>
              <FormItem {...formItemLayout} label="表面分数" hasFeedback>
                {getFieldDecorator('surface', {
                  initialValue: values ? values.surface : '',
                  rules: [
                    {
                      required: true, message: '表面分数不能为空',
                    },
                  ],
                })(<Input placeholder="请输入表面分数" maxLength={50} />)}
              </FormItem>
              <FormItem {...formItemLayout} label="居中分数" hasFeedback>
                {getFieldDecorator('center', {
                  initialValue: values ? values.center : '',
                  rules: [
                    {
                      required: true, message: '居中分数不能为空',
                    },
                  ],
                })(<Input placeholder="请输入居中分数" maxLength={50} />)}
              </FormItem>
              <FormItem {...formItemLayout} label="角落分数" hasFeedback>
                {getFieldDecorator('edge', {
                  initialValue: values ? values.edge : '',
                  rules: [
                    {
                      required: true, message: '角落分数不能为空',
                    },
                  ],
                })(<Input placeholder="请输入角落分数" maxLength={50} />)}
              </FormItem>
              <FormItem {...formItemLayout} label="边缘分数" hasFeedback>
                {getFieldDecorator('corner', {
                  initialValue: values ? values.corner : '',
                  rules: [
                    {
                      required: true, message: '边缘分数不能为空',
                    },
                  ],
                })(<Input placeholder="请输入边缘分数" maxLength={50} />)}
              </FormItem>
              <FormItem {...formItemLayout} label="总分" hasFeedback>
                {getFieldDecorator('score', {
                  initialValue: values ? values.score : '',
                  rules: [
                    {
                      required: true, message: '总分不能为空',
                    },
                  ],
                })(<Input placeholder="请输入总分" maxLength={50} />)}
              </FormItem>
            </div>
          </Form>
        </div>
      </Modal>
    )
  }
}
