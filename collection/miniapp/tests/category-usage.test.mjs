import test from 'node:test';
import assert from 'node:assert/strict';
import { sortCategoriesByUsage } from '../pages/collection/category-usage.mjs';
test('按数据库使用数量排序，不修改原数组或丢失新增类型', () => {
  const input = [2, 3, 1, 75, 76, 999, 998].map(id => ({ id }));
  assert.deepEqual(sortCategoriesByUsage(input).map(i => i.id), [1, 76, 75, 3, 2, 999, 998]);
  assert.deepEqual(input.map(i => i.id), [2, 3, 1, 75, 76, 999, 998]);
  assert.deepEqual(sortCategoriesByUsage([]), []);
});
test('公共签名卡砖固定第二位，兼容字符串编号且不改动输入', () => {
  const input = [680, 2, 76, 1, 999].map(id => ({ id: String(id) }));
  assert.deepEqual(sortCategoriesByUsage(input).map(i => i.id), ['1', '680', '76', '2', '999']);
  assert.equal(input[0].id, '680');
  assert.deepEqual(sortCategoriesByUsage([{ id: 680 }]), [{ id: 680 }]);
});
