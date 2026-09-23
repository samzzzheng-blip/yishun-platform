export function getbackStatus(status) {
  return ({ 0: '待发货', 1: '已发货', 2: '已收货' })[status] || '处理中';
}
export function findGetback(records, id) {
  return records.find(record => String(record.id) === String(id)) || null;
}
export function getbackPhotos(item) {
  let urls = item.picUrls;
  if (typeof urls === 'string') {
    try { urls = JSON.parse(urls); } catch { urls = [urls]; }
  }
  const source = Array.isArray(urls) && urls.length ? urls : [item.picUrl];
  return [...new Set(source.filter(url => typeof url === 'string' && url.trim()).map(url => url.trim()))];
}
