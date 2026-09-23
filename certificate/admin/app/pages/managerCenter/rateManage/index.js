
import React, { Component } from 'react'
import { Button, Modal, Form, Input, message, Layout, Spin } from 'antd'
import TableList from '@tableList'
import Addrate from './modal/addRate'
import ImportRate from './modal/importRate'

const { confirm } = Modal
const FormItem = Form.Item
const { Content } = Layout
import { getData, getFullUrl } from '@http';
import moment from 'moment';
import permission from '../../../utils/permission';
import { Link } from 'react-router';

@Form.create({})
// 声明组件  并对外输出
export default class app extends Component {
  // 初始化页面常量 绑定事件方法
  constructor(props) {
    super(props);
    this.onSelectChange = this.onSelectChange.bind(this);
    this.state = {
      addVisible: false,
      importVisible: false,
      spinloading: false,
      moduletitle: '',
      moduletype: '',
      searchKey: {
        keyword: '',
        startNumber: '',
        endNumber: '',
        pageSize: 10,
        pageNo: 1,
      },
      rateList: [],
      totalCount: 0,
      selectedRowKeys: [],
      rowData: null,
      hasUpdatePermission: permission.hasUpdatePermission('rateManage'),
      hasDeletePermission: permission.hasDeletePermission('rateManage'),
      hasAddPermission: permission.hasAddPermission('rateManage'),
      hasExportPermission: permission.hasExportPermission('rateManage'),
    }
  }

  // 组件即将加载
  componentWillMount() {

  }

  // 组件已经加载到dom中
  componentDidMount() {
    const certNumber=this.props.location.query.certNumber||''
    this.props.form.setFieldsValue({ key: certNumber })
    this.setState({searchKey:{...this.state.searchKey,keyword:certNumber}},()=>this.getData());
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
    if (this.state.searchKey.startNumber) {
      params.startNumber = this.state.searchKey.startNumber;
    }
    if (this.state.searchKey.endNumber) {
      params.endNumber = this.state.searchKey.endNumber;
    }
    getData('queryRateList', params).then((data) => {
      if (data && data.list) {
        this.setState({
          rateList: data.list,
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
    const range = this.getNumberRange(false)
    if (!range) return
    this.setState({
      spinloading: true,
      searchKey: {
        ...this.state.searchKey,
        keyword: (keyword || '').trim(),
        startNumber: range.startNumber,
        endNumber: range.endNumber,
        pageNo: 1,
      },
    }, () => { this.getData(() => { this.setState({ spinloading: false }) }) })
  }

  getNumberRange = (required) => {
    const startNumber = (this.props.form.getFieldValue('startNumber') || '').trim()
    const endNumber = (this.props.form.getFieldValue('endNumber') || '').trim()
    if (required && !startNumber && !endNumber) {
      message.warning('请至少填写一个编号区间')
      return null
    }
    if ((startNumber && !/^\d+$/.test(startNumber)) || (endNumber && !/^\d+$/.test(endNumber))) {
      message.warning('编号区间只能填写数字')
      return null
    }
    if (startNumber && endNumber && Number(startNumber) > Number(endNumber)) {
      message.warning('起始编号不能大于结束编号')
      return null
    }
    return { startNumber, endNumber }
  }

  handleDeleteRange = () => {
    const range = this.getNumberRange(true)
    if (!range) return
    getData('queryRateCertNumbers', range).then((certNumbers) => {
      if (!certNumbers || certNumbers.length === 0) {
        message.warning('当前编号区间内没有记录')
        return
      }
      const that = this
      confirm({
        title: '确认一键删除',
        content: `将永久删除当前编号区间内的 ${certNumbers.length} 条记录及对应资源，确定继续吗？`,
        okText: '确认删除',
        cancelText: '取消',
        okType: 'danger',
        onOk() {
          that.setState({ spinloading: true })
          return getData('deleteRateRange', range, 10 * 60000).then((result) => {
            message.success(`已删除 ${result.deleted} 条记录`)
            that.setState({ spinloading: false }, () => that.getData())
          }).catch((err) => {
            that.setState({ spinloading: false })
            message.warning(err.message)
          })
        },
      })
    }).catch((err) => message.warning(err.message))
  }

  handleDownloadRangeQrcodes = () => {
    const range = this.getNumberRange(true)
    if (!range) return
    this.setState({ spinloading: true })
    getData('queryRateCertNumbers', range, 10 * 60000).then((certNumbers) => {
      this.setState({ spinloading: false })
      if (!certNumbers || certNumbers.length === 0) {
        message.warning('当前编号区间内没有二维码')
        return
      }
      if (window.showDirectoryPicker) {
        confirm({
          title: '选择二维码保存位置',
          content: `共 ${certNumbers.length} 个二维码。点击“选择位置”后，将在所选位置创建“二维码-下载时间”文件夹。`,
          okText: '选择位置',
          cancelText: '取消',
          onOk: () => this.saveQrcodesToDirectory(certNumbers),
        })
      } else {
        this.downloadQrcodeZip(range)
      }
    }).catch((err) => {
      this.setState({ spinloading: false })
      message.warning(err.message)
    })
  }

  saveQrcodesToDirectory = async (certNumbers) => {
    try {
      const directory = await window.showDirectoryPicker()
      const folderName = `二维码-${moment().format('YYYYMMDD-HHmmss')}`
      const targetDirectory = await directory.getDirectoryHandle(folderName, { create: true })
      this.setState({ spinloading: true })
      let cursor = 0
      const worker = async () => {
        while (cursor < certNumbers.length) {
          const certNumber = certNumbers[cursor]
          cursor += 1
          const response = await fetch(`${getFullUrl('downloadQrcode')}?certNo=${encodeURIComponent(certNumber)}`, {
            method: 'get',
            credentials: 'include',
            headers: new Headers({ Authorization: localStorage.getItem('token') }),
          })
          if (!response.ok) throw new Error(`二维码 ${certNumber} 下载失败`)
          const fileHandle = await targetDirectory.getFileHandle(`qrcode_${certNumber}.png`, { create: true })
          const writable = await fileHandle.createWritable()
          await writable.write(await response.blob())
          await writable.close()
        }
      }
      await Promise.all([worker(), worker(), worker(), worker()])
      message.success(`已保存 ${certNumbers.length} 个二维码到“${folderName}”文件夹`)
    } catch (err) {
      if (!err || err.name !== 'AbortError') {
        message.warning(err.message || '二维码下载失败')
      }
    } finally {
      this.setState({ spinloading: false })
    }
  }

  downloadQrcodeZip = (range) => {
    const query = `startNumber=${encodeURIComponent(range.startNumber)}&endNumber=${encodeURIComponent(range.endNumber)}`
    this.setState({ spinloading: true })
    fetch(`${getFullUrl('downloadRateQrcodes')}?${query}`, {
      method: 'get',
      credentials: 'include',
      headers: new Headers({ Authorization: localStorage.getItem('token') }),
    }).then((response) => {
      if (!response.ok) throw new Error('二维码打包失败')
      return response.blob()
    }).then((blob) => {
      const blobUrl = window.URL.createObjectURL(blob)
      const aElement = document.createElement('a')
      aElement.href = blobUrl
      aElement.download = `二维码-${moment().format('YYYYMMDD-HHmmss')}.zip`
      document.body.appendChild(aElement)
      aElement.click()
      document.body.removeChild(aElement)
      window.URL.revokeObjectURL(blobUrl)
      message.success('二维码压缩包已生成')
    }).catch((err) => message.warning(err.message)).then(() => this.setState({ spinloading: false }))
  }

  // 新增
  handleAdd() {
    this.setState({
      addVisible: true,
      moduletype: 'add',
      moduletitle: '新增',
      rowData: null,
    })
  }

  handleImport = () => {
    this.setState({ importVisible: true })
  }

  handleImportCancel = () => {
    this.setState({ importVisible: false })
  }

  handleImportOk = () => {
    this.setState({
      importVisible: false,
      searchKey: {
        ...this.state.searchKey,
        pageNo: 1,
      },
    }, () => this.getData())
  }

  // 编辑
  handleEdit(record) {
    this.setState({
      addVisible: true,
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
        getData('deleteRate', params).then((data) => {
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
      addVisible: false,
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
    this.setState({ addVisible: false })
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
    getData('exportRate', { startTime: 0, endTime }, 60 * 60000).then((data) => {
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
        title: '编号',
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
        title: '名称',
        dataIndex: 'rateName',
        key: 'rateName',
        width: '200',
        // fixed: 'left',
      },
      {
        title: '表面',
        dataIndex: 'surface',
        key: 'surface',
        width: '100',
      },
      {
        title: '居中',
        dataIndex: 'center',
        key: 'center',
        width: '100',
      },
      {
        title: '边缘',
        dataIndex: 'edge',
        key: 'edge',
        width: '100',
      },
      {
        title: '角落',
        dataIndex: 'corner',
        key: 'corner',
        width: '100',
      },
      {
        title: '总评分',
        dataIndex: 'score',
        key: 'score',
        width: '100',
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
                 评级管理
                 <span style={{float:'right'}}><Link to="/rateWorkflow">评级流程</Link>{(this.state.hasAddPermission||this.state.hasUpdatePermission)&&<Link to="/rateIntake" style={{marginLeft:20}}>收卡建档</Link>}</span>
              </h3>
              <div className="page-header">
                <div className="layout-between">
                  <Form className="flexrow" onSubmit={e => this.handleSearch(e)}>
                    <FormItem>
                      {getFieldDecorator('key')(<Input className="input-base-width" size="default" placeholder="输入证书编号或名称" aria-label="证书编号或名称" />)}
                    </FormItem>
                    <FormItem style={{ marginLeft: '12px' }}>
                      {getFieldDecorator('startNumber')(<Input style={{ width: '145px' }} size="default" placeholder="起始编号" />)}
                    </FormItem>
                    <span style={{ lineHeight: '32px', margin: '0 8px' }}>至</span>
                    <FormItem>
                      {getFieldDecorator('endNumber')(<Input style={{ width: '145px' }} size="default" placeholder="结束编号" />)}
                    </FormItem>
                    <Button type="primary" htmlType="submit" >搜索</Button>
                    {this.state.hasAddPermission ? <Button type="primary" style={{ marginLeft: '30px' }} onClick={() => this.handleAdd()}> 新增</Button> : null}
                    {this.state.hasAddPermission ? <Button style={{ marginLeft: '30px' }} onClick={this.handleImport}>批量导入</Button> : null}
                    {this.state.hasExportPermission ? <Button type="primary" style={{ marginLeft: '30px' }} onClick={() => this.exportAllData()}>全部导出</Button> : null}
                  </Form>
                  <div style={{ display: 'flex', flexShrink: 0, marginLeft: '20px' }}>
                    {this.state.hasExportPermission ? <Button type="primary" onClick={this.handleDownloadRangeQrcodes}>一键下载二维码</Button> : null}
                    {this.state.hasDeletePermission ? <Button type="danger" style={{ marginLeft: '12px' }} onClick={this.handleDeleteRange}>一键删除</Button> : null}
                  </div>
                </div>
              </div>
              <div className="page-content has-pagination table-scrollfix">
                <TableList
                  rowKey="id"
                  columns={this.renderColumn()}
                  dataSource={this.state.rateList}
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
          this.state.addVisible ?
            <Addrate
              visible={this.state.addVisible}
              title={this.state.moduletitle}
              handleOk={() => this.handleOk()}
              values={this.state.rowData}
              type={this.state.moduletype}
              onCancel={this.handleCancel}
            />
            : null
        }
        {
          this.state.importVisible ?
            <ImportRate
              visible={this.state.importVisible}
              onCancel={this.handleImportCancel}
              onSuccess={this.handleImportOk}
            />
            : null
        }
      </div>
    )
  }
}
