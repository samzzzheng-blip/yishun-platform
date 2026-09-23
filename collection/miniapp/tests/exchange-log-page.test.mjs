import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';
const source = readFileSync(new URL('../pages/collection/exchangelog.vue', import.meta.url), 'utf8');
const handler = source.slice(source.indexOf('async function getLogList'), source.indexOf('function confirmDeliver'));
async function query(response, reject = false) {
  const completions = [];
  let params;
  const context = vm.createContext({
    exchangeApi: { getExchangeLog: async p => { params = p; if (reject) throw Error('network'); return response; } },
    paging: { value: { complete: value => completions.push(value) } }
  });
  vm.runInContext(handler, context);
  await context.getLogList(2, 10);
  return { params, completions };
}
test('分页组件参数传递给接口，成功返回记录数组', async () => {
  const rows = [{ id: 1, exchangeName: '兑换商品', status: 0 }];
  const result = await query({ code: 0, data: { list: rows, total: 11 } });
  assert.equal(result.params.pageNo, 2);
  assert.equal(result.params.pageSize, 10);
  assert.deepEqual(result.completions, [rows]);
  assert.match(source, /v-for="item in dataList"/);
});
test('无记录正常结束加载', async () => {
  assert.deepEqual((await query({ code: 0, data: { list: [] } })).completions, [[]]);
});
test('接口错误及网络失败进入分页错误状态', async () => {
  for (const result of [await query({ code: 401 }), await query({ code: 0, data: null }), await query(null, true)]) {
    assert.deepEqual(result.completions, [false]);
  }
});
