import React, { Component } from 'react'
import { Button, Form, Input, message, Modal, Select } from 'antd'
import { postData } from '@http';
import { getData } from '../../../../service/http';

const { confirm } = Modal

const { Option } = Select;

const FormItem = Form.Item;

@Form.create({})

export default class UpdateAudit extends Component {
  constructor(props) {
    super(props)
    this.state = {
      loading: false,
    };
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleReject = this.handleReject.bind(this);
  }

  // 组件已经加载到dom中
  componentDidMount() {
    this.props.form.resetFields();
  }

  // 拒绝审核
  handleReject(e) {
    e.preventDefault()
    const params = { id: this.props.values.id };
    const that = this;
    confirm({
      title: '提示',
      content: '确认拒绝审核吗？',
      onOk() {
        that.setState({ loading: true })
        getData('smartAdminRejectAudits', params).then((data) => {
          message.success('操作成功');
          that.props.handleOk && that.props.handleOk();
          that.setState({ loading: false });
        }).catch((err) => {
          message.warning(err.message);
        });
      },
    })
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
        postData('smartAdminUpdateAuditPass', params).then((data) => {
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
        <Button type="danger" onClick={this.handleReject} loading={this.state.loading}>拒审</Button>
        <Button type="primary" style={{ marginLeft: '20px' }} onClick={this.handleSubmit} loading={this.state.loading}>通过</Button>
        {/* <Button onClick={this.props.onCancel}>取消</Button> */}
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
          <img src={values.frontUrl} className="update-audit-img" />
          <img src={values.backUrl} className="update-audit-img" style={{ marginLeft: '10px' }} />
          <Form onSubmit={this.handleSubmit}>
            <div style={{ flex: 1, display: 'flex', flexDirection: 'row', flexWrap: 'wrap' }}>
              <FormItem {...formItemLayout} label="快捷编号" hasFeedback>
                {getFieldDecorator('alias', {
                  initialValue: values ? values.alias : '',
                  rules: [
                    {
                      required: true, message: '快捷编号不能为空',
                    },
                  ],
                })(<Input placeholder="请输入快捷编号" maxLength={10} />)}
              </FormItem>
              <FormItem {...formItemLayout} label="卡牌编号" hasFeedback>
                {getFieldDecorator('code', {
                  initialValue: values ? values.code : '',
                  rules: [
                    {
                      required: true, message: '卡牌编号不能为空',
                    },
                  ],
                })(<Input placeholder="请输入卡牌编号" maxLength={10} />)}
              </FormItem>
              <FormItem {...formItemLayout} label="总系列名称" hasFeedback>
                {getFieldDecorator('serial', {
                  initialValue: values ? values.serial : '',
                  rules: [
                    {
                      required: true, message: '总系列名称不能为空',
                    },
                  ],
                })(<Input placeholder="请输入总系列名称" maxLength={50} />)}
              </FormItem>
              <FormItem {...formItemLayout} label="子系列名称" hasFeedback>
                {getFieldDecorator('subserial', {
                  initialValue: values ? values.subserial : '',
                  rules: [
                    {
                      required: true, message: '子系列名称不能为空',
                    },
                  ],
                })(<Input placeholder="请输入子系列名称" maxLength={50} />)}
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
