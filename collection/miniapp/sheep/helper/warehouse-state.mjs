export function warehouseStatus(item = {}) {
  if (Number(item.getbackStatus) === 1) return '取回处理中';
  if (Number(item.getbackStatus) === 2) return '已寄出';
  if (item.saleType === 'auction') return ({ 0: '待发布竞拍', 1: '竞拍中', 2: '待确认成交', 6: '竞拍待核实' })[item.listingStatus] || '竞拍处理中';
  if (item.saleType === 'direct') return Number(item.listingStatus) === 0 ? '待上架' : '在售';
  if (Number(item.tradeStatus) === 1) return '变现中';
  if (Number(item.tradeStatus) === 2) return '挂售中';
  return '在库';
}

export function warehouseAction(item = {}) {
  if (Number(item.getbackStatus) !== 0 || !(Number(item.stock) > 0) || !item.listingId) return '';
  if (item.saleType === 'direct' && Number(item.listingStatus) === 0) return '取消上架';
  if (item.saleType === 'direct' && Number(item.listingStatus) === 3) return '下架';
  if (item.saleType === 'auction') return [0, 6].includes(Number(item.listingStatus)) ? '取消送拍' : '管理竞拍';
  return '';
}

export function isGetbackSelectable(item = {}) {
  return Number(item.stock) > 0 && item.saleType !== 'auction' && item.getbackStatus === 0 && [0, 2].includes(item.tradeStatus);
}
