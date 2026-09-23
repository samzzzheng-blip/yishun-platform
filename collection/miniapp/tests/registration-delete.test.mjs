import test from 'node:test';
import assert from 'node:assert/strict';
import { canDeleteRegistration } from '../pages/index/registration-delete.mjs';
const item = { id: 1, status: 0, tradeStatus: 0, getbackStatus: 0 };
test('未关联包裹的待登记和驳回允许删除', () => {
  assert.equal(canDeleteRegistration(item, {}, true), true);
  assert.equal(canDeleteRegistration({ ...item, status: 2 }, {}, true), true);
});
test('加载失败、已登记包裹、在库或交易中不允许删除', () => {
  assert.equal(canDeleteRegistration(item, {}, false), false);
  for (const parcel_status of [0, 1, 2, 3]) assert.equal(canDeleteRegistration(item, { 1: { parcel_status } }, true), false);
  for (const patch of [{ status: 1 }, { tradeStatus: 1 }, { tradeStatus: 2 }, { getbackStatus: 1 }, { getbackStatus: 2 }])
    assert.equal(canDeleteRegistration({ ...item, ...patch }, {}, true), false);
});
