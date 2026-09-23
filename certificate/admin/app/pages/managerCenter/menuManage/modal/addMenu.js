
import React, { Component } from 'react';
import { Button, Form, Input, message, Modal, TreeSelect } from 'antd';
import { postData, getData } from '@http';
import { regExpConfig } from '@reg';

const FormItem = Form.Item;
const { TreeNode } = TreeSelect;

@Form.create({})

export default class Index extends Component {
  constructor(props) {
    super(props);
    this.state = {
      loading: false,
      menuList: [],
      chooseParent: this.props.values && this.props.values.parentId ? new Array(`${this.props.values.parentId}`) : null,
    };
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleMenuChange = this.handleMenuChange.bind(this);
  }

  // 组件已经加载到dom中
  componentDidMount() {
    this.props.form.resetFields();
    this.getData();
  }

  getChildrenList(root, treeDatas) {
    if (treeDatas) {
      for (let i = 0, len = treeDatas.length; i < len; i++) {
        if (treeDatas[i].children == null) {
          const child = {
            title: treeDatas[i].name,
            value: treeDatas[i].id,
            key: treeDatas[i].id,
          };
          root.push(child);
        } else {
          const child = {
            title: treeDatas[i].name,
            value: treeDatas[i].id,
            key: treeDatas[i].id,
            children: this.getChildrenList([], treeDatas[i].children),
          };
          root.push(child);
        }
      }
      return root;
    }
    return [];
  }


  getData(callback) {
    const param = {
      cId: this.props.values ? this.props.values.id : null,
    };
    getData('queryMenuList', param).then((data) => {
      if (data) {
        const root = [];
        this.setState({
          menuList: this.getChildrenList(root, data),
        });
      }
      callback && callback()
    }).catch((err) => {
      message.warning(err.message);
      callback && callback()
    });
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
          postData('updateMenu', params).then((data) => {
            message.success('更新成功');
            this.props.handleOk && this.props.handleOk();
            this.setState({ loading: false });
          }).catch((err) => {
            message.warning(err.message);
            this.setState({ loading: false });
          });
        } else {
          // 新增
          postData('addMenu', values).then((data) => {
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

  handleMenuChange(value) {
    this.setState({
      chooseParent: value,
    });
    // this.props.form.setFieldsValue({
    //   'parentId': value
    // });
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
    const treeProps = {
      treeData: this.state.menuList,
      value: this.state.chooseParent,
      onChange: this.handleMenuChange,
      treeCheckable: false,
      searchPlaceholder: 'Please select',
      style: {
        width: '100%',
      },
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
          <FormItem {...formItemLayout} label="菜单名称" hasFeedback>
            {getFieldDecorator('name', {
              initialValue: values ? values.name : '',
              rules: [
                {
                  required: true,
                },
              ],
            })(<Input placeholder="请输入菜单名称" maxLength={20} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="菜单路径" hasFeedback>
            {getFieldDecorator('path', {
              initialValue: values ? values.path : '',
              rules: [
                {
                  required: true,
                },
              ],
            })(<Input placeholder="请输入菜单路径" maxLength={20} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="上级目录" >
            {getFieldDecorator('parentId', {
              initialValue: this.state.chooseParent ? this.state.chooseParent[0] : '',
              rules: [{ required: true, message: '请选择上级目录' }],
            })(<TreeSelect {...treeProps} />)}
          </FormItem>
        </Form>
      </Modal>
    )
  }
}
