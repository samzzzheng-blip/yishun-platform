import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const source = readFileSync(new URL('../pages/collection/parcel.vue', import.meta.url), 'utf8');
function setup() {
  const saved = [];
  const api = {
    save: async payload => { saved.push(payload); return { code: 0, data: 7 }; },
    detail: async () => ({ code: 0, data: { id: 7, delivery_method: 'offline', items: [] } }),
  };
  const script = source.split('<script setup>')[1].split('</script>')[0].replace(/^\s*import .*;$/gm, '');
  const state = new Function('ref', 'reactive', 'onLoad', 'onShow', 'api', 'sheep', 'uni',
    script + '; return { form, save, formError, detail };')(
    value => ({ value }), value => value, () => {}, () => {}, api, {}, {},
  );
  return { ...state, saved };
}
test('线下送达不要求单号，忽略之前填写的快递信息', async () => {
  const s = setup();
  Object.assign(s.form, { deliveryMethod: 'offline', collectionIds: [1, 2], carrier: '顺丰', trackingNo: 'SF123456' });
  await s.save();
  assert.equal(s.saved.length, 1);
  assert.equal(s.saved[0].trackingNo, '');
  assert.equal(s.saved[0].carrier, '');
  assert.deepEqual(s.saved[0].collectionIds, [1, 2]);
  assert.equal(s.detail.value.id, 7);
});
test('邮寄仍需有效快递信息，所有方式必须选择藏品', async () => {
  const s = setup(); s.form.collectionIds = [1];
  await s.save(); assert.equal(s.saved.length, 0);
  s.form.deliveryMethod = 'offline'; s.form.collectionIds = [];
  await s.save(); assert.equal(s.saved.length, 0);
  assert.match(s.formError.value, /勾选/);
});
test('显示接收时间与方式选择，历史邮寄记录默认仍为邮寄', () => {
  assert.match(source, /周一至周五 9:00–18:00/);
  assert.match(source, /<radio value="offline"/);
  assert.match(source, /deliveryMethod: p\?\.delivery_method \|\| 'courier'/);
  assert.match(source, /到店请出示登记编号/);
});
