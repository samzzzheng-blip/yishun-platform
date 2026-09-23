
import React, { Component } from 'react'
import { Button, Modal, Form, Input, message, Layout } from 'antd'
import TableList from '@tableList'
import AddOperation from './modal/addOperation'

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
      operationAddVisible: false,
      spinloading: true,
      moduletitle: '',
      moduletype: '',
      operationList: [],
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
      keywords: '',
    };
    getData('queryOperationList', params).then((data) => {
      if (data) {
        this.setState({
          operationList: data,
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
      operationAddVisible: true,
      moduletype: 'add',
      moduletitle: '新增',
      rowData: null,
    })
  }

  // 编辑
  handleEdit(record) {
    this.setState({
      operationAddVisible: true,
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
        getData('deleteOperation', params).then((data) => {
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
      operationAddVisible: false,
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
    this.setState({ operationAddVisible: false })
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
        title: '操作编码',
        dataIndex: 'code',
        key: 'code',
        width: '100',
      },
      {
        title: '操作名',
        dataIndex: 'name',
        key: 'name',
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
          </span>
        ),
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
                 操作管理
              </h3>
              <div className="page-header">
                <div className="layout-between">
                  <Form className="flexrow" onSubmit={e => this.handleSearch(e)}>
                    <Button type="primary" style={{ marginLeft: '0px' }} onClick={() => this.handleAdd()}> 新增</Button>
                  </Form>
                </div>
              </div>
              <div className="page-content has-pagination table-scrollfix">
                <TableList
                  rowKey="id"
                  columns={this.renderColumn()}
                  dataSource={this.state.operationList}
                  scroll={{ y: true }}
                />
              </div>
            </Content>
          </Layout>
        </Layout>

        {
          this.state.operationAddVisible ?
            <AddOperation
              visible={this.state.operationAddVisible}
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
