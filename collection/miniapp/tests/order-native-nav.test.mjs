import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
const read = path => readFileSync(new URL(path, import.meta.url), 'utf8');
test('微信订单页编译为白底黑字原生导航，保留下拉刷新', () => {
  const config = JSON.parse(read('../unpackage/dist/build/mp-weixin/pages/order/list.json'));
  assert.equal(config.navigationStyle, 'default');
  assert.equal(config.navigationBarTitleText, '我的订单');
  assert.equal(config.navigationBarBackgroundColor, '#FFFFFF');
  assert.equal(config.navigationBarTextStyle, 'black');
  assert.equal(config.enablePullDownRefresh, true);
});
test('订单页禁用自定义导航，但保留原有业务入口', () => {
  const source = read('../pages/order/list.vue');
  assert.match(source, /:navbar="orderNavbar"/);
  assert.match(source, /#ifdef MP-WEIXIN\s+orderNavbar = 'none'/);
  assert.match(source, /<GetbackEntry\s*\/>/);
  assert.match(source, /name: '能量石兑换'/);
  const compiled = read('../unpackage/dist/build/mp-weixin/pages/order/list.js');
  assert.match(compiled, /navbar:(?:\w+\.unref\()?"none"/);
});
test('不改变其他页面的全局自定义导航', () => {
  const app = JSON.parse(read('../unpackage/dist/build/mp-weixin/app.json'));
  assert.equal(app.window.navigationStyle, 'custom');
});
