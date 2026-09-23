import React, { Component } from 'react';
import { Button, Form, Input, message, Modal, TreeSelect } from 'antd';
import { getData, postData } from '@http';
import { regExpConfig } from '@reg';

const { SHOW_PARENT } = TreeSelect;

const FormItem = Form.Item;

@Form.create({})

export default class Index extends Component {
  constructor(props) {
    super(props);
    this.state = {
      loading: false,
      chooseMenu: [],
      menuList: [],
      permissionData: null, // {view:[menuId1、menuId2],add:...,update:...,delete:...}
    };
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handlePermissionChange = this.handlePermissionChange.bind(this);
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

  // 组件已经加载到dom中
  componentDidMount() {
    this.props.form.resetFields();
    getData('queryMenuList', null).then((data) => {
      if (data) {
        const root = [];
        this.setState({
          menuList: this.getChildrenList(root, data),
        });
      }
    }).catch((err) => {
      message.warning(err.message);
    });

    if (this.props.values && this.props.values.id) {
      const param = {
        roleId: this.props.values.id,
      }
      getData('queryPermissionByRoleId', param).then((data) => {
        let addArr = [],
          updateArr = [],
          deleteArr = [],
          exportArr = [],
          viewArr = [];
        if (data && data.length > 0) {
          data.map((item) => {
            if (item.operation.code === 'view') {
              viewArr.push(item);
            } else if (item.operation.code === 'add') {
              addArr.push(item);
            } else if (item.operation.code === 'update') {
              updateArr.push(item);
            } else if (item.operation.code === 'delete') {
              deleteArr.push(item);
            } else if (item.operation.code === 'export') {
              exportArr.push(item);
            }
          })
        }
        const permissionData = {
          view: viewArr,
          add: addArr,
          update: updateArr,
          delete: deleteArr,
          export: exportArr,
        };
        this.setState({
          permissionData: permissionData,
        });
      }).catch((err) => {
        message.warning(err.message);
      });
    }
  }

  getInitValue(arr) {
    const value = []
    if (arr && arr.length > 0) {
      arr.map((item) => {
        if (item.menu != null) {
          value.push(item.menu.id)
        }
      })
    }
    return value
  }

  handlePermissionChange(value) {
    this.setState({ chooseMenu: value })
  }

  handleSubmit(e) {
    e.preventDefault()
    this.props.form.validateFields((errors, values) => {
      if (errors) {
        return;
      }
      values.addPermission = values.addPermission ? values.addPermission.join(',') : null;
      values.updatePermission = values.updatePermission ? values.updatePermission.join(',') : null;
      values.deletePermission = values.deletePermission ? values.deletePermission.join(',') : null;
      values.viewPermission = values.viewPermission ? values.viewPermission.join(',') : null;
      values.exportPermission = values.exportPermission ? values.exportPermission.join(',') : null
      this.setState({ loading: true }, () => {
        if (this.props.type === 'edit') {
          // 更新
          const params = {
            ...values,
            id: this.props.values.id,
          }
          postData('updateRole', params).then((data) => {
            message.success('更新成功');
            this.props.handleOk && this.props.handleOk();
            this.setState({ loading: false });
          }).catch((err) => {
            message.warning(err.message);
            this.setState({ loading: false });
          });
        } else {
          // 新增
          postData('addRole', values).then((data) => {
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

    const treeProps = {
      treeData: this.state.menuList,
      value: this.state.chooseMenu,
      onChange: this.handlePermissionChange,
      treeCheckable: true,
      showCheckedStrategy: SHOW_PARENT,
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
        bodyStyle={{ height: 460 }}
      >
        <Form onSubmit={this.handleSubmit}>
          <FormItem {...formItemLayout} label="角色名" hasFeedback>
            {getFieldDecorator('name', {
              initialValue: values ? values.name : '',
              rules: [
                {
                  required: true,
                },
              ],
            })(<Input placeholder="请输入角色名" maxLength={50} disabled={this.props.type === 'edit' && values.level <= 1} />)}
          </FormItem>
          <div style={{ display: this.props.type === 'edit' ? 'none' : '' }}>
            <FormItem {...formItemLayout} label="角色编号" hasFeedback>
              {getFieldDecorator('code', {
                initialValue: values ? values.code : '',
                rules: this.props.type === 'edit' ? [] : [
                  {
                    required: true,
                  },
                ],
              })(<Input placeholder="请输入角色编号(英文)" maxLength={10} />)}
            </FormItem>
          </div>
          <FormItem {...formItemLayout} label="角色等级" hasFeedback>
            {getFieldDecorator('level', {
              initialValue: values ? values.level : '2',
              rules: [
                {
                  required: true,
                },
                { pattern: regExpConfig.num, message: '格式为数字' },
              ],
            })(<Input placeholder="请输入角色等级" maxLength={10} disabled="true" />)}
          </FormItem>
          <FormItem {...formItemLayout} label="查看权限" >
            {getFieldDecorator('viewPermission', {
              initialValue: this.state.permissionData && this.state.permissionData.view ? this.getInitValue(this.state.permissionData.view) : null,
            })(<TreeSelect {...treeProps} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="新增权限" >
            {getFieldDecorator('addPermission', {
              initialValue: this.state.permissionData && this.state.permissionData.add ? this.getInitValue(this.state.permissionData.add) : null,
            })(<TreeSelect {...treeProps} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="更新权限" >
            {getFieldDecorator('updatePermission', {
              initialValue: this.state.permissionData && this.state.permissionData.update ? this.getInitValue(this.state.permissionData.update) : null,
            })(<TreeSelect {...treeProps} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="删除权限" >
            {getFieldDecorator('deletePermission', {
              initialValue: this.state.permissionData && this.state.permissionData.delete ? this.getInitValue(this.state.permissionData.delete) : null,
            })(<TreeSelect {...treeProps} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="导出权限" >
            {getFieldDecorator('exportPermission', {
              initialValue: this.state.permissionData && this.state.permissionData.export ? this.getInitValue(this.state.permissionData.export) : null,
            })(<TreeSelect {...treeProps} />)}
          </FormItem>
        </Form>
      </Modal>
    )
  }
}
