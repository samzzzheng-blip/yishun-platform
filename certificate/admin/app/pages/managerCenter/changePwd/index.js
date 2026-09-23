
import React, { Component } from 'react'
import {
  Form, Input, Tooltip, Icon, Cascader, Select, Row, Col, Checkbox, Button, AutoComplete, message,
} from 'antd';

import { regExpConfig } from '@reg'
import { postData } from '@http';
import md5 from 'js-md5';

@Form.create({})
// 声明组件  并对外输出
export default class app extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props) {
    super(props)
  }

  handleSubmit = (e) => {
    e.preventDefault();
    this.props.form.validateFields((err, values) => {
      if (!err) {
        if (values.newpwd !== values.repwd) {
          message.warning('新密码不一致');
          return;
        }
        if (values.newpwd === values.oldpwd) {
          message.warning('原密码不能和新密码输入一样');
          return;
        }
        values.newpwd = md5(values.newpwd);
        values.oldpwd = md5(values.oldpwd);
        values.repwd = md5(values.repwd);
        postData('modifyPwd', values).then((data) => {
          message.success('更新成功');
          this.props.form.setFieldsValue({
            oldpwd: '',
            newpwd: '',
            repwd: '',
          });
        }).catch((err) => {
          message.warning(err.message);
        });
      }
    });
  }

  render() {
    const { getFieldDecorator } = this.props.form;
    const formItemLayout = {
      labelCol: {
        span: 2,
        offset: 0,
      },
      wrapperCol: {
        span: 4,
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
      <Form style={{ marginTop: 30 }} onSubmit={this.handleSubmit}>
        <Form.Item
          {...formItemLayout}
          label="原密码"
        >
          {getFieldDecorator('oldpwd', {
            rules: [
              {
                required: true, min: 6, max: 16, message: '密码为6-16个字符',
              },
              { pattern: regExpConfig.pwd, message: '密码由6-16位数字或者字母组成' },
            ],
          })(<Input addonBefore={<Icon type="lock" />} placeholder="请输入原密码" type="password" />)}
        </Form.Item>
        <Form.Item
          {...formItemLayout}
          label="新密码"
        >
          {getFieldDecorator('newpwd', {
            rules: [
              {
                required: true, min: 6, max: 16, message: '密码为6-16个字符',
              },
              { pattern: regExpConfig.pwd, message: '密码由6-16位数字或者字母组成' },
            ],
          })(<Input addonBefore={<Icon type="lock" />} placeholder="请输入新密码" type="password" />)}
        </Form.Item>
        <Form.Item
          {...formItemLayout}
          label="新密码确认"
        >
          {getFieldDecorator('repwd', {
            rules: [
              {
                required: true, min: 6, max: 16, message: '密码为6-16个字符',
              },
              { pattern: regExpConfig.pwd, message: '密码由6-16位数字或者字母组成' },
            ],
          })(<Input addonBefore={<Icon type="lock" />} placeholder="请再输一次新密码" type="password" />)}
        </Form.Item>
        <Form.Item {...tailFormItemLayout}>
          <Button type="primary" htmlType="submit">提交</Button>
        </Form.Item>
      </Form>
    );
  }
}
