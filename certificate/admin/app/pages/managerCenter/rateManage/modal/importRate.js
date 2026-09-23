import React, { Component } from 'react'
import { Alert, Button, Icon, Modal, Upload, message } from 'antd'
import { uploadFile } from '@http'

const { Dragger } = Upload

const XML_TEMPLATE = `<?xml version="1.0" encoding="UTF-8"?>
<评级记录>
  <记录>
    <编号>请填写唯一编号</编号>
    <标签>第一行可填写完整标题
第二行填写实际名称</标签>
    <表面>9.5</表面>
    <居中>10</居中>
    <边缘>9.5</边缘>
    <角落>9.5</角落>
    <总分>9.5</总分>
    <照片>请填写图片网址、系统图片路径或data:image图片数据</照片>
    <ysk>允许保留，但导入时忽略</ysk>
    <备注>可选备注</备注>
  </记录>
</评级记录>`

export default class ImportRate extends Component {
  state = {
    fileList: [],
    importing: false,
    result: null,
  }

  beforeUpload = (file) => {
    const fileName = file.name.toLowerCase()
    const supported = ['.xls', '.xlsx', '.xml'].some(ext => fileName.endsWith(ext))
    if (!supported) {
      message.error('仅支持 .xls、.xlsx 或 .xml 文件')
      return false
    }
    if (file.size > 20 * 1024 * 1024) {
      message.error('导入文件不能超过 20MB')
      return false
    }
    this.setState({ fileList: [file], result: null })
    return false
  }

  removeFile = () => {
    this.setState({ fileList: [], result: null })
  }

  handleImport = () => {
    const { fileList } = this.state
    if (fileList.length === 0) {
      message.warning('请先选择导入文件')
      return
    }
    this.setState({ importing: true, result: null })
    uploadFile('importRate', {}, fileList[0], 5 * 60 * 1000).then((response) => {
      const result = response.data || {}
      this.setState({ importing: false, result })
      if (result.imported > 0) {
        message.success(`成功导入 ${result.imported} 条`)
      } else {
        message.warning('没有可导入的数据，请检查文件内容')
      }
    }).catch((err) => {
      this.setState({ importing: false })
      message.error(err && err.message ? err.message : '导入失败，请检查文件格式')
    })
  }

  handleDone = () => {
    if (this.state.result && this.state.result.imported > 0) {
      this.props.onSuccess()
      return
    }
    this.props.onCancel()
  }

  downloadXmlTemplate = () => {
    const blob = new Blob([XML_TEMPLATE], { type: 'application/xml;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '评级导入标准模板.xml'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }

  renderResult() {
    const { result } = this.state
    if (!result) return null
    const errors = result.errors || []
    return (
      <div style={{ marginTop: 16 }} role="status" aria-live="polite">
        <Alert
          type={result.imported > 0 ? 'success' : 'warning'}
          showIcon
          message={`共读取 ${result.total || 0} 条，成功 ${result.imported || 0} 条，跳过 ${result.skipped || 0} 条`}
          description={errors.length > 0 ? (
            <div style={{ maxHeight: 140, overflowY: 'auto', marginTop: 8 }}>
              {errors.map((error, index) => <div key={index}>{error}</div>)}
            </div>
          ) : null}
        />
      </div>
    )
  }

  render() {
    const { visible } = this.props
    const { fileList, importing, result } = this.state
    return (
      <Modal
        visible={visible}
        title="批量导入评级"
        width={620}
        onCancel={this.handleDone}
        footer={[
          <Button key="cancel" onClick={this.handleDone}>{result ? '完成' : '取消'}</Button>,
          <Button key="import" type="primary" loading={importing} disabled={fileList.length === 0} onClick={this.handleImport}>
            开始导入
          </Button>,
        ]}
      >
        <Alert
          type="info"
          showIcon
          message="支持 Excel（.xls/.xlsx）和 XML，单次最多 5000 条"
          description="按表头名称识别，列顺序不限。支持表头：编号、标签、表面、居中、边缘、角落、总分、照片、ysk、备注。空单元格只忽略该字段，其他字段仍会导入；ysk 会被忽略；标签多行时取第二个非空行。"
          style={{ marginBottom: 16 }}
        />
        <Button icon="download" onClick={this.downloadXmlTemplate} style={{ marginBottom: 16 }}>
          下载标准 XML 模板
        </Button>
        <Dragger
          accept=".xls,.xlsx,.xml"
          multiple={false}
          fileList={fileList}
          beforeUpload={this.beforeUpload}
          onRemove={this.removeFile}
          disabled={importing}
        >
          <p className="ant-upload-drag-icon"><Icon type="inbox" /></p>
          <p className="ant-upload-text">点击或拖拽文件到这里</p>
          <p className="ant-upload-hint">文件大小不超过 20MB；字段为空时保留为空，重复编号会跳过并显示原因</p>
        </Dragger>
        {this.renderResult()}
      </Modal>
    )
  }
}
