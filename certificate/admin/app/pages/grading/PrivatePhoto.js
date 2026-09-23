import React from 'react'
import { getFullUrl } from '../../service/http'

// Keep bearer credentials out of image URLs, history, referrers and downloads.
export default class PrivatePhoto extends React.Component {
  state = {url:'',error:false}
  componentDidMount() {this.active=true;this.load(this.props.src)}
  componentWillReceiveProps(props) {if(props.src!==this.props.src)this.load(props.src)}
  componentWillUnmount() {this.active=false;this.sequence=(this.sequence||0)+1;if(this.blob)URL.revokeObjectURL(this.blob)}
  async load(src) {
    const sequence=this.sequence=(this.sequence||0)+1
    if(this.blob) {URL.revokeObjectURL(this.blob);this.blob=''}
    this.setState({url:'',error:false})
    if(!src || !src.startsWith('/api/usr/gradingPhotoFile/')) {this.setState({url:src||''});return}
    try {
      const response=await fetch(getFullUrl('gradingPhoto')+'File/'+src.split('/').pop(),{headers:{Authorization:localStorage.getItem('token')},cache:'no-store'})
      if(!response.ok)throw new Error('照片读取失败')
      const blob=await response.blob()
      if(!this.active||sequence!==this.sequence)return
      this.blob=URL.createObjectURL(blob);this.setState({url:this.blob})
    } catch(e) {if(this.active&&sequence===this.sequence)this.setState({error:true})}
  }
  render() {
    if(!this.props.src)return <span>暂无照片</span>
    if(!this.state.url)return <span>{this.state.error?'照片暂不可用':'照片加载中'}</span>
    const img=<img src={this.state.url} alt={this.props.alt}/>
    return this.props.zoom?<a href={this.state.url} target="_blank" rel="noopener noreferrer">{img}</a>:img
  }
}
