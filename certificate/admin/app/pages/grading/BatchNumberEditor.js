import React from 'react'
import {Button,Input,Modal,message} from 'antd'
import {postData} from '../../service/http'

export default class BatchNumberEditor extends React.Component {
  state={open:false,value:'',busy:false,error:''}
  open=()=>{
    if(this.props.dirty){message.warning('请先保存当前修改，再修改批次内编号');return}
    this.setState({open:true,value:this.props.job.batchNumber==null?'':String(this.props.job.batchNumber),error:''});this.props.onBusy(true)
  }
  close=()=>{if(!this.state.busy){this.setState({open:false});this.props.onBusy(false)}}
  save=async()=>{
    if(!/^[1-9][0-9]{0,14}$/.test(this.state.value)){this.setState({error:'请输入最多15位正整数，不含前导零。'});return}
    this.setState({busy:true,error:''})
    try{
      const j=this.props.job,job=await postData('gradingBatchNumberSave',{id:j.id,version:j.version,batchNumber:this.state.value})
      await this.props.onSaved(job);this.setState({open:false});message.success('批次内编号已保存，发布前会再次查重');this.props.onBusy(false)
    }catch(e){this.setState({error:e.message||'编号保存失败，请刷新后重试'})}finally{this.setState({busy:false})}
  }
  render(){const s=this.state,j=this.props.job;return <div className="grading-batch-number-edit">
    <Button disabled={this.props.disabled} onClick={this.open}>修改批次编号</Button>
    <Modal title="修改批次内编号" visible={s.open} onCancel={this.close} onOk={this.save} okText="保存编号" cancelText="取消" confirmLoading={s.busy} closable={!s.busy} maskClosable={false} cancelButtonProps={{disabled:s.busy}}>
      <p>批次：{j.batch||'未分批次'} · 当前编号：{j.batchNumber==null?'未编号':j.batchNumber}</p>
      <label className="grading-field">批次内编号<Input aria-label="批次内编号" inputMode="numeric" maxLength={15} disabled={s.busy} value={s.value} onChange={e=>this.setState({value:e.target.value,error:''})}/></label>
      <p>仅修改当前商品的批次序号，不改变信息、评分或状态。保存和发布均查重；已占用编号（含保留档案）不可重复使用。</p>
      <p>正式编号＝批次起始编号＋此序号－1。修改为更大序号后，后续自动编号从其后继续。</p>
      {s.error&&<p role="alert" className="grading-error">{s.error}</p>}
    </Modal>
  </div>}
}
