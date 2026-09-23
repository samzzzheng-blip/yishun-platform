
import React, { Component } from 'react'
import { Button, Modal, Form, Input, message, Layout } from 'antd'
import TableList from '@tableList'
import AddUser from './modal/addUser'

const { confirm } = Modal
const FormItem = Form.Item
const { Content } = Layout
import { getData, postData } from '@http';
import moment from 'moment';

@Form.create({})
// 声明组件  并对外输出
export default class app extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props) {
    super(props);
    this.onSelectChange = this.onSelectChange.bind(this);
    this.state = {
      userAddVisible: false,
      spinloading: true,
      moduletitle: '',
      moduletype: '',
      searchKey: {
        keyword: '',
        pageSize: 10,
        pageNo: 1,
      },
      userList: [],
      totalCount: 0,
      selectedRowKeys: [],
      rowData: null,
    }
  }

  // 组件即将加载
  componentWillMount() {

  }

  // 组件已经加载到dom中
  componentDidMount() {
    this.props.form.setFieldsValue({ key: '' })

    this.getData();
  }

  // 获取用户列表数据
  getData(callback) {
    const params = {
      page: this.state.searchKey.pageNo,
      pageSize: this.state.searchKey.pageSize,
    };
    if (this.state.searchKey.keyword) {
      params.keywords = this.state.searchKey.keyword;
    }
    getData('queryUserList', params).then((data) => {
      if (data && data.list) {
        this.setState({
          userList: data.list,
          totalCount: data.totalCount,
          selectedRowKeys: [],
        });
      }
      callback && callback()
    }).catch((err) => {
      message.warning(err.message);
      callback && callback()
    });
  }


  // 搜索
  handleSearch(e) {
    e.preventDefault()
    const keyword = this.props.form.getFieldValue('key')
    this.setState({
      spinloading: true,
      searchKey: {
        ...this.state.searchKey,
        keyword: keyword.toLowerCase(),
        pageNo: 1,
      },
    }, () => { this.getData(() => { this.setState({ spinloading: false }) }) })
  }

  // 新增
  handleAdd() {
    this.setState({
      userAddVisible: true,
      moduletype: 'add',
      moduletitle: '新增',
      rowData: null,
    })
  }

  // 编辑
  handleEdit(record) {
    this.setState({
      userAddVisible: true,
      moduletype: 'edit',
      moduletitle: '编辑',
      rowData: record,
    })
  }

  handleSwitch(record) {
    const params = {
      id: record.id,
      disabled: record.disabled === 1 ? 0 : 1,
    };
    postData('updateUserStatus', params).then((data) => {
      message.success('更新成功');
      this.getData();
    }).catch((err) => {
      message.warning(err.message);
    });
  }

  // 删除用户
  handleDelete(id) {
    const params = { id: id };
    if (params.id === '') {
      message.warning('请选择要删除的数据');
      return;
    }
    const that = this;
    confirm({
      title: '提示',
      content: '确认删除吗？',
      onOk() {
        getData('deleteUser', params).then((data) => {
          message.success('删除成功');
          that.getData();
        }).catch((err) => {
          message.warning(err.message);
        });
      },
    })
  }

  // 新增或编辑用户保存
  handleOk() {
    this.setState({
      userAddVisible: false,
      searchKey: {
        ...this.state.searchKey,
        pageNo: 1,
      },
    }, () => {
      this.getData()
    })
  }

  // 新增用户modal取消
  handleCancel = () => {
    this.setState({ userAddVisible: false })
  }

  // 页数改变事件
  pageChange = (newPage) => {
    this.state.searchKey.pageNo = newPage;
    this.getData();
  }

  // 页大小改变事件
  pageSizeChange = (e, pageSize) => {
    this.state.searchKey.pageNo = 1
    this.state.searchKey.pageSize = pageSize
    this.getData()
  }

  // 生成表格头部信息
  renderColumn() {
    return [
      {
        title: '用户名',
        dataIndex: 'username',
        key: 'username',
        width: '100',
      },
      {
        title: '角色',
        dataIndex: 'role',
        key: 'role',
        width: '100',
        render: role => role.name,
      },
      // {
      //   title: '权限',
      //   dataIndex: 'role',
      //   key: 'permissionList',
      //   width: '100',
      //   render: (role) => {
      //      let permissionNames = [];
      //      role.permissionList.map((permission) => {
      //        permissionNames.push(permission.name);
      //      });
      //      return permissionNames.join(",");
      //   }
      // },
      {
        title: '状态',
        dataIndex: 'disabled',
        key: 'disabled',
        width: '100',
        render: (disabled) => {
          if (disabled === 1) {
            return <span>停用</span>
          }
          return <span>正常</span>
        },
      },
      {
        title: '备注',
        dataIndex: 'remark',
        key: 'remark',
        width: '100',
      },
      {
        title: '操作',
        key: 'operate',
        width: '200',
        render: (text, record, index) => (
          <span>
            {
              <span onClick={() => this.handleEdit(record)}>
                <a>编辑</a>
              </span>
            }
            <span className="ant-divider" />
            {
              <span onClick={() => this.handleDelete(record.id)}>
                <a>删除</a>
              </span>
            }
            <span className="ant-divider" />
            {
              <span onClick={() => this.handleSwitch(record)}>
                <a>{record.disabled === 1 ? '启用' : '禁用'}</a>
              </span>
            }
          </span>

        ),
      },
    ]
  }

  onSelectChange = (selectedRowKeys) => {
    this.setState({ selectedRowKeys });
  };

  render() {
    const { getFieldDecorator } = this.props.form
    const rowSelection = {
      selectedRowKeys: this.state.selectedRowKeys,
      onChange: this.onSelectChange,
    };
    return (
      <div className="page page-scrollfix page-usermanage">
        <Layout>
          <Layout className="page-body">
            <Content>
              <h3 className="page-title">
                 用户管理
              </h3>
              <div className="page-header">
                <div className="layout-between">
                  <Form className="flexrow" onSubmit={e => this.handleSearch(e)}>
                    {/* <FormItem> */}
                    {/*  {getFieldDecorator('key')(<Input className="input-base-width" size="default" placeholder="请输入关键字进行搜索" />)} */}
                    {/* </FormItem> */}
                    {/* <Button type="primary" htmlType="submit" >搜索</Button> */}
                    <Button type="primary" style={{ marginLeft: '0px' }} onClick={() => this.handleAdd()}> 新增</Button>
                  </Form>
                </div>
              </div>
              <div className="page-content has-pagination table-scrollfix">
                <TableList
                  rowKey="id"
                  columns={this.renderColumn()}
                  dataSource={this.state.userList}
                  currentPage={this.state.searchKey.pageNo}
                  pageSize={this.state.searchKey.pageSize}
                  scroll={{ y: true }}
                  onChange={this.pageChange}
                  onShowSizeChange={this.pageSizeChange}
                  totalCount={this.state.totalCount}
                  // rowSelection={rowSelection}
                />
              </div>
            </Content>
          </Layout>
        </Layout>

        {
          this.state.userAddVisible ?
            <AddUser
              visible={this.state.userAddVisible}
              title={this.state.moduletitle}
              handleOk={() => this.handleOk()}
              values={this.state.rowData}
              type={this.state.moduletype}
              onCancel={this.handleCancel}
            />
            : null
        }
      </div>
    )
  }
}
