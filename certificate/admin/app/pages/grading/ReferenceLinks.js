import React from 'react'
import {Button} from 'antd'
import {getFullUrl} from '../../service/http'
import {copyPhoto} from './copyPhoto'

const GOOGLE='https://www.google.com/imghp'
// A native multipart navigation sends only image bytes, never our bearer token or private URL.
export default class ReferenceLinks extends React.Component {
  state={busy:false,error:'',sent:false,copying:false,copied:false}
  componentDidMount(){this.active=true}
  componentWillReceiveProps(props){if(props.frontPhoto!==this.props.frontPhoto||props.backPhoto!==this.props.backPhoto||props.finishedPhoto!==this.props.finishedPhoto)this.setState({copied:false,error:'',sent:false})}
  componentWillUnmount(){this.active=false;if(this.abort)this.abort.abort()}
  read=async src=>{
    const privatePhoto=/^\/api\/usr\/gradingPhotoFile\/[a-f0-9-]+\.(png|jpg)$/.test(src)
    if(!privatePhoto&&!/^\/upload\/precious\/workflow\/[a-f0-9-]+\.(png|jpg)$/.test(src))throw new Error('照片地址暂不支持自动传图，请打开照片后手动上传。')
    this.abort=new AbortController()
    const timeout=setTimeout(()=>this.abort.abort(),20000)
    try{
      const url=privatePhoto?getFullUrl('gradingPhoto')+'File/'+src.split('/').pop():src
      const response=await fetch(url,{headers:privatePhoto?{Authorization:localStorage.getItem('token')}:{},cache:'no-store',signal:this.abort.signal})
      if(!response.ok)throw new Error('照片读取失败，请刷新登录状态或重新上传照片。')
      const blob=await response.blob()
      if(!blob.size||blob.size>12*1024*1024)throw new Error('照片为空或超过12MB，无法自动传图。')
      const bytes=new Uint8Array(await blob.slice(0,8).arrayBuffer())
      const png=bytes[0]===137&&bytes[1]===80&&bytes[2]===78&&bytes[3]===71,jpg=bytes[0]===255&&bytes[1]===216&&bytes[2]===255
      if(!png&&!jpg)throw new Error('无法读取图片文件，请重新上传JPG或PNG照片。')
      return new File([blob],png?'card.png':'card.jpg',{type:png?'image/png':'image/jpeg'})
    }finally{clearTimeout(timeout)}
  }
  search=async()=>{
    if(this.state.busy)return
    const src=this.props.frontPhoto||this.props.backPhoto||this.props.finishedPhoto
    if(!src){window.open(GOOGLE,'_blank','noopener,noreferrer');return}
    const tab=window.open('about:blank','_blank')
    if(!tab){this.setState({error:'浏览器拦截了新标签页，请允许此网站打开弹窗后重试。'});return}
    tab.opener=null
    const meta=tab.document.createElement('meta');meta.name='referrer';meta.content='no-referrer';tab.document.head.appendChild(meta)
    tab.document.title='正在准备谷歌识图';tab.document.body.textContent='正在读取所选卡片照片，请稍候…'
    this.setState({busy:true,error:'',sent:false})
    try{
      const file=await this.read(src)
      if(!this.active){if(!tab.closed)tab.close();return}
      if(tab.closed)throw new Error('搜索标签页已关闭，请重新点击谷歌识图。')
      const form=tab.document.createElement('form');form.method='POST';form.action='https://lens.google.com/v3/upload';form.enctype='multipart/form-data'
      const input=tab.document.createElement('input');input.type='file';input.name='encoded_image'
      const transfer=new DataTransfer();transfer.items.add(file);input.files=transfer.files;form.appendChild(input)
      tab.document.body.appendChild(form);form.submit();this.setState({sent:true})
    }catch(e){
      if(!this.active){if(!tab.closed)tab.close();return}
      if(!tab.closed)tab.location.replace(GOOGLE)
      this.setState({error:e.name==='AbortError'?'照片读取超时，请重试或手动上传。':e.message||'浏览器不支持自动传图，请手动上传。'})
    }finally{if(this.active)this.setState({busy:false})}
  }
  download=async()=>{
    this.setState({busy:true,error:''})
    try{const file=await this.read(this.props.frontPhoto||this.props.backPhoto||this.props.finishedPhoto),url=URL.createObjectURL(file),a=document.createElement('a');a.href=url;a.download=file.name;document.body.appendChild(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(url),60000)}
    catch(e){this.setState({error:e.message||'照片下载失败，请重试。'})}finally{this.setState({busy:false})}
  }
  copy=async()=>{
    if(this.copying||this.state.busy)return
    const src=this.props.frontPhoto||this.props.backPhoto||this.props.finishedPhoto
    if(!src)return
    this.copying=true;this.setState({busy:true,copying:true,copied:false,error:''})
    try{
      await copyPhoto(()=>this.read(src))
      if(this.active)this.setState({copied:true})
    }catch(e){if(this.active)this.setState({error:e.name==='NotAllowedError'?'未能复制图片，请允许浏览器访问剪贴板后重试，或使用旁边的下载按钮。':e.message||'复制图片失败，请重试或下载图片。'})}
    finally{this.copying=false;if(this.active)this.setState({busy:false,copying:false})}
  }
  render(){const photo=this.props.frontPhoto||this.props.backPhoto||this.props.finishedPhoto
    return <section aria-label="资料查询" className="grading-references">
      <nav className="grading-reference-links"><span>资料查询</span><a href="https://www.suruga-ya.jp/search?category=&search_word=SMD-1-022&searchbox=1" target="_blank" rel="noopener noreferrer">骏河屋查询</a><a href="https://wiki.52poke.com/wiki/%E4%B8%BB%E9%A1%B5" target="_blank" rel="noopener noreferrer">神奇宝贝百科</a><Button type="link" loading={this.state.busy} onClick={this.search}>谷歌识图</Button><a href="https://www.doubao.com/chat/" target="_blank" rel="noopener noreferrer">豆包网页版</a></nav>
      <p className="grading-session">{photo?'点击谷歌识图会将'+(this.props.frontPhoto?'正面':this.props.backPhoto?'背面':'成品')+'照片发送给 Google；豆包仅打开网页，不自动传图。':'没有照片，谷歌识图将直接打开 Google 图片搜索；豆包打开网页版。'} 均在新标签页打开。</p>
      {this.state.sent&&<p className="grading-session" role="status">已提交照片并打开 Google。若图片未带入或出现验证，请在 Google 页面手动上传。</p>}
      {this.state.error&&<p className="grading-error" role="alert">{this.state.error}</p>}
      {photo&&<div className="grading-management-actions"><Button disabled={this.state.busy} onClick={this.download}>下载当前照片，备用上传</Button><Button icon="copy" loading={this.state.copying} disabled={this.state.busy&&!this.state.copying} onClick={this.copy}>复制图片</Button></div>}
      {this.state.copied&&<p className="grading-session" role="status">图片已复制，可粘贴到聊天或支持图片的输入框。</p>}
    </section>
  }
}
