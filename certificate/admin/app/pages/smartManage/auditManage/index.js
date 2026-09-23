import React, { Component } from 'react'
import { Form, Layout, message, Modal } from 'antd'
import TableList from '@tableList'
import UpdateAudit from './modal/updateAudit'
import { getData } from '@http';
import moment from 'moment';

const FormItem = Form.Item
const { Content } = Layout

@Form.create({})
// 声明组件  并对外输出
export default class AuditManage extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props) {
    super(props);
    this.onSelectChange = this.onSelectChange.bind(this);
    this.state = {
      auditUpdateVisible: false,
      spinloading: true,
      moduletitle: '',
      moduletype: '',
      searchKey: {
        pageSize: 10,
        pageNo: 1,
      },
      auditList: [],
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
    getData('smartAdminQueryAuditList', params).then((data) => {
      if (data && data.list) {
        this.setState({
          auditList: data.list,
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

  // 编辑
  handleEdit(record) {
    this.setState({
      auditUpdateVisible: true,
      moduletype: 'edit',
      moduletitle: '编辑',
      rowData: record,
    })
  }

  // 新增或编辑用户保存
  handleOk() {
    this.setState({
      auditUpdateVisible: false,
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
    this.setState({ auditUpdateVisible: false })
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
        title: 'ID',
        dataIndex: 'id',
        key: 'id',
        width: 80,
      },
      {
        title: '正面图片',
        dataIndex: 'frontUrl',
        key: 'frontUrl',
        width: 100,
        render: frontUrl => (frontUrl ? <img src={frontUrl} style={{ width: '80px', height: '80px' }} /> : ''),
      },
      {
        title: '背面图片',
        dataIndex: 'backUrl',
        key: 'backUrl',
        width: 100,
        render: backUrl => (backUrl ? <img src={backUrl} style={{ width: '80px', height: '80px' }} /> : ''),
      },
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
        title: '总系列',
        dataIndex: 'serial',
        key: 'serial',
        width: 100,
      },
      {
        title: '子系列',
        dataIndex: 'subserial',
        key: 'subserial',
        width: 100,
      },
      {
        title: '宝可梦名称',
        dataIndex: 'name',
        key: 'name',
        width: 100,
      },
      {
        title: '属性',
        dataIndex: 'attribute',
        key: 'attribute',
        width: 80,
      },
      {
        title: '稀有度',
        dataIndex: 'rarity',
        key: 'rarity',
        width: 80,
      },
      {
        title: '表面',
        dataIndex: 'surface',
        key: 'surface',
        width: 80,
      },
      {
        title: '居中',
        dataIndex: 'center',
        key: 'center',
        width: 80,
      },
      {
        title: '角落',
        dataIndex: 'edge',
        key: 'edge',
        width: 80,
      },
      {
        title: '边缘',
        dataIndex: 'corner',
        key: 'corner',
        width: 80,
      },
      {
        title: '总分',
        dataIndex: 'score',
        key: 'score',
        width: 80,
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
        width: 100,
        fixed: 'right',
        render: (text, record, index) => (
          <span>
            {
              <span onClick={() => this.handleEdit(record)}>
                <a>审核</a>
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
                待审核管理
              </h3>
              <div className="page-content has-pagination table-scrollfix">
                <TableList
                  rowKey="id"
                  columns={this.renderColumn()}
                  dataSource={this.state.auditList}
                  currentPage={this.state.searchKey.pageNo}
                  pageSize={this.state.searchKey.pageSize}
                  scroll={{ y: true, x: true }}
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
          this.state.auditUpdateVisible ?
            <UpdateAudit
              visible={this.state.auditUpdateVisible}
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
