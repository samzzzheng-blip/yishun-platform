import test from 'node:test';
import assert from 'node:assert/strict';
import vm from 'node:vm';
import { readFileSync } from 'node:fs';
const source = readFileSync(new URL('../pages/collection/getbacklog.vue', import.meta.url), 'utf8');
function setup(getLogistics) {
  const c = { logisticsLoading: { value: false }, logistics: { value: null }, logisticsRequestId: 0,
    selected: { value: { id: 9, deliverCode: 'SF123' } }, getbackApi: { getLogistics } };
  vm.createContext(c);
  vm.runInContext(source.slice(source.indexOf('async function loadLogistics()'), source.indexOf('function copyCode()')), c);
  return c;
}
test('请求仅提交本人列表选中的取回编号，展示服务端轨迹', async () => {
  const c = setup(async id => {
    assert.equal(id, 9);
    return { code: 0, data: { availability: 'OK', status: '派送中', nodes: [{ time: '2026-09-16 12:00:00', description: '正在派送' }] } };
  });
  await c.loadLogistics();
  assert.equal(c.logistics.value.status, '派送中');
  assert.equal(c.logistics.value.nodes[0].description, '正在派送');
  assert.equal(c.logisticsLoading.value, false);
});
test('请求失败提供查询提示，不把失败当作运输状态', async () => {
  const c = setup(async () => { throw new Error('offline'); });
  await c.loadLogistics();
  assert.match(c.logistics.value.message, /暂不可用/);
  assert.equal(c.logistics.value.status, undefined);
  assert.equal(c.logisticsLoading.value, false);
});
test('页面重新加载后不接受旧的轨迹响应', async () => {
  let done;
  const c = setup(() => new Promise(resolve => { done = resolve; }));
  const pending = c.loadLogistics();
  c.logisticsRequestId++;
  c.logistics.value = null;
  done({ code: 0, data: { status: '已签收' } });
  await pending;
  assert.equal(c.logistics.value, null);
});
