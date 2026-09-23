import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { findGetback, getbackPhotos, getbackStatus } from '../pages/collection/getback-view.mjs';
test('detail only selects from current user response and preserves every item', () => {
  const record = { id: 2, items: Array.from({ length: 10 }, (_, id) => ({ id })) };
  assert.equal(findGetback([record], '2').items.length, 10);
  assert.equal(findGetback([record], '99'), null);
});
test('all photos survive with single image fallback and empty input', () => {
  assert.deepEqual(getbackPhotos({ picUrls: ['a', 'b', 'a'] }), ['a', 'b']);
  assert.deepEqual(getbackPhotos({ picUrls: [], picUrl: 'a' }), ['a']);
  assert.deepEqual(getbackPhotos({ picUrls: '["a","b"]' }), ['a', 'b']);
  assert.deepEqual(getbackPhotos({}), []);
});
test('status accepts API numbers and strings', () => {
  assert.equal(getbackStatus('1'), '已发货');
  assert.equal(getbackStatus(0), '待发货');
  assert.equal(getbackStatus(2), '已收货');
});
test('both warehouse screens and orders expose the common entry', () => {
  for (const file of ['order/list.vue', 'index/store.vue', 'collection/detail.vue']) {
    const source = readFileSync(new URL('../pages/' + file, import.meta.url), 'utf8');
    assert.match(source, /<GetbackEntry\s*\/>/);
    assert.match(source, /import GetbackEntry/);
  }
});
