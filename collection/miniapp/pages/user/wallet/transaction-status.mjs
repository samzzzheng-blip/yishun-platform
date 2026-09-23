export function orderStatus(order) {
  const statuses = {
    '购买订单': { 0: '待支付', 1: '已支付', 2: '已取消' },
    '挂售订单': { 0: '待上架', 1: '已成交', 2: '已下架', 3: '在售中' },
    '快速变现订单': { 0: '待审核', 1: '已通过', 2: '已驳回' },
    '提现申请': { 0: '待审核', 10: '转账处理中', 11: '提现成功', 20: '已驳回', 21: '提现失败' },
  };
  if (order.kind === '充值订单') return order.status;
  if (order.kind === '竞拍结算') return '已结算';
  if (order.kind === '批量买单') return Number(order.status) === 0 ? '待支付' : Number(order.quantity) > 0 ? '已有成交' : '待成交';
  return statuses[order.kind]?.[order.status] || '状态待确认';
}
