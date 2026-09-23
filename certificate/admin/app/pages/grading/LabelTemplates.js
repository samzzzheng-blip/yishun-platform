import React from 'react'
import {Button,Input,Select,Checkbox,Modal,message} from 'antd'
import {postData} from '../../service/http'

// Extend the existing Ant Design administration form: ordered fields, explicit separators,
// live plain-text preview. No new visual system or independent route.
export const parseObject = (value,fallback={}) => {try{return JSON.parse(value||'')||fallback}catch(e){return fallback}}
export function labelPreview(schema,values,name) {
  let output='',join=''
  ;(schema.fields||[]).forEach(field=>{
    const value=String(field.key==='cardName'?name||'':values[field.key]||'').trim()
    if(value) {output+=(output?join:'')+value;join=field.join==='newline'?'\n':' '}
  })
  return output
}
const blank=()=>({name:'',archived:false,fields:[{key:'cardName',label:'名称',join:'newline'}]})
export class TemplateManager extends React.Component {
  state={draft:blank(),dirty:false,busy:false,error:''}
  change = patch => {this.setState({draft:{...this.state.draft,...patch},dirty:true});this.props.onDirty(true)}
  choose = t => {
    const action=()=>{this.setState({draft:t?{...t,fields:parseObject(t.fieldsJson,[])}:blank(),dirty:false,error:''});this.props.onDirty(false)}
    if(this.state.dirty)Modal.confirm({title:'放弃未保存的模板修改？',onOk:action});else action()
  }
  field = (index,patch) => this.change({fields:this.state.draft.fields.map((f,i)=>i===index?{...f,...patch}:f)})
  move = (index,delta) => {const fields=[...this.state.draft.fields];[fields[index],fields[index+delta]]=[fields[index+delta],fields[index]];this.change({fields})}
  save = async () => {
    this.setState({busy:true,error:''})
    try {const saved=await postData('gradingTemplateSave',{...this.state.draft,fieldsJson:JSON.stringify(this.state.draft.fields)});this.setState({draft:{...saved,fields:parseObject(saved.fieldsJson,[])},dirty:false});this.props.onDirty(false);await this.props.reload();message.success('模板已保存，已选用的批次保持原版本；需要更新时重新选用。')}
    catch(e){this.setState({error:e.message||'模板保存失败，请重试'})}finally{this.setState({busy:false})}
  }
  render() {
    const {draft:d,busy,error,dirty}=this.state
    const demo={};d.fields.forEach(f=>{demo[f.key]='〈'+(f.label||'字段')+'〉'})
    return <section className="grading-template-manager" aria-label="卡片信息模板管理">
      <h2>卡片信息模板管理</h2><p>模板决定卡片需要填写哪些信息，固定保留名称字段，可调整顺序。空格与换行控制信息汇总的排列，不改变打印标签格式。</p>
      <label className="grading-field">已有模板<Select disabled={busy} value={d.id||undefined} placeholder="新建模板" onChange={id=>this.choose(this.props.templates.find(t=>t.id===id))}>{this.props.templates.map(t=><Select.Option key={t.id} value={t.id}>{t.name}{t.archived?'（已停用）':''}</Select.Option>)}</Select></label>
      <Button disabled={busy} onClick={()=>this.choose(null)}>新建模板</Button>
      <label className="grading-field">模板名称<Input disabled={busy} maxLength={80} value={d.name} onChange={e=>this.change({name:e.target.value})}/></label>
      <div className="grading-template-fields">{d.fields.map((f,i)=><div className="grading-template-row" key={f.key}>
        <label>字段 {i+1}<Input aria-label={'字段 '+(i+1)+' 名称'} disabled={busy||f.key==='cardName'} maxLength={40} value={f.label} onChange={e=>this.field(i,{label:e.target.value})}/></label>
        <label>字段后连接<Select aria-label={'字段 '+(i+1)+' 连接方式'} disabled={busy} value={f.join} onChange={join=>this.field(i,{join})}><Select.Option value="space">空格</Select.Option><Select.Option value="newline">换行</Select.Option></Select></label>
        <div className="grading-template-row-actions"><Button aria-label={'上移字段 '+(i+1)} disabled={busy||i===0} icon="arrow-up" onClick={()=>this.move(i,-1)}/><Button aria-label={'下移字段 '+(i+1)} disabled={busy||i===d.fields.length-1} icon="arrow-down" onClick={()=>this.move(i,1)}/><Button disabled={busy||f.key==='cardName'} onClick={()=>this.change({fields:d.fields.filter((_,n)=>n!==i)})}>移除</Button></div>
      </div>)}</div>
      <Button disabled={busy||d.fields.length>=20} onClick={()=>this.change({fields:[...d.fields,{key:'f'+Date.now().toString(36)+Math.random().toString(36).slice(2,8),label:'',join:'space'}]})}>添加字段</Button>
      <p><Checkbox disabled={busy} checked={d.archived} onChange={e=>this.change({archived:e.target.checked})}>停用模板（已选用的批次不受影响）</Checkbox></p>
      <h3>结构预览</h3><pre className="grading-label-preview">{labelPreview({fields:d.fields},demo,'〈名称〉')}</pre>
      {error&&<p role="alert" className="grading-error">{error}</p>}
      <Button type="primary" loading={busy} disabled={!dirty} onClick={this.save}>保存模板</Button>
    </section>
  }
}
export class LabelFields extends React.Component {
  state={choice:undefined,busy:false,error:'',switching:false,scopeOpen:false}
  componentWillReceiveProps(next) {if(next.job.id!==this.props.job.id)this.setState({choice:undefined,error:'',switching:false,scopeOpen:false})}
  apply = async scope => {
    this.setState({busy:true,error:''})
    try {
      const j=this.props.job;const job=await postData('gradingTemplateSelect',{id:j.id,version:j.version,templateId:this.state.choice,expectedSchema:j.templateSchema||'',scope})
      this.props.onSelected(job);this.setState({choice:undefined,switching:false,scopeOpen:false});message.success(scope==='SINGLE'?'已应用到当前卡片，可以填写信息':'已应用到同批次未确认卡片，可以填写信息')
    } catch(e){this.setState({error:e.message||'应用失败，请刷新后重试'})}finally{this.setState({busy:false})}
  }
  render() {
    const {job:j,templates,editable,dirty,onChange}=this.props,schema=parseObject(j.templateSchema),values=parseObject(j.templateValues)
    const selected=templates.find(t=>t.id===this.state.choice),preview=!!selected
    const displaySchema=preview?{fields:parseObject(selected.fieldsJson,[]),name:selected.name}:schema
    const formFields=displaySchema.fields||this.props.fallbackFields.filter(f=>f[0]!=='batch').map(([key,label,max])=>({key,label,max}))
    return <section className="grading-label-fields" aria-label="卡片信息"><h3>卡片信息</h3>
      <div className="grading-template-current"><span>{schema.name?'当前信息模板：'+schema.name:'未选择模板，暂使用原有卡片信息字段。'}{j.batch?' · 批次：'+j.batch:' · 未分批次，仅当前卡片'}</span>{editable&&<Button disabled={this.state.busy} onClick={()=>this.setState({switching:!this.state.switching,choice:undefined,error:''})}>{this.state.switching?'取消切换':'切换模板'}</Button>}</div>
      {editable&&this.state.switching&&<div className="grading-template-select"><label>选择信息模板<Select aria-label="选择卡片信息模板" value={this.state.choice} disabled={this.state.busy} placeholder="选择模板后预览字段" onChange={choice=>this.setState({choice,error:''})}>{templates.filter(t=>!t.archived).map(t=><Select.Option value={t.id} key={t.id}>{t.name}</Select.Option>)}</Select></label><Button disabled={!selected||dirty||this.state.busy} onClick={()=>this.setState({scopeOpen:true})}>应用模板</Button></div>}
      {preview&&<p role="status">正在预览：{selected.name}。以下字段暂不可填写，应用模板后解锁。</p>}
      <Modal title="选择模板应用范围" visible={this.state.scopeOpen} footer={null} closable={!this.state.busy} maskClosable={!this.state.busy} onCancel={()=>{if(!this.state.busy)this.setState({scopeOpen:false})}}>
        <p>仅当前卡片：不改变同批次其他卡片的模板。</p><p>同批次一键应用：替换该批次所有未确认卡片的模板（含单卡模板及其他员工的卡片），以后新增的同批次卡片也沿用。只更换字段结构，不复制填写内容；已确认记录不变。</p>
        {!j.batch&&<p>当前未填写收卡批次，只能应用到当前卡片。</p>}
        {this.state.error&&<p role="alert" className="grading-error">{this.state.error}</p>}
        <div className="grading-template-scope"><Button disabled={this.state.busy} onClick={()=>this.apply('SINGLE')}>仅当前卡片</Button><Button type="primary" disabled={!j.batch||this.state.busy} onClick={()=>this.apply('BATCH')}>同批次一键应用</Button></div>
      </Modal>
      {editable&&dirty&&<p>请先保存卡片信息，再应用模板。</p>}
      {this.state.error&&<p role="alert" className="grading-error">{this.state.error}</p>}
      <div className="grading-fields">{formFields.map(f=>{
        const direct=!displaySchema.fields||f.key==='cardName'
        return <label className="grading-field" key={f.key}>{f.label}<Input maxLength={f.key==='cardName'?100:f.max||200} disabled={preview||!editable||this.state.busy} value={direct?j[f.key]||'':values[f.key]||''} onChange={e=>direct?onChange(f.key,e.target.value):onChange('templateValues',JSON.stringify({...values,[f.key]:e.target.value}))}/></label>
      })}<label className="grading-field">收卡批次<Input readOnly value={j.batch||''}/><small>通过上方“修改批次”操作调整。</small></label></div>
      <div className="grading-summary"><p>卡片信息汇总</p>{this.props.summaryEditable?<React.Fragment>
        <Input.TextArea aria-label="卡片信息汇总" rows={5} maxLength={5000} value={j.labelText!=null?j.labelText:labelPreview(displaySchema.fields?displaySchema:{fields:formFields.map(f=>({...f,join:'space'}))},displaySchema.fields?values:j,j.cardName)} onChange={e=>onChange('labelText',e.target.value)}/>
        <p>可调整文字、空格和换行，最多5000字。点击下方“保存汇总”后生效；不修改名称字段、评分或待发布状态。</p>
        <Button disabled={j.labelText==null} onClick={()=>onChange('labelText',null)}>恢复自动汇总</Button>
      </React.Fragment>:<pre className="grading-label-preview">{!preview&&j.labelText!=null?j.labelText:labelPreview(displaySchema.fields?displaySchema:{fields:formFields.map(f=>({...f,join:'space'}))},displaySchema.fields?values:j,j.cardName)||'填写信息后显示汇总'}</pre>}</div>
    </section>
  }
}
