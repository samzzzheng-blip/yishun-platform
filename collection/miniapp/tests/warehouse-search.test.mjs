import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';
const source = readFileSync(new URL('../pages/index/store.vue', import.meta.url), 'utf8');
function setup() {
  const calls = [];
  const context = { currentTab: { value: 1 }, keyword: { value: ' 小野友树 ' },
    showingProducts: {}, dataList: {}, listLoading: {}, listError: {}, listRequestId: 0,
    collectionApi: { getCollection: async p => { calls.push(['products', p]); return { code: 0, data: [{ id: 42, categoryId: 7, name: '小野友树签名' }] }; } },
    CollectuinCategoryApi: { getMyCollectionCategory: async p => { calls.push(['categories', p]); return { code: 0, data: [{ id: 7, name: '签名卡砖' }] }; } },
    sheep: { $router: { go: (...args) => calls.push(['route', ...args]) } },
  };
  vm.createContext(context);
  vm.runInContext(source.slice(source.indexOf('  async function getList()'), source.indexOf('  function handleEmptyAction()')) + source.slice(source.indexOf('  function goDetail(item)'), source.indexOf('  function yikoujia(item)')), context);
  return { context, calls };
}
test('产品名搜索返回具体藏品，点击携带藏品 ID 而非把产品 ID 当分类 ID', async () => {
  const { context: c, calls } = setup();
  await c.getList();
  assert.equal(calls[0][0], 'products');
  assert.equal(calls[0][1].collectionName, '小野友树');
  assert.equal(calls[0][1].status, 1);
  assert.equal(c.dataList.value[0].name, '小野友树签名');
  c.goDetail(c.dataList.value[0]);
  assert.equal(calls[1][2].id, 7);
  assert.equal(calls[1][2].collectionId, 42);
});
test('清空搜索恢复分类；失败响应显示错误', async () => {
  const { context: c, calls } = setup();
  c.keyword.value = '  ';
  await c.getList();
  assert.equal(calls[0][0], 'categories');
  assert.equal(c.showingProducts.value, false);
  c.keyword.value = '不存在';
  c.collectionApi.getCollection = async () => ({ code: 500 });
  await c.getList();
  assert.equal(c.listError.value, true);
  assert.equal(c.listLoading.value, false);
});
test('旧搜索响应不会覆盖清空搜索后的分类', async () => {
  const { context: c } = setup();
  let resolve;
  c.collectionApi.getCollection = () => new Promise(r => { resolve = r; });
  const old = c.getList();
  c.keyword.value = '';
  await c.getList();
  resolve({ code: 0, data: [{ id: 42, name: '旧结果' }] });
  await old;
  assert.equal(c.dataList.value[0].name, '签名卡砖');
  assert.equal(c.showingProducts.value, false);
});
