// Operate: extend the existing grading shell with date-filtered, private, read-only
// completed cards. Clearing is explicit, previewed, and irreversible; no automatic purge.
import React from 'react'
import {Button,Input,Modal,Table,message} from 'antd'
import {Link} from 'react-router'
import {getData,postData} from '../../service/http'
import {canDeleteRating,canEditRating} from '../../utils/ratingNavigation'
import TemporaryImport from './TemporaryImport'
import moment from 'moment'
import PrivatePhoto from './PrivatePhoto'
import '../../styles/grading-workbench.less'

export default class Temporary extends React.Component {
  state={from:'',to:'',applied:{from:'',to:''},rows:[],total:0,page:1,loading:false,busy:false,error:'',preview:null,confirm:''}
  componentDidMount(){this.active=true;document.documentElement.classList.add('grading-document');this.viewport=document.querySelector('meta[name="viewport"]');if(this.viewport){this.oldViewport=this.viewport.getAttribute('content');this.viewport.setAttribute('content','width=device-width, initial-scale=1')}this.load()}
  componentWillUnmount(){this.active=false;document.documentElement.classList.remove('grading-document');if(this.viewport){if(this.oldViewport===null)this.viewport.removeAttribute('content');else this.viewport.setAttribute('content',this.oldViewport)}}
  load=async(page=1,range=this.state.applied)=>{
    const request=this.request=(this.request||0)+1;this.setState({loading:true,error:''})
    try{const data=await getData('gradingTemporary',{...range,page});if(this.active&&request===this.request)this.setState({rows:data.content,total:data.totalElements,page,applied:range})}
    catch(e){if(this.active&&request===this.request)this.setState({error:e.message||'读取失败，请重试'})}
    finally{if(this.active&&request===this.request)this.setState({loading:false})}
  }
  preview=async()=>{
    if(this.working)return;this.working=true;this.setState({busy:true,error:''})
    try{const preview=await postData('gradingTemporaryPreview',this.state.applied);if(this.active)this.setState({preview,confirm:''})}
    catch(e){if(this.active)this.setState({error:e.message||'清除预览失败，请重新筛选'})}
    finally{this.working=false;if(this.active)this.setState({busy:false})}
  }
  purge=async()=>{
    if(this.working||this.state.confirm!=='清除'||!this.state.preview)return;this.working=true;this.setState({busy:true,error:''})
    try{const result=await postData('gradingTemporaryPurge',this.state.preview);if(this.active){this.setState({preview:null,confirm:''});message.success('已永久清除 '+result.count+' 件临时商品');await this.load(1)}}
    catch(e){if(this.active)this.setState({preview:null,confirm:'',error:e.message||'清除结果未确认，请刷新列表后核对，不要重复提交'})}
    finally{this.working=false;if(this.active)this.setState({busy:false})}
  }
  render(){const s=this.state,changed=s.from!==s.applied.from||s.to!==s.applied.to;
    const columns=[{title:'照片',dataIndex:'frontPhoto',width:90,render:src=><div className="grading-temporary-photo"><PrivatePhoto src={src} alt="卡片正面" zoom/></div>},{title:'名称',dataIndex:'cardName',width:160},{title:'批次',dataIndex:'batch',width:180},{title:'批次序号',dataIndex:'batchNumber',width:100},{title:'信息汇总',dataIndex:'labelText',width:240,render:text=><span className="grading-temporary-summary">{text}</span>},{title:'表面 / 居中 / 边缘 / 角落 / 总分',key:'scores',width:240,render:(_,j)=>[j.surface,j.center,j.edge,j.corner,j.score].join(' / ')},{title:'完成时间（北京时间）',dataIndex:'updatedAt',width:190,render:value=>moment(value).utcOffset(480).format('YYYY-MM-DD HH:mm:ss')}];
    return <main className="grading-workbench"><div className="grading-top"><div><h1>临时</h1><p>未填写正式编号的已完成商品，仅当前账号可见，不进入正式评级。</p></div><nav><Link to="/rateManage">评级管理</Link><Link to="/rateWorkflow">评级流程</Link></nav></div>
      <section className="grading-detail" aria-label="临时商品管理"><div className="grading-template-row"><label>完成开始日期<Input placeholder="YYYY-MM-DD，例如 2026-09-11" maxLength={10} value={s.from} disabled={s.busy} onChange={e=>this.setState({from:e.target.value})}/></label><label>完成结束日期<Input placeholder="YYYY-MM-DD，例如 2026-09-11" maxLength={10} value={s.to} disabled={s.busy} onChange={e=>this.setState({to:e.target.value})}/></label><Button loading={s.loading} disabled={s.busy} onClick={()=>this.load(1,{from:s.from,to:s.to})}>筛选</Button><Button disabled={s.busy||s.loading} onClick={()=>this.setState({from:'',to:''},()=>this.load(1,{from:'',to:''}))}>重置</Button></div>
      <p>按北京时间筛选，包含开始和结束日期当天。不自动清除；永久清除前必须选定时间范围。</p>
      <div className="grading-management-actions"><span>共 {s.total} 件</span>{canEditRating(sessionStorage)&&<Button type="primary" icon="upload" disabled={s.busy} onClick={()=>this.setState({importOpen:true})}>导入临时商品</Button>}{canDeleteRating(sessionStorage)&&<Button type="danger" loading={s.busy} disabled={s.loading||changed||!s.applied.from||!s.applied.to||s.total===0} onClick={this.preview}>清除该时间范围的临时商品</Button>}</div>
      {changed&&<p role="status">日期已修改，请先点击筛选。</p>}{s.error&&<p className="grading-error" role="alert">{s.error}</p>}
      <Table rowKey="id" loading={s.loading} columns={columns} dataSource={s.rows} scroll={{x:1200}} locale={{emptyText:'暂无符合条件的临时商品'}} pagination={{current:s.page,total:s.total,pageSize:20,onChange:page=>this.load(page),disabled:s.busy}}/>
      </section><Modal title="永久清除临时商品" visible={!!s.preview} confirmLoading={s.busy} closable={!s.busy} maskClosable={false} cancelButtonProps={{disabled:s.busy}} okText="永久清除" okType="danger" okButtonProps={{disabled:s.confirm!=='清除'}} cancelText="取消" onCancel={()=>{if(!s.busy)this.setState({preview:null})}} onOk={this.purge}>
      {s.preview&&<React.Fragment><p>{s.preview.from} 至 {s.preview.to}（含当天），共 {s.preview.count} 件。清除涵盖此范围的所有分页。</p><p>商品及其关联操作记录将从当前业务数据库彻底删除，不保留删除标记，不可撤销。不会清除正式评级商品。历史备份、服务器日志及磁盘照片不在本次数据库清除范围内。</p><label>请输入“清除”确认<Input autoComplete="off" disabled={s.busy} value={s.confirm} onChange={e=>this.setState({confirm:e.target.value})}/></label></React.Fragment>}
      </Modal>{s.importOpen&&<TemporaryImport onClose={imported=>{this.setState({importOpen:false});if(imported)this.setState({from:'',to:''},()=>this.load(1,{from:'',to:''}))}}/>}</main>
  }
}
