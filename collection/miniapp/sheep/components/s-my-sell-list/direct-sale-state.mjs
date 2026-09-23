export function canDelist(item) {
  return item?.saleType === 'direct' && item.status != null && [0, 3].includes(Number(item.status));
}

export function directSaleStatus(item) {
  if (item?.status === 0 || item?.status === '0') return '待上架';
  if (canDelist(item)) return '已上架';
  return '状态待更新';
}
