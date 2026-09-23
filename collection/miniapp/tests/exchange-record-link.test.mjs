import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
test('兑换中心记录入口直接打开订单兑换分类', () => {
  const source = readFileSync(new URL('../pages/collection/exchange.vue', import.meta.url), 'utf8');
  const body = source.match(/function goExchangeLog\(\)\s*\{([\s\S]*?)\n\}/)[1];
  let target;
  new Function('sheep', body)({ $router: { go: (...args) => { target = args; } } });
  assert.deepEqual(target, ['/pages/order/list', { tab: 'exchange' }]);
  const order = readFileSync(new URL('../pages/order/list.vue', import.meta.url), 'utf8');
  assert.match(order, /if \(options\?\.tab === 'exchange'\) currentTab.value = tabs.findIndex/);
});
