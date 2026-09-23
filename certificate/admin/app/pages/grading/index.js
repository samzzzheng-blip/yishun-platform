import React from 'react'
import { Button, Input, Select, message, Tag, Pagination, Spin } from 'antd'
import { getData, postData, getFullUrl } from '../../service/http'
import { Link } from 'react-router'
import { hasRatingMenu, canEditRating, canDeleteRating } from '../../utils/ratingNavigation'
import moment from 'moment'
import PrivatePhoto from './PrivatePhoto'
import {TemplateManager,LabelFields} from './LabelTemplates'
import BatchPublish from './BatchPublish'
import WorkflowActions from './WorkflowActions'
import ReferenceLinks from './ReferenceLinks'
import BatchNumberEditor from './BatchNumberEditor'
import '../../styles/grading-workbench.less'

const stages = [['COLLECTING','待整理'],['PUBLISHING','待发布'],['DONE','已完成']]
const nextText = { COLLECTING:'提交到待发布' }
const fields = [['cardName','卡片名称',100],['series','系列',100],['cardYear','年份',20],['language','语言',40],['cardNumber','卡号',100],['batch','收卡批次',100]]
const grades = [['surface','表面'],['center','居中'],['edge','边缘'],['corner','角落'],['score','总分']]
const nameOf = stage => (stages.find(s=>s[0]===stage)||['',''])[1]
function requestId() {
  return 'G-xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,c=>{
    const a = new Uint8Array(1); window.crypto.getRandomValues(a)
    const n=a[0]%16; return (c==='x'?n:(n&3)|8).toString(16)
  })
}
const newCard = batch => ({jobNumber:requestId(),certNumber:'',batch:batch||'',frontPhoto:'',backPhoto:''})

export default class GradingWorkbench extends React.Component {
  state = { stage:'UNFINISHED', keyword:'', batch:'',batches:[],appliedFilter:{stage:'UNFINISHED',keyword:'',batch:''}, page:1, list:[], total:0,templates:[],templateAdmin:false,manageTemplates:false,templateDirty:false,
    job:null, history:[], busy:false, loading:false, dirty:false, create:this.props.location.pathname==='/rateIntake', intake:newCard(''),lastCreated:null, qr:'', width:70,height:35,error:'' }
  componentDidMount() {
    document.documentElement.classList.add('grading-document')
    this.viewport=document.querySelector('meta[name="viewport"]')
    if(this.viewport) {this.oldViewport=this.viewport.getAttribute('content');this.viewport.setAttribute('content','width=device-width, initial-scale=1')}
    window.addEventListener('beforeunload',this.beforeUnload)
    this.canRead=hasRatingMenu(JSON.parse(sessionStorage.getItem('gMenuList')||'[]'))
    this.canEdit=canEditRating(sessionStorage)
    this.removeLeaveHook=this.props.router.setRouteLeaveHook(this.props.route,()=>this.state.busy?'操作尚未完成，请稍后再离开。':this.state.dirty||this.state.templateDirty?'有未保存内容，确定离开当前页面吗？':undefined)
    if(this.canRead) {
      this.loadTemplates()
      this.load()
      const id=this.props.location.query.job
      if(id && /^\d+$/.test(id)) this.open({id:Number(id)})
    }
  }
  componentWillReceiveProps(nextProps) {
    if(nextProps.route!==this.props.route) {
      if(this.removeLeaveHook) this.removeLeaveHook()
      this.removeLeaveHook=nextProps.router.setRouteLeaveHook(nextProps.route,()=>this.state.busy?'操作尚未完成，请稍后再离开。':this.state.dirty||this.state.templateDirty?'有未保存内容，确定离开当前页面吗？':undefined)
    }
    if(nextProps.location.pathname!==this.props.location.pathname) {
      this.setState({create:nextProps.location.pathname==='/rateIntake',intake:newCard(this.state.intake.batch),dirty:false,job:null,error:''},()=>{
        if(nextProps.location.query.job) this.open({id:Number(nextProps.location.query.job)})
      })
    }
  }
  componentWillUnmount() {
    if(this.removeLeaveHook) this.removeLeaveHook()
    document.documentElement.classList.remove('grading-document')
    if(this.viewport) {if(this.oldViewport===null)this.viewport.removeAttribute('content');else this.viewport.setAttribute('content',this.oldViewport)}
    window.removeEventListener('beforeunload',this.beforeUnload)
    if(this.state.qr) URL.revokeObjectURL(this.state.qr)
  }
  beforeUnload = e => { if(this.state.busy||this.state.dirty||this.state.templateDirty) { e.preventDefault(); e.returnValue='' } }
  loadTemplates = async () => {try{const data=await getData('gradingTemplates');this.setState({templates:data.list,templateAdmin:data.canManage})}catch(e){this.fail(e)}}
  fail = e => {
    const text=e.message||'操作失败，请重试'
    this.setState({error:text})
    message.error(text)
  }
  load = async () => {
    const {stage,keyword,batch,page}=this.state,request=(this.loadRequest||0)+1;this.loadRequest=request
    this.setState({loading:true,error:''})
    try {
      const [data,batches]=await Promise.all([getData('gradingJobs',{stage,keyword,batch,page}),getData('gradingBatches')])
      if(request===this.loadRequest)this.setState({batches,appliedFilter:{stage,keyword,batch},list:data.content.map(row=>({...row,stage:row.rateId?'DONE':row.stage==='PUBLISHING'?'PUBLISHING':'COLLECTING'})),total:data.totalElements})
    } catch(e) {if(request===this.loadRequest)this.fail(e)} finally {if(request===this.loadRequest)this.setState({loading:false})}
  }
  newBatch = async () => {
    if(!this.batchRequest)this.batchRequest=requestId().slice(2)
    const data=await postData('gradingNewBatch',{requestId:this.batchRequest});this.batchRequest=null
    this.setState({batches:[data.batch,...this.state.batches.filter(b=>b!==data.batch)]})
    return data.batch
  }
  newIntakeBatch = async () => {
    this.setState({busy:true})
    try{const batch=await this.newBatch();this.setState({intake:{...this.state.intake,batch},dirty:true})}catch(e){this.fail(e)}finally{this.setState({busy:false})}
  }
  managed = async () => {this.setState({job:null,history:[],dirty:false,page:1});await this.load()}
  open = async row => {
    if(this.state.busy) return
    if(this.props.location.pathname==='/rateIntake') {this.props.router.push('/rateWorkflow?job='+row.id);return}
    if(this.state.dirty && !window.confirm('有未保存内容，确定离开当前档案吗？')) return
    this.setState({loading:true,error:''})
    try {
      const [job,history]=await Promise.all([getData('gradingJob',{id:row.id}),getData('gradingHistory',{id:row.id})])
      if(this.state.qr) URL.revokeObjectURL(this.state.qr)
      this.setState({job,history,dirty:false,create:false,qr:''},this.focusDetail)
    } catch(e) {this.fail(e)} finally {this.setState({loading:false})}
  }
  edit = (key,value) => this.setState({job:{...this.state.job,[key]:value},dirty:true})
  focusDetail = () => {if(window.innerWidth<=640) document.querySelector('.grading-detail').scrollIntoView({block:'start'})}
  photo = async (e,key,intake) => {
    const file=e.target.files[0]; e.target.value=''
    if(!file) return
    if(!['image/jpeg','image/png'].includes(file.type)||file.size>12*1024*1024) {message.error('请选择12MB以内的JPG或PNG照片');return}
    this.setState({busy:true})
    try {
      const form=new FormData();form.append('file',file)
      const response=await fetch(getFullUrl('gradingPhoto'),{method:'POST',headers:{Authorization:localStorage.getItem('token')},body:form})
      const data=await response.json()
      if(!response.ok||data.code!==200) throw new Error(data.msg||'照片上传失败')
      if(intake) this.setState({intake:{...this.state.intake,[key]:data.data},dirty:true})
      else this.edit(key,data.data)
    } catch(err) {this.fail(err)} finally {this.setState({busy:false})}
  }
  create = async () => {
    this.setState({busy:true})
    try {
      let intake=this.state.intake
      if(!intake.batch){const batch=await this.newBatch();intake={...intake,batch};this.setState({intake})}
      const job=await postData('gradingCreate',intake)
      message.success('已建档：'+(job.batch||'未分批次')+' · 编号 '+job.batchNumber)
      this.setState({dirty:false,intake:newCard(job.batch),lastCreated:job},this.load)
    } catch(e) {this.fail(e)} finally {this.setState({busy:false})}
  }
  save = async () => {
    const current=this.state.job
    const job=await postData(current.stage==='PUBLISHING'?'gradingSummarySave':'gradingSave',current.stage==='PUBLISHING'?{id:current.id,version:current.version,labelText:current.labelText,expectedSchema:current.templateSchema}:current)
    this.setState({job,dirty:false})
    return job
  }
  saveClick = async () => {
    if(this.saving||this.state.busy)return
    this.saving=true
    this.setState({busy:true})
    try {
      const job=await this.save()
      if(job.stage==='COLLECTING') {
        try {
          const data=await getData('gradingNextInBatch',{id:job.id})
          if(data.next) {
            const history=await getData('gradingHistory',{id:data.next.id})
            if(this.state.qr)URL.revokeObjectURL(this.state.qr)
            await new Promise(resolve=>this.setState({job:data.next,history,dirty:false,qr:'',batch:job.batch,stage:'COLLECTING',keyword:'',page:1},()=>{this.focusDetail();resolve()}))
            message.success('已保存编号 '+job.batchNumber+'，已切换到编号 '+data.next.batchNumber)
          } else {
            const history=await getData('gradingHistory',{id:job.id});this.setState({history})
            message.success('已保存；当前已是本批次最后一张待整理商品。发布状态未改变。')
          }
        } catch(e) {throw new Error('当前商品已保存，但未能切换下一张，请手动选择或重试。'+(e.message||''))}
      } else {
        const history=await getData('gradingHistory',{id:job.id});this.setState({history});message.success('汇总已保存')
      }
      await this.load()
    } catch(e) {this.fail(e)} finally {this.saving=false;this.setState({busy:false})}
  }
  advance = async action => {
    this.setState({busy:true})
    try {
      const certNumber=this.state.job.certNumber
      let job=this.state.job
      if(this.state.dirty) job=await this.save()
      job=await postData('gradingAdvance',{id:job.id,version:job.version,action,certNumber})
      const history=await getData('gradingHistory',{id:job.id})
      this.setState({job,history,dirty:false})
      message.success('已进入：'+nameOf(job.stage));await this.load()
    } catch(e) {this.fail(e)} finally {this.setState({busy:false})}
  }
  photoControl(key,title,intake=false) {
    const data=intake?this.state.intake:this.state.job
    return <div className="grading-photo" key={key}>
      <span>{title}</span>
      {data[key]?<PrivatePhoto src={data[key]} zoom alt={title+'，点击查看大图'}/>:<div className="grading-photo-empty">尚未上传</div>}
      <label className="grading-file">{data[key]?'更换照片':'拍照或选择照片'}<input disabled={this.state.busy||!this.canEdit} type="file" accept="image/jpeg,image/png" capture="environment" onChange={e=>this.photo(e,key,intake)}/></label>
    </div>
  }
  render() {
    const s=this.state,j=s.job, editable=j&&j.stage==='COLLECTING'
    const canRead=hasRatingMenu(JSON.parse(sessionStorage.getItem('gMenuList')||'[]')), canEdit=canEditRating(sessionStorage),canDelete=canDeleteRating(sessionStorage)
    const actionProps={canEdit,canDelete,batches:s.batches,newBatch:this.newBatch,onChanged:this.managed,onBusy:busy=>this.setState({busy}),dirty:s.dirty||s.templateDirty,disabled:s.busy||s.loading}
    if(!canRead || (s.create&&!canEdit)) return <div className="grading-access" role="alert">当前账号没有{!canRead?'评级查看':'收卡编辑'}权限，请联系管理员。</div>
    return <main className="grading-workbench">
      <header className="grading-top"><div><h1>{s.create?'收卡建档':'评级流程'}</h1><p>工作台 / 评级 / {s.create?'收卡建档':'评级流程'}</p></div><nav>
        <Link to="/rateManage">评级管理</Link>
        {s.create?<Link to="/rateWorkflow">查看评级流程</Link>:canEdit&&<Button type="primary" disabled={s.busy} icon="camera" onClick={()=>this.props.router.push('/rateIntake')}>收卡建档</Button>}
      </nav></header>
      {window.location.hostname==='127.0.0.1'&&<p className="grading-session">本地测试环境 · 与线上数据隔离</p>}
      <p className="grading-session">发布前仅当前账号可见；手机和电脑可同时登录，上传后点击刷新查看。整批发布后进入正式评级，按原权限共享。</p>
      {!canEdit&&<p className="grading-session">当前为只读权限，可查看档案；编辑需评级新增或修改权限。</p>}
      {s.templateAdmin&&<Button disabled={s.templateDirty} onClick={()=>this.setState({manageTemplates:!s.manageTemplates})}>{s.manageTemplates?'收起模板管理':'管理卡片信息模板'}</Button>}
      {s.manageTemplates&&s.templateAdmin&&<TemplateManager templates={s.templates} reload={this.loadTemplates} onDirty={templateDirty=>this.setState({templateDirty})}/>}
      {s.error&&<div className="grading-error" role="alert">{s.error} <Button size="small" onClick={this.load}>重试</Button></div>}
      <div className="grading-stages" hidden={s.create} aria-label="按进度筛选">
        {[['','全部'],['UNFINISHED','未完成'],...stages].map(([value,label])=><button key={value} disabled={s.busy} aria-pressed={s.stage===value} onClick={()=>this.setState({stage:value,page:1},this.load)}>{label}</button>)}
      </div>
      <div className={s.create?"grading-layout grading-intake-layout":"grading-layout"}>
        <aside className="grading-list"><form className="grading-search" onSubmit={e=>{e.preventDefault();this.setState({page:1},this.load)}}><Input aria-label="搜索流水号、证书编号、名称或批次" placeholder="流水号 / 编号 / 名称 / 批次" value={s.keyword} onChange={e=>this.setState({keyword:e.target.value})}/><Button htmlType="submit">搜索</Button></form>
          <label className="grading-batch-filter">按批次筛选<Select aria-label="按批次筛选" showSearch disabled={s.busy} value={s.batch} onChange={batch=>this.setState({batch,page:1},this.load)}><Select.Option value="">全部批次</Select.Option>{s.batches.map(batch=><Select.Option key={batch} value={batch}>{batch}</Select.Option>)}</Select></label>
          <div className="grading-list-meta">共 {s.total} 张卡片 <Button size="small" onClick={this.load}>刷新</Button></div>
          <WorkflowActions {...actionProps} disabled={s.busy||s.loading||!s.total} scope={s.appliedFilter}/>
          <Spin spinning={s.loading}><div className="grading-rows">
            {!s.list.length&&<p className="grading-empty">暂无符合条件的档案。可调整批次或状态筛选，或点击“收卡建档”。</p>}
            {s.list.map(row=><button disabled={s.busy} className={'grading-row '+(j&&j.id===row.id?'selected':'')} key={row.id} onClick={()=>this.open(row)}>
              <PrivatePhoto src={row.finishedPhoto||row.frontPhoto} alt="卡片缩略图"/>
              <span><strong>{row.cardName||'待整理卡片'}</strong><small>{row.rateId?row.certNumber:row.batchNumber!=null?'批次编号 '+row.batchNumber:row.certNumber||row.jobNumber}</small><small>{row.batch||'未分批次'}</small><Tag>{nameOf(row.stage)}</Tag></span>
            </button>)}
          </div></Spin>
          <Pagination simple current={s.page} pageSize={20} total={s.total} onChange={page=>this.setState({page},this.load)}/>
        </aside>
        <section className="grading-detail">
        {s.create?<div><h2>收卡建档</h2><p>同批次从 1 自动编号，照片选填。批次名按日期、账号和序号生成，例如 9.11demo1；保存后保留批次继续收卡。</p><p>整理并提交后，在待发布中设置批次起始编号，整批确认完成。</p>
          <div className="grading-template-select"><label className="grading-field">收卡批次<Select aria-label="收卡批次" showSearch disabled={s.busy} placeholder="首次保存时自动创建批次" value={s.intake.batch||undefined} onChange={batch=>this.setState({intake:{...s.intake,batch},dirty:true})}>{s.batches.map(batch=><Select.Option key={batch} value={batch}>{batch}</Select.Option>)}</Select></label><Button disabled={s.busy} onClick={this.newIntakeBatch}>新增批次</Button></div>
          <div className="grading-photos">{this.photoControl('frontPhoto','卡片正面',true)}{this.photoControl('backPhoto','卡片背面',true)}</div>
          <Button type="primary" loading={s.busy} onClick={this.create}>保存档案并继续收卡</Button>
          {s.lastCreated&&<p className="grading-created">已建档：{s.lastCreated.batch||'未分批次'} · 编号 {s.lastCreated.batchNumber} <Link to={'/rateWorkflow?job='+s.lastCreated.id}>整理这张卡片</Link></p>}
        </div>:!j?<div className="grading-empty"><h2>选择一张卡片开始处理</h2><p>手机上传的档案会出现在左侧。按进度筛选后，可逐张整理信息和评分。</p></div>:<div>
          <div className="grading-detail-title"><div><h2>{j.cardName||'待整理卡片'}</h2><p>{j.rateId?'编号 '+j.certNumber:j.batchNumber!=null?(j.batch||'未分批次')+' · 编号 '+j.batchNumber:j.jobNumber}</p></div><Tag color={j.stage==='DONE'?'green':'blue'}>{nameOf(j.stage)}</Tag></div>
          <WorkflowActions key={j.id} {...actionProps} single scope={{id:j.id}}/>
          {canEdit&&!j.rateId&&<BatchNumberEditor key={'number-'+j.id} job={j} disabled={s.busy} dirty={s.dirty||s.templateDirty} onBusy={busy=>this.setState({busy})} onSaved={async job=>{const history=await getData('gradingHistory',{id:job.id});this.setState({job,history});await this.load()}}/>}
          <ol className="grading-progress">{stages.map(([code,label])=><li key={code} className={code===j.stage?'active':''}>{label}</li>)}</ol>
          <fieldset disabled={s.busy||!canEdit}>
          {editable?<div>
            <div className="grading-photos">{this.photoControl('frontPhoto','卡片正面')}{this.photoControl('backPhoto','卡片背面')}</div>
            <ReferenceLinks key={j.id} frontPhoto={j.frontPhoto} backPhoto={j.backPhoto} finishedPhoto={j.finishedPhoto}/>
            <LabelFields job={j} fallbackFields={fields} templates={s.templates} editable={canEdit&&!s.busy} dirty={s.dirty} onChange={this.edit} onSelected={job=>this.setState({job,dirty:false})}/>
            <h3>人工评分</h3><p>请结合实物检查后填写。总分由评级员确认。</p><div className="grading-scores">{grades.map(([key,label])=><label key={key}>{label}<Select disabled={s.busy||!canEdit} aria-label={label} allowClear value={j[key]||undefined} onChange={value=>this.edit(key,value||'')} placeholder="待评分">{Array.from({length:21},(_,i)=>10-i/2).map(score=><Select.Option key={score} value={String(score)}>{score}</Select.Option>)}</Select></label>)}</div>
            <label className="grading-field">工作备注（确认时最多500字）<Input.TextArea maxLength={500} rows={3} value={j.notes||''} onChange={e=>this.edit('notes',e.target.value)}/></label>
          </div>:<div><LabelFields job={j} fallbackFields={fields} templates={s.templates} editable={false} dirty={s.dirty} summaryEditable={j.stage==='PUBLISHING'&&canEdit&&!s.busy} onChange={this.edit}/><div className="grading-facts"><p><span>正式证书编号</span>{j.certNumber}</p>{grades.map(([key,label])=><p key={key}><span>{label}</span>{j[key]}</p>)}</div>
            <div className="grading-originals"><a href={j.frontPhoto} target="_blank" rel="noopener noreferrer">查看收卡正面</a><a href={j.backPhoto} target="_blank" rel="noopener noreferrer">查看收卡背面</a></div>
          </div>}
          </fieldset>
          {j.rateId&&<p><Link to={'/rateManage?certNumber='+encodeURIComponent(j.certNumber)}>在评级管理中查看证书 {j.certNumber}</Link></p>}
          {canEdit&&j.stage==='PUBLISHING'&&<BatchPublish job={j} disabled={s.dirty||s.busy} onBusy={busy=>this.setState({busy})} onPublished={async result=>{if(result.temporary){this.setState({busy:false,dirty:false,job:null,history:[]},()=>this.props.router.push('/rateTemporary'));return}const job=await getData('gradingJob',{id:j.id});const history=await getData('gradingHistory',{id:j.id});this.setState({job,history,stage:'DONE',page:1},this.load)}}/>}
          <footer className="grading-actions"><span aria-live="polite">{s.dirty?'有未保存内容':'已保存'}</span>
            {canEdit&&editable&&<Button loading={s.busy} onClick={this.saveClick}>保存并下一张</Button>}
            {canEdit&&j.stage==='PUBLISHING'&&<Button type="primary" disabled={!s.dirty} loading={s.busy} onClick={this.saveClick}>保存汇总</Button>}
            {canEdit&&j.stage==='PUBLISHING'&&<Button disabled={s.busy} onClick={()=>this.advance('REOPEN')}>退回整理</Button>}
            {canEdit&&nextText[j.stage]&&<Button type="primary" loading={s.busy} onClick={()=>this.advance('NEXT')}>{nextText[j.stage]}</Button>}
          </footer>
          <details className="grading-history"><summary>操作记录 · {s.history.length}条</summary>{s.history.map(h=><p key={h.id}>{moment(h.createdAt).format('MM-DD HH:mm')} · {h.actor} · {{CREATE:'建档',SAVE:'保存',SAVE_SUMMARY:'保存信息汇总',SAVE_BATCH_NUMBER:'修改批次编号',NEXT:'提交',REOPEN:'退回',PUBLISH_BATCH:'整批发布',MOVE:'修改批次',DELETE:'从评级流程删除'}[h.action]} · {nameOf(h.stage)||'历史步骤'}</p>)}</details>
        </div>}
        </section>
      </div>
    </main>
  }
}
