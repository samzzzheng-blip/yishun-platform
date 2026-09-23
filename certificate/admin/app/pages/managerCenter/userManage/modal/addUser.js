
import React, { Component } from 'react'
import { Button, Form, Input, message, Upload, Icon, Modal, DatePicker, Select } from 'antd'
import { postData } from '@http';

const { Option } = Select;
import { regExpConfig } from '@reg';
import moment from 'moment';

const FormItem = Form.Item;
import api from '@service/api';
import md5 from 'js-md5';
import { getData } from '@http';

@Form.create({})

export default class Index extends Component {
  constructor(props) {
    super(props)
    this.state = {
      loading: false,
      role: this.props.values && this.props.values.role ? this.props.values.role.code : null,
      roleList: [],
    };
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleRoleChange = this.handleRoleChange.bind(this);
    this._renderRoleListView = this._renderRoleListView.bind(this);
  }

  // 组件已经加载到dom中
  componentDidMount() {
    this.props.form.resetFields();
    getData('queryRoleList', null).then((data) => {
      if (data) {
        this.setState({
          roleList: data,
        });
      }
    }).catch((err) => {
      message.warning(err.message);
    });
  }

  handleRoleChange(value) {
    this.setState({ role: value })
  }

  _renderRoleListView() {
    return this.state.roleList.map(role => <Option value={role.code}>{role.name}</Option>)
  }

  handleSubmit(e) {
    e.preventDefault()
    this.props.form.validateFields((errors, values) => {
      if (errors) {
        return;
      }
      const role = {
        code: values.role,
      }
      values.role = role;
      this.setState({ loading: true }, () => {
        if (this.props.type === 'edit') {
          // 更新
          const params = {
            ...values,
            id: this.props.values.id,
          }
          postData('updateUser', params).then((data) => {
            message.success('更新成功');
            this.props.handleOk && this.props.handleOk();
            this.setState({ loading: false });
          }).catch((err) => {
            message.warning(err.message);
            this.setState({ loading: false });
          });
        } else {
          // 新增
          values.password = md5(values.password);
          postData('addUser', values).then((data) => {
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
          <FormItem {...formItemLayout} label="用户名" hasFeedback>
            {getFieldDecorator('username', {
              initialValue: values ? values.username : '',
              rules: [
                {
                  required: true, min: 4, max: 10, message: '用户名为4-10个字符',
                },
                { pattern: regExpConfig.policeNo, message: '账号4-10位数字或字母组成' },
              ],
            })(<Input placeholder="请输入用户名" maxLength={50} disabled={this.props.type === 'edit'} />)}
          </FormItem>
          <div style={{ display: this.props.type === 'edit' ? 'none' : '' }}>
            <FormItem {...formItemLayout} label="密码" hasFeedback>
              {getFieldDecorator('password', {
                initialValue: values ? values.password : '',
                rules: this.props.type === 'edit' ? [] : [
                  {
                    required: true, min: 6, max: 16, message: '密码为6-16个字符',
                  },
                  { pattern: regExpConfig.pwd, message: '密码由6-16位数字或者字母组成' },
                ],
              })(<Input placeholder="请输入密码" type="password" maxLength={50} />)}
            </FormItem>
          </div>
          <FormItem {...formItemLayout} label="角色" hasFeedback>
            {getFieldDecorator('role', {
              initialValue: this.state.role ? this.state.role : null,
              rules: [{ required: true, message: '请输入角色' }],
            })(this.state.roleList ? (
              <Select style={{ width: 120 }} onChange={this.handleRoleChange}>
                {this._renderRoleListView()}
              </Select>
            ) : null)}
          </FormItem>
          <FormItem {...formItemLayout} label="备注" hasFeedback>
            {getFieldDecorator('remark', {
              initialValue: values ? values.remark : '',
            })(<Input placeholder="请输入备注" maxLength={100} />)}
          </FormItem>
        </Form>
      </Modal>
    )
  }
}
