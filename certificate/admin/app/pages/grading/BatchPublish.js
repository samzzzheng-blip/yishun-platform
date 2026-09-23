import React from 'react'
import {Button,Input,Modal,message} from 'antd'
import {postData} from '../../service/http'

export default class BatchPublish extends React.Component {
  state={start:'',preview:null,busy:false,error:''}
  componentWillReceiveProps(next){if(next.job.id!==this.props.job.id)this.setState({start:'',preview:null,error:''});else if(next.job.version!==this.props.job.version)this.setState({preview:null})}
  preview=async()=>{
    if(this.props.disabled)return
    this.setState({busy:true,error:''});this.props.onBusy(true)
    try{const preview=await postData('gradingBatchPreview',{id:this.props.job.id,startNumber:this.state.start});this.setState({preview})}
    catch(e){this.setState({error:e.message||'预览失败，请重试'})}finally{this.setState({busy:false});this.props.onBusy(false)}
  }
  publish=async()=>{
    if(this.props.disabled)return
    this.setState({busy:true,error:''});this.props.onBusy(true)
    try{const result=await postData('gradingBatchPublish',{id:this.props.job.id,startNumber:this.state.preview.startNumber,token:this.state.preview.token});this.setState({preview:null});message.success(result.temporary?'已完成 '+result.rows.length+' 件临时商品':'已发布 '+result.rows.length+' 张卡片，整批已完成');await this.props.onPublished(result)}
    catch(e){this.setState({preview:null,error:e.message||'发布失败，请重新预览后重试'})}finally{this.setState({busy:false});this.props.onBusy(false)}
  }
  render(){const {start,preview,busy,error}=this.state;return <section className="grading-batch-publish" aria-label="批次发布">
    <h3>批次发布</h3><p>批次：{this.props.job.batch||'未分批次'}。只发布当前账号在本批次中的未发布卡片，所有卡片需先提交到待发布。</p>
    <label className="grading-field">批次起始编号（选填）<Input inputMode="numeric" maxLength={18} placeholder="填写则正式发布，留空则完成为临时商品" disabled={busy} value={start} onChange={e=>this.setState({start:e.target.value,preview:null,error:''})}/></label>
    {!start.trim()&&<p>未填写编号：确认后进入“临时”区域，仅当前账号可见，不生成正式评级记录和二维码。</p>}
    <p>正式编号＝起始编号＋批次序号－1。例如 1 → 2022007，2 → 2022008。序号有空缺时保留间隔；历史无序号档案会追加到批次末尾。</p>
    {error&&<p className="grading-error" role="alert">{error}</p>}
    {this.props.disabled&&!busy&&<p>请先保存信息汇总，再预览和发布。</p>}
    <Button type="primary" loading={busy} disabled={this.props.disabled||!this.props.job.batch} onClick={this.preview}>{start.trim()?'预览整批编号':'预览临时完成'}</Button>
    {!this.props.job.batch&&<p>请先退回整理并填写收卡批次。</p>}
    <Modal title={preview&&preview.temporary?'确认完成为临时商品':'确认整批发布'} visible={!!preview} confirmLoading={busy} closable={!busy} maskClosable={!busy} cancelButtonProps={{disabled:busy}} okText={preview&&preview.temporary?'确认临时完成':'确认发布并标记已完成'} cancelText="返回修改" onCancel={()=>{if(!busy)this.setState({preview:null})}} onOk={this.publish}>
      {preview&&<React.Fragment><p>批次：{preview.batch}，共 {preview.rows.length} 张。{preview.temporary?'确认后进入临时区域，不生成正式编号，可按完成时间永久清除。':'确认后统一生成正式评级记录并标记已完成，不再经过打印或拍成品步骤。'}</p>
        <div className="grading-batch-preview"><table><thead><tr><th>批次序号</th><th>名称</th><th>信息汇总</th><th>正式编号</th></tr></thead><tbody>{preview.rows.map(r=><tr key={r.id}><td>{r.batchNumber}</td><td>{r.name}</td><td style={{whiteSpace:'pre-wrap'}}>{r.labelText}</td><td>{preview.temporary?'不生成':r.certNumber}</td></tr>)}</tbody></table></div>
      </React.Fragment>}
    </Modal>
  </section>}
}
