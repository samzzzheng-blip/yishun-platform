// Only matches files explicitly selected by the user. Never resolves local paths itself.
export function normalizePath(value, uri = false) {
  let path = String(value || '').trim().replace(/\\/g, '/');
  if (uri && /^file:/i.test(path)) {
    path = path.replace(/^file:\/*/i, '');
    try { path = decodeURIComponent(path); } catch (e) { throw new Error('文件路径编码无效'); }
  }
  return path.normalize('NFC').replace(/^\/+/, '').replace(/\/+/g, '/').toLowerCase();
}

export function matchAttachments(refs, files) {
  const entries = files.map(file => ({ file, path: normalizePath(file.webkitRelativePath || file.name) }));
  return refs.map(ref => {
    const path = normalizePath(ref, true);
    let matches = entries.filter(entry => entry.path === path || path.endsWith('/' + entry.path) || entry.path.endsWith('/' + path));
    if (!matches.length) {
      const name = path.split('/').pop();
      matches = entries.filter(entry => entry.path.split('/').pop() === name);
    }
    if (matches.length !== 1) throw new Error(`${ref}：${matches.length ? '存在多个同名附件，请在表格填写可区分的相对路径' : '未找到附件，请重新选择包含该文件的文件夹'}`);
    return { ref, file: matches[0].file, path: matches[0].file.webkitRelativePath || matches[0].file.name };
  });
}
