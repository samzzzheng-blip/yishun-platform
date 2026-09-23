
import React, { Component } from 'react'
import { Button, Modal, Form, Input, message, Layout } from 'antd'
import TableList from '@tableList'
import AddMenu from './modal/addMenu'

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
    this.state = {
      menuAddVisible: false,
      moduletitle: '',
      moduletype: '',
      menuList: [],
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

  getData(callback) {
    getData('queryMenuList', null).then((data) => {
      if (data) {
        this.setState({
          menuList: data,
        });
      }
      callback && callback()
    }).catch((err) => {
      message.warning(err.message);
      callback && callback()
    });
  }

  // 新增
  handleAdd() {
    this.setState({
      menuAddVisible: true,
      moduletype: 'add',
      moduletitle: '新增',
      rowData: null,
    })
  }

  // 编辑
  handleEdit(record) {
    this.setState({
      menuAddVisible: true,
      moduletype: 'edit',
      moduletitle: '编辑',
      rowData: record,
    })
  }

  // 删除
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
        getData('deleteMenu', params).then((data) => {
          message.success('删除成功');
          that.getData();
        }).catch((err) => {
          message.warning(err.message);
        });
      },
    })
  }

  // 新增或编辑保存
  handleOk() {
    this.setState({
      menuAddVisible: false,
      searchKey: {
        ...this.state.searchKey,
        pageNo: 1,
      },
    }, () => {
      this.getData()
    })
  }

  // 新增modal取消
  handleCancel = () => {
    this.setState({ menuAddVisible: false })
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
        title: '菜单id',
        dataIndex: 'id',
        key: 'id',
      },
      {
        title: '菜单名称',
        dataIndex: 'name',
        key: 'name',
      },
      {
        title: '路径',
        dataIndex: 'path',
        key: 'path',
      },
      {
        title: '操作',
        key: 'operate',
        render: (text, record, index) => (record.parentId ? (
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
          </span>
        ) : null),
      },
    ]
  }


  render() {
    const { getFieldDecorator } = this.props.form
    return (
      <div className="page page-scrollfix page-usermanage">
        <Layout>
          <Layout className="page-body">
            <Content>
              <h3 className="page-title">
                 菜单管理
              </h3>
              <div className="page-header">
                <div className="layout-between">
                  <div className="flexrow">
                    <Button type="primary" style={{ marginLeft: '0px' }} onClick={() => this.handleAdd()}> 新增</Button>
                  </div>
                </div>
              </div>
              <div className="page-content has-pagination table-scrollfix">
                <TableList
                  rowKey="id"
                  columns={this.renderColumn()}
                  dataSource={this.state.menuList}
                  scroll={{ y: true }}
                />
              </div>
            </Content>
          </Layout>
        </Layout>

        {
          this.state.menuAddVisible ?
            <AddMenu
              visible={this.state.menuAddVisible}
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
