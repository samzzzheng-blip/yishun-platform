
import React, { Component } from 'react'
import { Button, Modal, Form, Input, message, Layout, Spin } from 'antd'
import TableList from '@tableList'
import Addcartoon from './modal/addCartoon'

const { confirm } = Modal
const FormItem = Form.Item
const { Content } = Layout
import { getData, getFullUrl } from '@http';
import moment from 'moment';
import permission from '../../../utils/permission';

@Form.create({})
// 声明组件  并对外输出
export default class app extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props) {
    super(props);
    this.onSelectChange = this.onSelectChange.bind(this);
    this.state = {
      cartoonAddVisible: false,
      spinloading: false,
      moduletitle: '',
      moduletype: '',
      searchKey: {
        keyword: '',
        pageSize: 10,
        pageNo: 1,
      },
      cartoonList: [],
      totalCount: 0,
      selectedRowKeys: [],
      rowData: null,
      hasUpdatePermission: permission.hasUpdatePermission('cartoonManage'),
      hasDeletePermission: permission.hasDeletePermission('cartoonManage'),
      hasAddPermission: permission.hasAddPermission('cartoonManage'),
      hasExportPermission: permission.hasExportPermission('cartoonManage'),
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
      type: 1,
    };
    if (this.state.searchKey.keyword) {
      params.keywords = this.state.searchKey.keyword;
    }
    getData('queryCartoonList', params).then((data) => {
      if (data && data.list) {
        this.setState({
          cartoonList: data.list,
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
        keyword: (keyword || '').trim(),
        pageNo: 1,
      },
    }, () => { this.getData(() => { this.setState({ spinloading: false }) }) })
  }

  // 新增
  handleAdd() {
    this.setState({
      cartoonAddVisible: true,
      moduletype: 'add',
      moduletitle: '新增',
      rowData: null,
    })
  }

  // 编辑
  handleEdit(record) {
    this.setState({
      cartoonAddVisible: true,
      moduletype: 'edit',
      moduletitle: '编辑',
      rowData: record,
    })
  }

  // 删除宝贝
  handleDelete(id) {
    const params = id ? { id: id } : { id: this.state.selectedRowKeys.join(',') };
    if (params.id === '') {
      message.warning('请选择要删除的数据');
      return;
    }
    const that = this;
    confirm({
      title: '提示',
      content: '确认删除吗？',
      onOk() {
        getData('deleteCartoon', params).then((data) => {
          message.success('删除成功');
          that.setState({ selectedRowKeys: [] })
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
      cartoonAddVisible: false,
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
    this.setState({ cartoonAddVisible: false })
  }

  // 页数改变事件
  pageChange = (newPage) => {
    const maxPage = Math.max(1, Math.ceil(this.state.totalCount / this.state.searchKey.pageSize));
    if (!Number.isInteger(newPage) || newPage < 1 || newPage > maxPage) return;
    this.setState({ searchKey: { ...this.state.searchKey, pageNo: newPage } }, () => this.getData());
  }

  // 页大小改变事件
  pageSizeChange = (e, pageSize) => {
    this.setState({ searchKey: { ...this.state.searchKey, pageNo: 1, pageSize } }, () => this.getData());
  }

  exportAllData = () => {
    this.setState({
      spinloading: true,
    })
    const endTime = new Date().getTime()
    getData('exportCartoon', { startTime: 0, endTime }, 60 * 60000).then((data) => {
      this.setState({
        spinloading: false,
      })
      if (data == null) {
        message.error('没有可导出的数据')
      } else {
        window.open(data)
      }
    }).catch((err) => {
      message.warning(err.message);
      this.setState({
        spinloading: false,
      })
    });
  }

  jumpBlankHref = (href) => {
    const w = window.open('about:blank');
    w.location.href = href;
  }

  handleQcode = (certNo) => {
    const param = {
      certNo: certNo,
    };
    fetch(`${getFullUrl('downloadQrcode')}?certNo=${certNo}`, { // downloadFiles 接口请求地址
      method: 'get',
      credentials: 'include',
      headers: new Headers({
        'Content-Type': 'application/json',
        Authorization: localStorage.getItem('token'), // 设置header 获取token
      }),
    }).then((response) => {
      response.blob().then((blob) => {
        const blobUrl = window.URL.createObjectURL(blob);
        const filename = `qrcode_${certNo}.png`;
        const aElement = document.createElement('a');
        document.body.appendChild(aElement);
        aElement.style.display = 'none';
        aElement.href = blobUrl;
        aElement.download = filename;
        aElement.click();
        document.body.removeChild(aElement);
      });
    }).catch((err) => {
      message.warning(err.message);
    });
  }

  // 生成表格头部信息
  renderColumn() {
    return [
      {
        title: '证书编号',
        dataIndex: 'certNumber',
        key: 'certNumber',
        width: '100',
        render: (text, record, index) => (
          <div>
            <span>{text}</span>
            <div>
              <span onClick={() => this.handleQcode(record.certNumber)}>
                <a>二维码下载</a>
              </span>
            </div>

          </div>
        ),
        // fixed: 'left',
      },
      {
        title: '角色名称',
        dataIndex: 'roleName',
        key: 'roleName',
        width: '100',
        // fixed: 'left',
      },
      {
        title: '照片',
        dataIndex: 'imgUrl',
        key: 'imgUrl',
        width: '100',
        // flexed: 'left',
        render: (text) => {
          if (text) {
            return <img src={text} width={100} onClick={this.jumpBlankHref.bind(this, text)} />
          }
          return <div>无</div>
        },
      },
      {
        title: '动漫名称',
        dataIndex: 'cartoonName',
        key: 'cartoonName',
        width: '100',
      },
      {
        title: '载体',
        dataIndex: 'itemType',
        key: 'itemType',
        width: '100',
      },
      {
        title: '原作者',
        dataIndex: 'author',
        key: 'author',
        width: '400',
      },
      {
        title: '制作公司',
        dataIndex: 'company',
        key: 'company',
        width: '100',
      },
      {
        title: '评级',
        dataIndex: 'score',
        key: 'score',
        width: '100',
      },
      {
        title: '证据-图片',
        dataIndex: 'evidenceImg',
        key: 'evidenceImg',
        width: '100',
        render: (text) => {
          if (text) {
            return <img src={text} width={100} onClick={this.jumpBlankHref.bind(this, text)} />
          }
          return <div>无</div>
        },
      },
      {
        title: '证据-视频',
        dataIndex: 'evidenceVideo',
        key: 'evidenceVideo',
        width: '100',
        render: (text) => {
          if (text) {
            return <a onClick={this.jumpBlankHref.bind(this, text)} >预览>></a>
          }
          return <div>无</div>
        },
      },
      {
        title: '更新时间',
        dataIndex: 'updatedate',
        key: 'updatedate',
        width: '100',
        render: text => moment(text).format('YYYY-MM-DD HH:mm:ss'),
      },
      {
        title: '备注',
        dataIndex: 'remark',
        key: 'remark',
        width: '200',
      },
      {
        title: '操作人',
        dataIndex: 'operator',
        key: 'operator',
        width: '120',
      },
      {
        title: '操作',
        key: 'operate',
        width: '200',
        // flexed: 'right',
        render: (text, record, index) => (
          <span>
            {
              this.state.hasUpdatePermission ?
                <span onClick={() => this.handleEdit(record)}>
                  <a>编辑</a>
                </span>
                : null
            }
            {
              this.state.hasDeletePermission ? <span className="ant-divider" /> : null
            }
            {
              this.state.hasDeletePermission ?
                <span onClick={() => this.handleDelete(record.id)}>
                  <a>删除</a>
                </span>
                : null
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
                 赛璐璐管理
              </h3>
              <div className="page-header">
                <div className="layout-between">
                  <Form className="flexrow" onSubmit={e => this.handleSearch(e)}>
                    <FormItem>
                      {getFieldDecorator('key')(<Input className="input-base-width" size="default" placeholder="输入证书编号、角色名或动漫名" aria-label="证书编号、角色名或动漫名" />)}
                    </FormItem>
                    <Button type="primary" htmlType="submit" >搜索</Button>
                    {this.state.hasAddPermission ? <Button type="primary" style={{ marginLeft: '30px' }} onClick={() => this.handleAdd()}> 新增</Button> : null}
                    {this.state.hasExportPermission ? <Button type="primary" style={{ marginLeft: '30px' }} onClick={() => this.exportAllData()}>全部导出</Button> : null}
                  </Form>
                </div>
              </div>
              <div className="page-content has-pagination table-scrollfix">
                <TableList
                  rowKey="id"
                  columns={this.renderColumn()}
                  dataSource={this.state.cartoonList}
                  currentPage={this.state.searchKey.pageNo}
                  pageSize={this.state.searchKey.pageSize}
                  scroll={{ y: true }}
                  onChange={this.pageChange}
                  showQuickJumper
                  onShowSizeChange={this.pageSizeChange}
                  totalCount={this.state.totalCount}
                  rowSelection={rowSelection}
                />
              </div>
              {
                this.state.hasDeletePermission ?
                  <div className="page-footer" >
                    <div>
                      <Button type="primary" style={{ marginLeft: '30px' }} onClick={() => this.handleDelete()}> 删除</Button>
                    </div>
                  </div>
                  : null
              }
              {this.state.spinloading ? <div className="loading" style={{ height: 800 }}>
                <Spin tip="加载中..." />
              </div> : null}
            </Content>
          </Layout>
        </Layout>

        {/* 允许新增的判断 */}
        {
          this.state.cartoonAddVisible ?
            <Addcartoon
              visible={this.state.cartoonAddVisible}
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
