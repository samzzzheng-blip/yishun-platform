import React, { Component } from 'react'
import { Button, Form, Input, Layout, message, Modal } from 'antd'
import TableList from '@tableList'
import UpdateUser from './modal/updateUser'
import { getData, postData } from '@http';

const { confirm } = Modal
const FormItem = Form.Item
const { Content } = Layout

@Form.create({})
// 声明组件  并对外输出
export default class UserManage extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props) {
    super(props);
    this.onSelectChange = this.onSelectChange.bind(this);
    this.state = {
      userUpdateVisible: false,
      spinloading: true,
      moduletitle: '',
      moduletype: '',
      searchKey: {
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
    this.getData();
  }

  // 获取用户列表数据
  getData(callback) {
    const params = {
      page: this.state.searchKey.pageNo,
      pageSize: this.state.searchKey.pageSize,
    };
    if (this.state.searchKey.phone) {
      params.phone = this.state.searchKey.phone
    }
    getData('smartAdminQueryUserList', params).then((data) => {
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
    const phone = this.props.form.getFieldValue('phone')
    this.setState({
      spinloading: true,
      searchKey: {
        ...this.state.searchKey,
        phone: phone,
        pageNo: 1,
      },
    }, () => { this.getData(() => { this.setState({ spinloading: false }) }) })
  }

  // 编辑
  handleEdit(record) {
    this.setState({
      userUpdateVisible: true,
      moduletype: 'edit',
      moduletitle: '编辑',
      rowData: record,
    })
  }

  handleResetPwd(id) {
    this.setState({
      userUpdateVisible: true,
      moduletype: 'editPwd',
      moduletitle: '重置密码',
      rowData: { id },
    })
  }

  handleSwitch(record) {
    const params = {
      id: record.id,
      disabled: record.disabled === 1 ? 0 : 1,
    };
    postData('smartAdminUpdateUser', params).then((data) => {
      message.success('更新成功');
      this.getData();
    }).catch((err) => {
      message.warning(err.message);
    });
  }

  // 编辑用户保存
  handleOk() {
    this.setState({
      userUpdateVisible: false,
      searchKey: {
        ...this.state.searchKey,
        pageNo: 1,
      },
    }, () => {
      this.getData()
    })
  }

  // 用户modal取消
  handleCancel = () => {
    this.setState({ userUpdateVisible: false })
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
        title: '手机号',
        dataIndex: 'phone',
        key: 'phone',
        width: '100',
      },
      {
        title: '昵称',
        dataIndex: 'nick',
        key: 'nick',
        width: '100',
      },
      {
        title: '头像',
        dataIndex: 'head',
        key: 'head',
        width: '100',
        render: head => (head ? <img src={head} style={{ width: '80px', height: '80px' }} /> : ''),
      },
      {
        title: '状态',
        dataIndex: 'disabled',
        key: 'disabled',
        width: '100',
        render: disabled => <span>{disabled === 1 ? '停用' : '正常'}</span>,
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
              <span onClick={() => this.handleResetPwd(record.id)}>
                <a>重置密码</a>
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
                    <FormItem>
                      {getFieldDecorator('phone')(<Input className="input-base-width" size="default" placeholder="请输入手机号" allowClear />)}
                    </FormItem>
                    <Button type="primary" htmlType="submit">搜索</Button>
                    {/* <Button type="primary" style={{ marginLeft: '0px' }} onClick={() => this.handleAdd()}> 新增</Button> */}
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
          this.state.userUpdateVisible ?
            <UpdateUser
              visible={this.state.userUpdateVisible}
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
