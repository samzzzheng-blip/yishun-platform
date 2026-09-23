import React, { Component } from 'react';
import { Button, Modal } from 'antd';
import axios from 'axios';
import api from '../../../service/api';
import { matchAttachments } from './attachmentMatch';

export default class ExcelImport extends Component {
  state = { visible: false, excel: null, files: [], matches: null, busy: false, progress: '', error: '', result: null };
  request = async (url, body, onUploadProgress) => {
    const response = await axios.post(url, body, {
      headers: { Authorization: localStorage.getItem('token') },
      timeout: 600000,
      onUploadProgress,
    });
    if (!response.data || response.data.code !== 200) throw new Error((response.data || {}).msg || '请求失败');
    return response.data.data;
  };
  check = async () => {
    if (!this.state.excel || this.state.busy) return;
    this.setState({ busy: true, error: '', progress: '正在检查表格引用…', matches: null });
    try {
      const body = new FormData(); body.append('file', this.state.excel);
      const refs = await this.request(api.uploadPreciousExcel.replace('uploadPreciousExcel', 'previewPreciousExcel'), body);
      const matches = matchAttachments(refs, this.state.files);
      const size = this.state.excel.size + matches.reduce((sum, match) => sum + match.file.size, 0);
      if (size > 90 * 1024 * 1024) throw new Error('Excel 与引用附件合计超过 90MB，请拆分表格后分批导入');
      this.setState({ matches, progress: `检查通过，匹配 ${matches.length} 个附件引用，合计 ${(size / 1024 / 1024).toFixed(1)} MB。请核对下方匹配清单。` });
    } catch (e) { this.setState({ error: e.message || '预检失败', progress: '' }); }
    finally { this.setState({ busy: false }); }
  };
  submit = async () => {
    if (this.state.busy || !this.state.matches || this.state.result) return;
    this.setState({ busy: true, error: '', progress: '正在上传，请勿关闭页面…' });
    try {
      const body = new FormData(); body.append('file', this.state.excel);
      body.append('attachmentRefs', JSON.stringify(this.state.matches.map(match => match.ref)));
      this.state.matches.forEach(match => body.append('attachments', match.file, match.file.name));
      const result = await this.request(api.uploadPreciousExcel, body, event => {
        if (event.total) this.setState({ progress: event.loaded < event.total ? `上传 ${Math.floor(event.loaded * 100 / event.total)}%` : '上传完成，服务器正在导入，请勿重复提交…' });
      });
      this.setState({ result, progress: `共 ${result.total} 行，成功 ${result.imported} 行，跳过 ${result.skipped} 行` });
      if (result.imported > 0) this.props.onImported();
    } catch (e) {
      this.setState({ error: `${e.message || '请求失败'}。如上传已开始，请先核查导入结果，再重新打开导入窗口，避免重复提交。`, matches: null, progress: '' });
    } finally { this.setState({ busy: false }); }
  };
  render() {
    const { visible, busy, excel, files, matches, error, progress, result } = this.state;
    return <span>
      <Button type="primary" onClick={() => this.setState({ visible: true, excel: null, files: [], matches: null, error: '', progress: '', result: null })}>excel导入</Button>
      <Modal title="宝贝管理 Excel 导入" visible={visible} width={700} destroyOnClose maskClosable={false}
        closable={!busy} keyboard={!busy} onCancel={() => !busy && this.setState({ visible: false })}
        footer={<span><Button disabled={busy} onClick={() => this.setState({ visible: false })}>关闭</Button>
          <Button disabled={!excel || busy || !!result} onClick={this.check}>检查附件匹配</Button>
          <Button type="primary" loading={busy} disabled={!matches || busy || !!result} onClick={this.submit}>确认导入</Button></span>}>
        <p>仅证书编号必填，重复编号跳过、不覆盖。只读取第一张工作表。</p>
        <p><label htmlFor="precious-excel-file">1. 选择 Excel（必选，最大 20MB）</label></p>
        <input id="precious-excel-file" type="file" accept=".xls,.xlsx" disabled={busy || !!result}
          onChange={e => this.setState({ excel: e.target.files[0] || null, matches: null, error: '', progress: '' })} />
        <p style={{ marginTop: 20 }}><label htmlFor="precious-attachment-folder">2. 选择照片、视频所在文件夹（表格引用本地文件时必选）</label></p>
        <input id="precious-attachment-folder" type="file" webkitdirectory="" directory="" multiple disabled={busy || !!result}
          onChange={e => this.setState({ files: Array.from(e.target.files), matches: null, error: '', progress: '' })} />
        <p>已选择 {files.length} 个文件；只有表格引用的附件会被上传。文件夹包含子文件夹时也可匹配。不支持目录选择的浏览器请使用 Chrome/Edge。</p>
        <p>图片：JPG/PNG/GIF，每张 ≤10MB；视频：MP4/MOV/WebM/M4V，每个 ≤80MB；整批 ≤90MB。视频不自动生成封面。</p>
        <div aria-live="polite">{progress}</div>
        {error ? <p role="alert" style={{ color: '#b42318', whiteSpace: 'pre-wrap', overflowWrap: 'break-word' }}>{error}</p> : null}
        {matches && matches.length ? <div style={{ maxHeight: 220, overflow: 'auto' }}><ul>{matches.map(match => <li key={match.ref} style={{ overflowWrap: 'break-word' }}>{match.ref} → {match.path}</li>)}</ul></div> : null}
        {result ? <div style={{ maxHeight: 220, overflow: 'auto' }}>{(result.errors || []).map((text, i) => <p key={i}>{text}</p>)}{result.skipped > 100 ? <p>仅显示前 100 条错误。</p> : null}</div> : null}
      </Modal>
    </span>;
  }
}
