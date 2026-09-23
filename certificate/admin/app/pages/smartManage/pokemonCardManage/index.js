import React, { Component } from 'react'
import { Button, Form, Input, Layout, message, Modal } from 'antd'
import TableList from '@tableList'
import AddCard from './modal/addCard'
import { getData } from '@http';
import moment from 'moment';

const { confirm } = Modal
const FormItem = Form.Item
const { Content } = Layout

@Form.create({})
// 声明组件  并对外输出
export default class PokemonCardManage extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props) {
    super(props);
    this.onSelectChange = this.onSelectChange.bind(this);
    this.state = {
      cardAddVisible: false,
      spinloading: true,
      moduletitle: '',
      moduletype: '',
      searchKey: {
        pageSize: 10,
        pageNo: 1,
      },
      cardList: [],
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
    if (this.state.searchKey.alias) {
      params.alias = this.state.searchKey.alias
    }
    if (this.state.searchKey.code) {
      params.code = this.state.searchKey.code
    }
    getData('smartAdminQueryCardList', params).then((data) => {
      if (data && data.list) {
        this.setState({
          cardList: data.list,
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
    const alias = this.props.form.getFieldValue('alias')
    const code = this.props.form.getFieldValue('code')
    this.setState({
      spinloading: true,
      searchKey: {
        ...this.state.searchKey,
        alias: alias,
        code: code,
        pageNo: 1,
      },
    }, () => { this.getData(() => { this.setState({ spinloading: false }) }) })
  }

  // 新增
  handleAdd() {
    this.setState({
      cardAddVisible: true,
      moduletype: 'add',
      moduletitle: '新增',
      rowData: null,
    })
  }

  // 编辑
  handleEdit(record) {
    this.setState({
      cardAddVisible: true,
      moduletype: 'edit',
      moduletitle: '编辑',
      rowData: record,
    })
  }

  // 新增或编辑用户保存
  handleOk() {
    this.setState({
      cardAddVisible: false,
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
    this.setState({ cardAddVisible: false })
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
        title: '快捷编号',
        dataIndex: 'alias',
        key: 'alias',
        width: 100,
      },
      {
        title: '卡牌编号',
        dataIndex: 'code',
        key: 'code',
        width: 100,
      },
      {
        title: '宝可梦名称',
        dataIndex: 'name',
        key: 'name',
        width: 180,
      },
      {
        title: '宝可梦属性',
        dataIndex: 'attribute',
        key: 'attribute',
        width: 80,
      },
      {
        title: '稀有度',
        dataIndex: 'rarity',
        key: 'rarity',
        width: 100,
      },
      {
        title: '更新时间',
        dataIndex: 'updatedate',
        key: 'updatedate',
        width: 200,
        render: text => moment(text).format('YYYY-MM-DD HH:mm:ss'),
      },
      {
        title: '操作',
        key: 'operate',
        width: 200,
        render: (text, record, index) => (
          <span>
            {
              <span onClick={() => this.handleEdit(record)}>
                <a>编辑</a>
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
                卡牌管理
              </h3>
              <div className="page-header">
                <div className="layout-between">
                  <Form className="flexrow" onSubmit={e => this.handleSearch(e)}>
                    <FormItem>
                      {getFieldDecorator('alias')(<Input style={{ width: '150px', marginRight: '10px' }} size="default" placeholder="请输入快捷编号" allowClear />)}
                    </FormItem>
                    <FormItem>
                      {getFieldDecorator('code')(<Input style={{ width: '150px', marginRight: '10px' }} size="default" placeholder="请输入卡牌编号" allowClear />)}
                    </FormItem>
                    <Button type="primary" htmlType="submit" >搜索</Button>
                    <Button type="primary" style={{ marginLeft: '20px' }} onClick={() => this.handleAdd()}> 新增</Button>
                  </Form>
                </div>
              </div>
              <div className="page-content has-pagination table-scrollfix">
                <TableList
                  rowKey="id"
                  columns={this.renderColumn()}
                  dataSource={this.state.cardList}
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
          this.state.cardAddVisible ?
            <AddCard
              visible={this.state.cardAddVisible}
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
