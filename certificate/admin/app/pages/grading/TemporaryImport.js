// Operate: extend the existing Ant Design import interaction, scoped explicitly to
// account-private temporary cards. Preserve labels, tolerate blanks, prevent resubmission.
import React from 'react'
import {Alert,Button,Icon,Modal,Upload,message} from 'antd'
import {uploadFile} from '../../service/http'

export default class TemporaryImport extends React.Component {
  state={file:null,busy:false,result:null,error:''}
  componentDidMount(){this.active=true}
  componentWillUnmount(){this.active=false}
  choose=file=>{
    if(this.working)return false
    if(!/\.(xls|xlsx|xml)$/i.test(file.name)){message.error('请选择 .xls、.xlsx 或 .xml 文件');return false}
    if(!file.size||file.size>20*1024*1024){message.error('文件不能为空，且不能超过20MB');return false}
    this.setState({file,result:null,error:''});return false
  }
  close=()=>{if(!this.working)this.props.onClose(!!(this.state.result&&this.state.result.imported))}
  submit=async()=>{
    if(this.working||!this.state.file||this.state.result)return
    this.working=true;this.setState({busy:true,error:''})
    try{
      const response=await uploadFile('gradingTemporaryImport',{},this.state.file,5*60*1000)
      if(this.active)this.setState({result:response.data})
    }catch(e){if(this.active)this.setState({error:(e.message||'请求未完成')+'。若网络中断，请先关闭弹窗并刷新临时列表核对；重新提交同一未修改文件不会重复导入。'})}
    finally{this.working=false;if(this.active)this.setState({busy:false})}
  }
  render(){const s=this.state,r=s.result;return <Modal title="导入临时商品" visible width={620} onCancel={this.close} closable={!s.busy} maskClosable={false} keyboard={!s.busy} footer={[
    <Button key="close" disabled={s.busy} onClick={this.close}>{r?'完成':'取消'}</Button>,
    <Button key="import" type="primary" loading={s.busy} disabled={!s.file||!!r} onClick={this.submit}>导入临时区</Button>
  ]}>
    <Alert type="info" showIcon message="无需编号，仅导入当前账号的临时区" description="支持 Excel（.xls/.xlsx）和 XML，单次最多5000条、20MB。编号及二维码文件名不生成正式证书编号；不会进入正式评级。"/>
    <p className="grading-import-help">按表头读取标签、表面、居中、边缘、角落、总分、照片/图片、备注。空字段保留为空；多行标签完整保存为信息汇总，名称取第二个非空行。每次导入自动建一个批次，批次序号从1开始，完成时间为导入时间。</p>
    <Upload.Dragger accept=".xls,.xlsx,.xml" multiple={false} fileList={s.file?[s.file]:[]} beforeUpload={this.choose} disabled={s.busy} onRemove={()=>{if(this.working)return false;this.setState({file:null,result:null,error:''})}}>
      <p className="ant-upload-drag-icon"><Icon type="inbox"/></p>
      <p className="ant-upload-text">点击选择或拖入临时商品文件</p>
      <p className="ant-upload-hint">名称、编号、评分及照片均可留空；完全空白行不导入</p>
    </Upload.Dragger>
    <p className="grading-import-help">评分如填写，须为0至10、间隔0.5的数字。图片可嵌入文件或填写图片地址；本地图片文件名不代表图片已上传。文件校验失败时，本次新记录全部不保存。</p>
    {s.error&&<Alert type="error" showIcon message="导入未完成" description={s.error}/>}
    {r&&<div role="status" aria-live="polite"><Alert showIcon type={r.skipped?'warning':'success'} message={'读取 '+r.total+' 条，新导入 '+r.imported+' 条，已导入跳过 '+r.skipped+' 条'} description={<div className="grading-import-errors">{(r.errors||[]).map((e,i)=><div key={i}>{e}</div>)}{r.imported>0&&<p>点击“完成”后清空日期筛选并显示最新临时商品。</p>}</div>}/></div>}
  </Modal>}
}
