import React from 'react'
import {Button,Modal,Select,message} from 'antd'
import {postData} from '../../service/http'

export default class WorkflowActions extends React.Component {
  state={action:'',targetBatch:'',preview:null,busy:false,error:'',request:null}
  open=action=>{
    if(this.props.dirty){message.warning('请先保存当前档案，再进行删除或修改批次');return}
    this.setState({action,targetBatch:'',preview:null,error:'',request:{...this.props.scope,action}})
  }
  run=async confirm=>{
    this.setState({busy:true,error:''});this.props.onBusy(true)
    try{
      const request={...this.state.request,targetBatch:this.state.targetBatch,token:this.state.preview&&this.state.preview.token}
      const data=await postData(confirm?'gradingManageApply':'gradingManagePreview',request)
      if(confirm){message.success((this.state.action==='DELETE'?'已从评级流程删除 ':'已修改批次 ')+data.count+' 张');this.setState({action:'',preview:null});await this.props.onChanged()}
      else this.setState({preview:data})
    }catch(e){this.setState({error:e.message||'操作失败，请重新预览后重试',preview:null})}
    finally{this.setState({busy:false});this.props.onBusy(false)}
  }
  newBatch=async()=>{
    this.setState({busy:true,error:''})
    try{const batch=await this.props.newBatch();this.setState({targetBatch:batch,preview:null})}
    catch(e){this.setState({error:e.message||'新建批次失败'})}
    finally{this.setState({busy:false})}
  }
  render(){
    const s=this.state,p=this.props,remove=s.action==='DELETE'
    return <div className="grading-management-actions">
      {p.canEdit&&<Button disabled={p.disabled} onClick={()=>this.open('MOVE')}>{p.single?'修改批次':'修改筛选结果批次'}</Button>}
      {p.canDelete&&<Button type="danger" disabled={p.disabled} onClick={()=>this.open('DELETE')}>{p.single?'删除此商品':'删除筛选结果'}</Button>}
      <Modal visible={!!s.action} title={remove?'删除评级流程档案':'修改收卡批次'} footer={null} maskClosable={false} closable={!s.busy} onCancel={()=>this.setState({action:''})}>
        <p>{p.single?'仅处理当前商品。':'处理当前筛选条件下的全部结果（包含其他分页），不影响筛选范围外的档案。'}</p>
        {!p.single&&s.request&&<p>批次：{s.request.batch||'全部'} · 状态：{{COLLECTING:'待整理',PUBLISHING:'待发布',DONE:'已完成',UNFINISHED:'未完成'}[s.request.stage]||'全部'} · 关键词：{s.request.keyword||'无'}</p>}
        {remove?<p>仅从评级流程移除；正式评级记录、证书查询和照片均保留。页面无撤销入口，需管理员从保留数据恢复。</p>:<div>
          <label className="grading-field">目标批次<Select aria-label="目标批次" showSearch disabled={s.busy} value={s.targetBatch||undefined} placeholder="选择目标批次" onChange={targetBatch=>this.setState({targetBatch,preview:null})} style={{width:'100%'}}>{p.batches.map(batch=><Select.Option key={batch} value={batch}>{batch}</Select.Option>)}</Select></label>
          <Button disabled={s.busy} onClick={this.newBatch}>新增目标批次</Button>
          <p>移入后按目标批次继续编号。未完成商品退回待整理并使用目标批次模板，信息值保留；已完成商品的正式证书编号不变。已在目标批次的商品保持原样。</p>
        </div>}
        {s.error&&<p className="grading-error" role="alert">{s.error}</p>}
        {s.preview&&<p role="status">本次将{remove?'删除':'处理'} {s.preview.count} 张商品{!remove?'，目标批次：'+s.targetBatch:''}。请核对后确认。</p>}
        <div className="grading-management-actions"><Button disabled={s.busy} onClick={()=>this.setState({action:''})}>取消</Button>{s.preview?<Button type={remove?'danger':'primary'} loading={s.busy} onClick={()=>this.run(true)}>{remove?'确认删除':'确认修改'} {s.preview.count} 张</Button>:<Button type="primary" disabled={!remove&&!s.targetBatch} loading={s.busy} onClick={()=>this.run(false)}>预览影响范围</Button>}</div>
      </Modal>
    </div>
  }
}
